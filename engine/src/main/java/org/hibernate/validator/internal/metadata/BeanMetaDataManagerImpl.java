/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.time.Duration;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.util.ArrayList;
import java.util.List;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataBuilder;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaDataImpl;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.provider.AnnotationMetaDataProvider;
import org.hibernate.validator.internal.metadata.provider.MetaDataProvider;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.spi.tracking.ProcessedBeansTrackingVoter;

/**
 * This manager is in charge of providing all constraint related meta data
 * required by the validation engine.
 * <p>
 * Actual retrieval of meta data is delegated to {@link MetaDataProvider}
 * implementations which load meta-data based e.g. based on annotations or XML.
 * <p>
 * For performance reasons a cache is used which stores all meta data once
 * loaded for repeated retrieval. Upon initialization this cache is populated
 * with meta data provided by the given <i>eager</i> providers. If the cache
 * doesn't contain the meta data for a requested type it will be retrieved on
 * demand using the annotation based provider.
 *
 * @author Gunnar Morling
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
*/
public class BeanMetaDataManagerImpl implements BeanMetaDataManager {
	private static final Log LOG = LoggerFactory.make();

	/**
	 * The default initial capacity for this cache.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	/**
	 * Additional metadata providers used for meta data retrieval if
	 * the XML and/or programmatic configuration is used.
	 */
	@Immutable
	private final List<MetaDataProvider> metaDataProviders;

	/**
	 * The constraint creation context containing all the helpers necessary to the constraint creation.
	 */
	private final ConstraintCreationContext constraintCreationContext;

	private final ExecutableParameterNameProvider parameterNameProvider;

	/**
	 * Used to cache the constraint meta data for validated entities
	 */
	private final Cache<Class<?>, BeanMetaData<?>> beanMetaDataCache;

	/**
	 * Used for resolving type parameters. Thread-safe.
	 */
	private final ExecutableHelper executableHelper;

	private final BeanMetaDataClassNormalizer beanMetaDataClassNormalizer;

	private final ValidationOrderGenerator validationOrderGenerator;

	private final ProcessedBeansTrackingVoter processedBeansTrackingVoter;

	/**
	 * the three properties in this field affect the invocation of rules associated to section 4.5.5
	 * of the specification.  By default they are all false, if true they allow
	 * for relaxation of the Liskov Substitution Principal.
	 */
	private final MethodValidationConfiguration methodValidationConfiguration;

	public BeanMetaDataManagerImpl(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			BeanMetaDataClassNormalizer beanMetaDataClassNormalizer,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration,
			ProcessedBeansTrackingVoter processedBeansTrackingVoter,
			Integer metadataCacheMaxSize,
			Duration metadataCacheExpireAfterWrite) {
		this.constraintCreationContext = constraintCreationContext;
		this.executableHelper = executableHelper;
		this.parameterNameProvider = parameterNameProvider;
		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
		this.validationOrderGenerator = validationOrderGenerator;
		this.methodValidationConfiguration = methodValidationConfiguration;
		this.processedBeansTrackingVoter = processedBeansTrackingVoter;

		Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
				.initialCapacity(DEFAULT_INITIAL_CAPACITY);

		if (metadataCacheMaxSize != null) {
			caffeineBuilder.maximumSize(metadataCacheMaxSize);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Setting bean metadata cache maximum size to: " + metadataCacheMaxSize);
			}
		}

