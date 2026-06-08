/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors.
 */
package org.hibernate.validator.internal.metadata;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;
import org.hibernate.validator.spi.tracking.ProcessedBeansTrackingVoter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;

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
 * <p>
 * The cache is backed by Caffeine and supports configuration of maximum size
 * and expire-after-access duration. By default, the cache is unbounded and
 * entries do not expire. Keys are compared by identity and values are held
 * by soft reference to allow garbage collection under memory pressure.
 *
 * @author Gunnar Morling
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
*/
public class BeanMetaDataManagerImpl implements BeanMetaDataManager {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

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
	 * Used to cache the constraint meta data for validated entities.
	 * Backed by Caffeine with soft values for memory-sensitive caching.
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
			ProcessedBeansTrackingVoter processedBeansTrackingVoter) {
		this( constraintCreationContext, executableHelper, parameterNameProvider, javaBeanHelper,
				beanMetaDataClassNormalizer, validationOrderGenerator, optionalMetaDataProviders,
				methodValidationConfiguration, processedBeansTrackingVoter, -1, -1 );
	}

	public BeanMetaDataManagerImpl(ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			JavaBeanHelper javaBeanHelper,
			BeanMetaDataClassNormalizer beanMetaDataClassNormalizer,
			ValidationOrderGenerator validationOrderGenerator,
			List<MetaDataProvider> optionalMetaDataProviders,
			MethodValidationConfiguration methodValidationConfiguration,
			ProcessedBeansTrackingVoter processedBeansTrackingVoter,
			long beanMetaDataCacheMaxSize,
			long beanMetaDataCacheExpireAfterAccessInMinutes) {
		this.constraintCreationContext = constraintCreationContext;
		this.executableHelper = executableHelper;
		this.parameterNameProvider = parameterNameProvider;
		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
		this.validationOrderGenerator = validationOrderGenerator;
		this.methodValidationConfiguration = methodValidationConfiguration;
		this.processedBeansTrackingVoter = processedBeansTrackingVoter;

		Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
				.weakKeys()
				.softValues()
				.removalListener( (Class<?> key, BeanMetaData<?> value, RemovalCause cause) -> {
					if ( LOG.isDebugEnabled() ) {
						LOG.debug( "Bean meta data cache entry removed for class " + key.getName() + " due to " + cause );
					}
				} );

		if ( beanMetaDataCacheMaxSize > 0 ) {
			cacheBuilder.maximumSize( beanMetaDataCacheMaxSize );
			if ( LOG.isDebugEnabled() ) {
				LOG.debug( "Bean meta data cache configured with maximum size " + beanMetaDataCacheMaxSize );
			}
		}

		if ( beanMetaDataCacheExpireAfterAccessInMinutes > 0 ) {
			cacheBuilder.expireAfterAccess( Duration.ofMinutes( beanMetaDataCacheExpireAfterAccessInMinutes ) );
			if ( LOG.isDebugEnabled() ) {
				LOG.debug( "Bean meta data cache configured with expire after access of " + beanMetaDataCacheExpireAfterAccessInMinutes + " minutes" );
			}
		}

		this.beanMetaDataCache = cacheBuilder.build();

		AnnotationProcessingOptions annotationProcessingOptions = getAnnotationProcessingOptionsFromNonDefaultProviders( optionalMetaDataProviders );
		AnnotationMetaDataProvider defaultProvider = new AnnotationMetaDataProvider(
				constraintCreationContext,
				javaBeanHelper,
				annotationProcessingOptions
		);
		List<MetaDataProvider> tmpMetaDataProviders = new ArrayList<>( optionalMetaDataProviders.size() + 1 );
		tmpMetaDataProviders.add( defaultProvider );
		tmpMetaDataProviders.addAll( optionalMetaDataProviders );

		this.metaDataProviders = CollectionHelper.toImmutableList( tmpMetaDataProviders );
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass, MESSAGES.beanTypeCannotBeNull() );

		Class<? super T> normalizedBeanClass = beanMetaDataClassNormalizer.normalize( beanClass );

		BeanMetaData<? super T> beanMetaData = (BeanMetaData<? super T>) beanMetaDataCache.getIfPresent( normalizedBeanClass );

		if ( beanMetaData != null ) {
			if ( LOG.isDebugEnabled() ) {
				LOG.debug( "Bean meta data cache hit for class " + normalizedBeanClass.getName() );
			}
			return (BeanMetaData<T>) beanMetaData;
		}

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Bean meta data cache miss for class " + normalizedBeanClass.getName() + ", creating new meta data" );
		}

		beanMetaData = createBeanMetaData( normalizedBeanClass );
		BeanMetaData<? super T> previousBeanMetaData =
				(BeanMetaData<? super T>) beanMetaDataCache.asMap().putIfAbsent( normalizedBeanClass, beanMetaData );

		if ( previousBeanMetaData != null ) {
			return (BeanMetaData<T>) previousBeanMetaData;
		}

		return (BeanMetaData<T>) beanMetaData;
	}

	@Override
	public void clear() {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Clearing bean meta data cache, current size: " + beanMetaDataCache.estimatedSize() );
		}
		beanMetaDataCache.invalidateAll();
	}

	public int numberOfCachedBeanMetaDataInstances() {
		return (int) beanMetaDataCache.estimatedSize();
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
