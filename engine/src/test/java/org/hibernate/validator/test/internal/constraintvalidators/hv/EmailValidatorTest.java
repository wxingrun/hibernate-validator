/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for the {@link Email} constraint and its {@link EmailValidator}.
 */
public class EmailValidatorTest {

	private EmailValidator emailValidator;
	private ConstraintAnnotationDescriptor.Builder<Email> descriptorBuilder;

	@BeforeMethod
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Email.class );
		emailValidator = new EmailValidator();
	}

	@Test
	public void allowsNullAndEmpty() {
		Email emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertTrue( emailValidator.isValid( null, null ) );
		assertTrue( emailValidator.isValid( "", null ) );
	}

	@Test
	public void defaultRejectsTldOnlyDomain() {
		Email emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertFalse( emailValidator.isValid( "user@com", null ),
				"user@com should be rejected when allowTld=false (default)" );
		assertFalse( emailValidator.isValid( "admin@org", null ),
				"admin@org should be rejected when allowTld=false (default)" );
		assertFalse( emailValidator.isValid( "test@net", null ),
				"test@net should be rejected when allowTld=false (default)" );
	}

	@Test
	public void allowTldTrueAllowsTldOnlyDomain() {
		descriptorBuilder.setAttribute( "allowTld", true );
		Email emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertTrue( emailValidator.isValid( "user@com", null ),
				"user@com should be valid when allowTld=true" );
		assertTrue( emailValidator.isValid( "admin@org", null ),
				"admin@org should be valid when allowTld=true" );
		assertTrue( emailValidator.isValid( "test@net", null ),
				"test@net should be valid when allowTld=true" );
	}

	@Test
	public void fullDomainAlwaysAllowed() {
		descriptorBuilder.setAttribute( "allowTld", false );
		Email emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertTrue( emailValidator.isValid( "emmanuel@hibernate.org", null ) );
		assertTrue( emailValidator.isValid( "user@example.com", null ) );
		assertTrue( emailValidator.isValid( "test@foo.bar", null ) );

		descriptorBuilder.setAttribute( "allowTld", true );
		emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertTrue( emailValidator.isValid( "emmanuel@hibernate.org", null ) );
		assertTrue( emailValidator.isValid( "user@example.com", null ) );
		assertTrue( emailValidator.isValid( "test@foo.bar", null ) );
	}

	@Test
	public void ipAddressDomainAlwaysAllowed() {
		descriptorBuilder.setAttribute( "allowTld", false );
		Email emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertTrue( emailValidator.isValid( "emmanuel@[123.12.2.11]", null ),
				"IP address domain should be valid even with allowTld=false" );
		assertTrue( emailValidator.isValid( "user@[IPv6:2001:DB8::1]", null ),
				"IPv6 domain should be valid even with allowTld=false" );

		descriptorBuilder.setAttribute( "allowTld", true );
		emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertTrue( emailValidator.isValid( "emmanuel@[123.12.2.11]", null ) );
		assertTrue( emailValidator.isValid( "user@[IPv6:2001:DB8::1]", null ) );
	}

	@Test
	public void allowTldTrueDoesNotAffectInvalidEmails() {
		descriptorBuilder.setAttribute( "allowTld", true );
		Email emailAnnotation = descriptorBuilder.build().getAnnotation();
		emailValidator.initialize( emailAnnotation );

		assertFalse( emailValidator.isValid( "invalid", null ) );
		assertFalse( emailValidator.isValid( "emmanuel.hibernate.org", null ) );
		assertFalse( emailValidator.isValid( "@example.com", null ) );
		assertFalse( emailValidator.isValid( "emmanuel@", null ) );
		assertFalse( emailValidator.isValid( "emma nuel@hibernate.org", null ) );
	}

	@Test
	public void localhostIsTldOnly() {
		emailValidator.initialize( descriptorBuilder.build().getAnnotation() );

		// "localhost" does not contain a dot and is not an IP bracket, so it's TLD-only
		assertFalse( emailValidator.isValid( "example@localhost", null ),
				"example@localhost should be rejected by default as TLD-only" );

		descriptorBuilder.setAttribute( "allowTld", true );
		emailValidator.initialize( descriptorBuilder.build().getAnnotation() );

		assertTrue( emailValidator.isValid( "example@localhost", null ),
				"example@localhost should be valid when allowTld=true" );
	}
}