		if (metadataCacheExpireAfterWrite != null) {
			caffeineBuilder.expireAfterWrite(metadataCacheExpireAfterWrite);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Setting bean metadata cache expire after write to: " + metadataCacheExpireAfterWrite);
			}
		}

		caffeineBuilder.removalListener(new RemovalListener<Class<?>, BeanMetaData<?>>() {
			@Override
			public void onRemoval(@Nullable Class<?> key, @Nullable BeanMetaData<?> value, RemovalCause cause) {
				if (LOG.isDebugEnabled() && key != null) {
					LOG.debug("Bean metadata for class " + key.getName() + " was removed from cache, cause: " + cause);
				}
			}
		});

		this.beanMetaDataCache = caffeineBuilder.build();

		AnnotationProcessingOptions annotationProcessingOptions = getAnnotationProcessingOptionsFromNonDefaultProviders( optionalMetaDataProviders );
		AnnotationMetaDataProvider defaultProvider = new AnnotationMetaDataProvider(
				constraintCreationContext,
				javaBeanHelper,
				annotationProcessingOptions
		);
		List<MetaDataProvider> tmpMetaDataProviders = new ArrayList<>( optionalMetaDataProviders.size() + 1 );
		// We add the annotation based metadata provider at the first position so that the entire metadata model is assembled
		// first.
		// The other optional metadata providers will then contribute their additional metadata to the preexisting model.
		// This helps to mitigate issues like HV-1450.
		tmpMetaDataProviders.add( defaultProvider );
		tmpMetaDataProviders.addAll( optionalMetaDataProviders );

		this.metaDataProviders = CollectionHelper.toImmutableList( tmpMetaDataProviders );
	}

	@Override
	// TODO Some of these casts from BeanMetadata<? super T> to BeanMetadata<T> may not be safe.
	//  Maybe we should return a wrapper around the BeanMetadata if the normalized class is different from beanClass?
	@SuppressWarnings("unchecked")
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass, MESSAGES.beanTypeCannotBeNull() );

		Class<? super T> normalizedBeanClass = beanMetaDataClassNormalizer.normalize( beanClass );

		// First, let's do a simple lookup as it's the default case
		BeanMetaData<? super T> beanMetaData = (BeanMetaData<? super T>) beanMetaDataCache.getIfPresent( normalizedBeanClass );

		if ( beanMetaData != null ) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Bean metadata cache hit for class: " + normalizedBeanClass.getName());
			}
			return (BeanMetaData<T>) beanMetaData;
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Bean metadata cache miss for class: " + normalizedBeanClass.getName());
		}

		beanMetaData = createBeanMetaData( normalizedBeanClass );
		BeanMetaData<? super T> previousBeanMetaData =
				(BeanMetaData<? super T>) beanMetaDataCache.asMap().putIfAbsent( normalizedBeanClass, beanMetaData );

		// we return the previous value if not null
		if ( previousBeanMetaData != null ) {
			return (BeanMetaData<T>) previousBeanMetaData;
		}

		return (BeanMetaData<T>) beanMetaData;
	}

	@Override
	public void clear() {
		beanMetaDataCache.clear();
	}

	public int numberOfCachedBeanMetaDataInstances() {
		return beanMetaDataCache.size();
	}

	/**
	 * Creates a {@link org.hibernate.validator.internal.metadata.aggregated.BeanMetaData} containing the meta data from all meta
	 * data providers for the given type and its hierarchy.
	 *
	 * @param <T> The type of interest.
	 * @param clazz The type's class.
	 *
	 * @return A bean meta data object for the given type.
	 */
	private <T> BeanMetaDataImpl<T> createBeanMetaData(Class<T> clazz) {
		BeanMetaDataBuilder<T> builder = BeanMetaDataBuilder.getInstance(
				constraintCreationContext, executableHelper, parameterNameProvider,
				validationOrderGenerator, clazz, methodValidationConfiguration,
				processedBeansTrackingVoter );

		for ( MetaDataProvider provider : metaDataProviders ) {
			for ( BeanConfiguration<? super T> beanConfiguration : getBeanConfigurationForHierarchy( provider, clazz ) ) {
				builder.add( beanConfiguration );
			}
		}

		return builder.build();
	}

	/**
	 * @return returns the annotation ignores from the non annotation based meta data providers
	 */
	private AnnotationProcessingOptions getAnnotationProcessingOptionsFromNonDefaultProviders(List<MetaDataProvider> optionalMetaDataProviders) {
		AnnotationProcessingOptions options = new AnnotationProcessingOptionsImpl();
		for ( MetaDataProvider metaDataProvider : optionalMetaDataProviders ) {
			options.merge( metaDataProvider.getAnnotationProcessingOptions() );
		}

		return options;
	}

	/**
	 * Returns a list with the configurations for all types contained in the given type's hierarchy (including
	 * implemented interfaces) starting at the specified type.
	 *
	 * @param beanClass The type of interest.
	 * @param <T> The type of the class to get the configurations for.
	 * @return A set with the configurations for the complete hierarchy of the given type. May be empty, but never
	 * {@code null}.
	 */
	private <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(MetaDataProvider provider, Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> clazz : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = provider.getBeanConfiguration( clazz );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}
}
