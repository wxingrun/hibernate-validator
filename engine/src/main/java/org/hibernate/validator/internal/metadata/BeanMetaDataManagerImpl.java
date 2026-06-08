/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	@Immutable
	private final List<MetaDataProvider> metaDataProviders;

	private final ConstraintCreationContext constraintCreationContext;

	private final ExecutableParameterNameProvider parameterNameProvider;

	private final Cache<Class<?>, BeanMetaData<?>> beanMetaDataCache;

	private final Long beanMetaDataCacheMaxSize;

	private final Duration beanMetaDataCacheExpireAfterAccess;

	private final ExecutableHelper executableHelper;

	private final BeanMetaDataClassNormalizer beanMetaDataClassNormalizer;

	private final ValidationOrderGenerator validationOrderGenerator;

	private final ProcessedBeansTrackingVoter processedBeansTrackingVoter;

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
		this(
				constraintCreationContext,
				executableHelper,
				parameterNameProvider,
				javaBeanHelper,
				beanMetaDataClassNormalizer,
				validationOrderGenerator,
				optionalMetaDataProviders,
				methodValidationConfiguration,
				processedBeansTrackingVoter,
				null,
				null
		);
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
			Long beanMetaDataCacheMaxSize,
			Duration beanMetaDataCacheExpireAfterAccess) {
		this.constraintCreationContext = constraintCreationContext;
		this.executableHelper = executableHelper;
		this.parameterNameProvider = parameterNameProvider;
		this.beanMetaDataClassNormalizer = beanMetaDataClassNormalizer;
		this.validationOrderGenerator = validationOrderGenerator;
		this.methodValidationConfiguration = methodValidationConfiguration;
		this.processedBeansTrackingVoter = processedBeansTrackingVoter;
		this.beanMetaDataCacheMaxSize = beanMetaDataCacheMaxSize;
		this.beanMetaDataCacheExpireAfterAccess = beanMetaDataCacheExpireAfterAccess;

		Caffeine<Object, Object> beanMetaDataCacheBuilder = Caffeine.newBuilder()
				.initialCapacity( DEFAULT_INITIAL_CAPACITY )
				.softValues();

		if ( beanMetaDataCacheMaxSize != null ) {
			beanMetaDataCacheBuilder.maximumSize( beanMetaDataCacheMaxSize );
		}

		if ( beanMetaDataCacheExpireAfterAccess != null ) {
			beanMetaDataCacheBuilder.expireAfterAccess( beanMetaDataCacheExpireAfterAccess );
		}

		this.beanMetaDataCache = beanMetaDataCacheBuilder.build();

		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Initialized bean metadata cache with max size " + formatCacheSetting( beanMetaDataCacheMaxSize, "unbounded" )
					+ " and expire-after-access " + formatCacheSetting( beanMetaDataCacheExpireAfterAccess, "disabled" ) + "." );
		}

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
				LOG.debug( "Bean metadata cache hit for " + normalizedBeanClass.getName() + "." );
			}
			return (BeanMetaData<T>) beanMetaData;
		}

		return (BeanMetaData<T>) beanMetaDataCache.get( normalizedBeanClass, this::createBeanMetaDataAndLog );
	}

	@Override
	public void clear() {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Clearing bean metadata cache." );
		}
		beanMetaDataCache.invalidateAll();
		beanMetaDataCache.cleanUp();
	}

	public int numberOfCachedBeanMetaDataInstances() {
		return (int) Math.min( Integer.MAX_VALUE, beanMetaDataCache.estimatedSize() );
	}

	private <T> BeanMetaData<T> createBeanMetaDataAndLog(Class<T> clazz) {
		if ( LOG.isDebugEnabled() ) {
			LOG.debug( "Loading bean metadata for " + clazz.getName() + " into cache." );
		}
		return createBeanMetaData( clazz );
	}

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

	private AnnotationProcessingOptions getAnnotationProcessingOptionsFromNonDefaultProviders(List<MetaDataProvider> optionalMetaDataProviders) {
		AnnotationProcessingOptions options = new AnnotationProcessingOptionsImpl();
		for ( MetaDataProvider metaDataProvider : optionalMetaDataProviders ) {
			options.merge( metaDataProvider.getAnnotationProcessingOptions() );
		}

		return options;
	}

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

	private static String formatCacheSetting(Object setting, String defaultValue) {
		return setting != null ? setting.toString() : defaultValue;
	}
}
