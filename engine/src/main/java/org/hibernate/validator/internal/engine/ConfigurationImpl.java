/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine;

import java.time.Duration;

import jakarta.validation.spi.BootstrapState;
import jakarta.validation.spi.ConfigurationState;
import jakarta.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.util.Contracts;

/**
 * Hibernate specific {@code Configuration} implementation.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 */
public class ConfigurationImpl extends AbstractConfigurationImpl<HibernateValidatorConfiguration> implements HibernateValidatorConfiguration, ConfigurationState {

	private Long beanMetaDataCacheMaxSize;

	private Duration beanMetaDataCacheExpireAfterAccess;

	public ConfigurationImpl(BootstrapState state) {
		super( state );
	}

	public ConfigurationImpl(ValidationProvider<?> provider) {
		super( provider );
	}

	@Override
	public HibernateValidatorConfiguration beanMetaDataCacheMaxSize(long maxSize) {
		Contracts.assertTrue( maxSize > 0, "beanMetaDataCacheMaxSize must be greater than 0" );
		this.beanMetaDataCacheMaxSize = maxSize;
		return this;
	}

	@Override
	public HibernateValidatorConfiguration beanMetaDataCacheExpireAfterAccess(Duration expireAfterAccess) {
		Contracts.assertNotNull( expireAfterAccess, "beanMetaDataCacheExpireAfterAccess must not be null" );

		Duration normalizedExpireAfterAccess = expireAfterAccess.abs();
		Contracts.assertTrue( !normalizedExpireAfterAccess.isZero(), "beanMetaDataCacheExpireAfterAccess must be greater than 0" );

		this.beanMetaDataCacheExpireAfterAccess = normalizedExpireAfterAccess;
		return this;
	}

	public Long getBeanMetaDataCacheMaxSize() {
		return beanMetaDataCacheMaxSize;
	}

	public Duration getBeanMetaDataCacheExpireAfterAccess() {
		return beanMetaDataCacheExpireAfterAccess;
	}

	@Override
	protected boolean preloadResourceBundles() {
		return false;
	}
}
