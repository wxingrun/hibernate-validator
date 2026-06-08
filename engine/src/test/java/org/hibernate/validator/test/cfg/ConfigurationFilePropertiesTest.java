/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cfg;

import java.lang.reflect.Field;
import java.time.Duration;

import jakarta.validation.Configuration;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.bootstrap.GenericBootstrap;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Chris Beckey
 */
public class ConfigurationFilePropertiesTest {

	@Test
	public void testAllowMultipleCascadedValidationOnReturnValues() {
		runWithCustomValidationXml( "ConfigurationFilePropertiesTest_validation.xml", new Runnable() {

			@Override
			public void run() {
				GenericBootstrap provider = Validation.byDefaultProvider();
				Assert.assertNotNull( provider );

				Configuration<?> config = provider.configure();
				Assert.assertNotNull( config );
				Assert.assertTrue( config instanceof HibernateValidatorConfiguration );

				HibernateValidatorConfiguration hibernateConfig = (HibernateValidatorConfiguration) config;
				ValidatorFactory factory = hibernateConfig.buildValidatorFactory();
				Validator validator = factory.getValidator();

				ValidatorImpl hibernateValidatorImpl = (ValidatorImpl) validator;
				BeanMetaDataManager bmdm = findPropertyOfType( hibernateValidatorImpl, BeanMetaDataManager.class );
				MethodValidationConfiguration methodConfig = findPropertyOfType( bmdm, MethodValidationConfiguration.class );

				Assert.assertTrue( methodConfig.isAllowMultipleCascadedValidationOnReturnValues() );
			}
		} );
	}

	@Test
	public void testAllowOverridingMethodAlterParameterConstraint() {
		runWithCustomValidationXml( "ConfigurationFilePropertiesTest_validation.xml", new Runnable() {

			@Override
			public void run() {
				GenericBootstrap provider = Validation.byDefaultProvider();
				Assert.assertNotNull( provider );

				Configuration<?> config = provider.configure();
				Assert.assertNotNull( config );
				Assert.assertTrue( config instanceof HibernateValidatorConfiguration );

				HibernateValidatorConfiguration hibernateConfig = (HibernateValidatorConfiguration) config;
				ValidatorFactory factory = hibernateConfig.buildValidatorFactory();
				Validator validator = factory.getValidator();

				ValidatorImpl hibernateValidatorImpl = (ValidatorImpl) validator;
				BeanMetaDataManager bmdm = findPropertyOfType( hibernateValidatorImpl, BeanMetaDataManager.class );
				MethodValidationConfiguration methodConfig = findPropertyOfType( bmdm, MethodValidationConfiguration.class );

				Assert.assertTrue( methodConfig.isAllowOverridingMethodAlterParameterConstraint() );
			}
		} );
	}

	@Test
	public void testAllowParallelMethodsDefineParameterConstraints() {
		runWithCustomValidationXml( "ConfigurationFilePropertiesTest_validation.xml", new Runnable() {

			@Override
			public void run() {
				GenericBootstrap provider = Validation.byDefaultProvider();
				Assert.assertNotNull( provider );

				Configuration<?> config = provider.configure();
				Assert.assertNotNull( config );
				Assert.assertTrue( config instanceof HibernateValidatorConfiguration );

				HibernateValidatorConfiguration hibernateConfig = (HibernateValidatorConfiguration) config;
				ValidatorFactory factory = hibernateConfig.buildValidatorFactory();
				Validator validator = factory.getValidator();

				ValidatorImpl hibernateValidatorImpl = (ValidatorImpl) validator;
				BeanMetaDataManager bmdm = findPropertyOfType( hibernateValidatorImpl, BeanMetaDataManager.class );
				MethodValidationConfiguration methodConfig = findPropertyOfType( bmdm, MethodValidationConfiguration.class );

				Assert.assertTrue( methodConfig.isAllowParallelMethodsDefineParameterConstraints() );
			}
		} );
	}

	@Test
	public void testBeanMetaDataCacheConfiguredThroughValidationXml() {
		runWithCustomValidationXml( "ConfigurationFilePropertiesTest_validation.xml", new Runnable() {

			@Override
			public void run() {
				GenericBootstrap provider = Validation.byDefaultProvider();
				HibernateValidatorConfiguration hibernateConfig = (HibernateValidatorConfiguration) provider.configure();

				ValidatorFactory factory = hibernateConfig.buildValidatorFactory();
				ValidatorImpl validator = (ValidatorImpl) factory.getValidator();
				BeanMetaDataManager beanMetaDataManager = findPropertyOfType( validator, BeanMetaDataManager.class );

				Assert.assertEquals( findFieldValue( beanMetaDataManager, "beanMetaDataCacheMaxSize", Long.class ), Long.valueOf( 128 ) );
				Assert.assertEquals( findFieldValue( beanMetaDataManager, "beanMetaDataCacheExpireAfterAccess", Duration.class ), Duration.ofSeconds( 30 ) );
			}
		} );
	}

	@Test
	public void testBeanMetaDataCacheConfiguredProgrammatically() {
		HibernateValidatorConfiguration configuration = Validation.byProvider( HibernateValidator.class ).configure();
		ValidatorFactory factory = configuration
				.beanMetaDataCacheMaxSize( 64 )
				.beanMetaDataCacheExpireAfterAccess( Duration.ofSeconds( 5 ) )
				.buildValidatorFactory();
		ValidatorImpl validator = (ValidatorImpl) factory.getValidator();
		BeanMetaDataManager beanMetaDataManager = findPropertyOfType( validator, BeanMetaDataManager.class );

		Assert.assertEquals( findFieldValue( beanMetaDataManager, "beanMetaDataCacheMaxSize", Long.class ), Long.valueOf( 64 ) );
		Assert.assertEquals( findFieldValue( beanMetaDataManager, "beanMetaDataCacheExpireAfterAccess", Duration.class ), Duration.ofSeconds( 5 ) );
	}

	@IgnoreForbiddenApisErrors(reason = "Prints the stacktrace in case an exception is raised")
	private <T> T findPropertyOfType(Object subject, Class<T> clazz) {
		Field[] fields = subject.getClass().getDeclaredFields();
		for ( Field field : fields ) {
			if ( field.getType().equals( clazz ) ) {
				boolean accessible = field.canAccess( subject );
				try {
					field.setAccessible( true );
					return clazz.cast( field.get( subject ) );
				}
				catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				finally {
					field.setAccessible( accessible );
				}
			}
		}
		return null;
	}

	@IgnoreForbiddenApisErrors(reason = "Uses reflection to access private fields in tests")
	private <T> T findFieldValue(Object subject, String fieldName, Class<T> fieldType) {
		try {
			Field field = subject.getClass().getDeclaredField( fieldName );
			boolean accessible = field.canAccess( subject );
			field.setAccessible( true );
			Object value = field.get( subject );
			field.setAccessible( accessible );
			return fieldType.cast( value );
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			throw new AssertionError( e );
		}
	}

	private void runWithCustomValidationXml(String validationXmlName, Runnable runnable) {
		new ValidationXmlTestHelper( ConfigurationFilePropertiesTest.class ).runWithCustomValidationXml( validationXmlName, runnable );
	}
}
