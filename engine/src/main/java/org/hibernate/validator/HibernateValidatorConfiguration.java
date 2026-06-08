/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator;

import java.time.Duration;

/**
 * Uniquely identifies Hibernate Validator in the Bean Validation bootstrap
 * strategy. Also contains Hibernate Validator specific configurations.
 *
 * @author Guillaume Smet
 */
public interface HibernateValidatorConfiguration extends BaseHibernateValidatorConfiguration<HibernateValidatorConfiguration> {

	/**
	 * Property for configuring the maximum number of bean metadata entries kept in the metadata cache.
	 *
	 * @since 9.2
	 */
	@Incubating
	String BEAN_META_DATA_CACHE_MAX_SIZE = "hibernate.validator.bean_metadata_cache_max_size";

	/**
	 * Property for configuring the time in milliseconds after which bean metadata cache entries expire when not accessed.
	 *
	 * @since 9.2
	 */
	@Incubating
	String BEAN_META_DATA_CACHE_EXPIRE_AFTER_ACCESS = "hibernate.validator.bean_metadata_cache_expire_after_access";

	/**
	 * Configures the maximum number of bean metadata entries kept in the metadata cache.
	 *
	 * @param maxSize the maximum number of entries to keep in the cache
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 9.2
	 */
	@Incubating
	default HibernateValidatorConfiguration beanMetaDataCacheMaxSize(long maxSize) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Configures how long bean metadata cache entries may remain unused before expiring.
	 *
	 * @param expireAfterAccess the expire-after-access duration
	 * @return {@code this} following the chaining method pattern
	 *
	 * @since 9.2
	 */
	@Incubating
	default HibernateValidatorConfiguration beanMetaDataCacheExpireAfterAccess(Duration expireAfterAccess) {
		throw new UnsupportedOperationException();
	}
}
