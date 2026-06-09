/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit tests for the {@link EmailValidator} with {@code allowTld} parameter.
 *
 * @author Hibernate Validator Team
 */
public class EmailValidatorAllowTldTest {

	private EmailValidator validatorWithTld;
	private EmailValidator validatorWithoutTld;

	@BeforeClass
	public static void init() {
	}

	@Test
	public void testAllowTldTrueAcceptsTldOnlyDomains() {
		validatorWithTld = new EmailValidator();
		validatorWithTld.initialize( new EmailAnnotationWithAllowTldTrue() );

		assertTrue( validatorWithTld.isValid( "user@com", null ), "TLD-only domain 'com' should be valid when allowTld=true" );
		assertTrue( validatorWithTld.isValid( "admin@org", null ), "TLD-only domain 'org' should be valid when allowTld=true" );
		assertTrue( validatorWithTld.isValid( "test@net", null ), "TLD-only domain 'net' should be valid when allowTld=true" );
		assertTrue( validatorWithTld.isValid( "info@io", null ), "TLD-only domain 'io' should be valid when allowTld=true" );
	}

	@Test
	public void testAllowTldTrueStillAcceptsNormalDomains() {
		validatorWithTld = new EmailValidator();
		validatorWithTld.initialize( new EmailAnnotationWithAllowTldTrue() );

		assertTrue( validatorWithTld.isValid( "user@example.com", null ), "Normal domain should still be valid when allowTld=true" );
		assertTrue( validatorWithTld.isValid( "admin@sub.domain.org", null ), "Subdomain should still be valid when allowTld=true" );
		assertTrue( validatorWithTld.isValid( "test@hibernate.org", null ), "Standard email should still be valid when allowTld=true" );
	}

	@Test
	public void testAllowTldFalseRejectsTldOnlyDomains() {
		validatorWithoutTld = new EmailValidator();
		validatorWithoutTld.initialize( new EmailAnnotationWithAllowTldFalse() );

		assertFalse( validatorWithoutTld.isValid( "user@com", null ), "TLD-only domain 'com' should be invalid when allowTld=false" );
		assertFalse( validatorWithoutTld.isValid( "admin@org", null ), "TLD-only domain 'org' should be invalid when allowTld=false" );
		assertFalse( validatorWithoutTld.isValid( "test@net", null ), "TLD-only domain 'net' should be invalid when allowTld=false" );
	}

	@Test
	public void testAllowTldFalseAcceptsNormalDomains() {
		validatorWithoutTld = new EmailValidator();
		validatorWithoutTld.initialize( new EmailAnnotationWithAllowTldFalse() );

		assertTrue( validatorWithoutTld.isValid( "user@example.com", null ), "Normal domain should be valid when allowTld=false" );
		assertTrue( validatorWithoutTld.isValid( "admin@sub.domain.org", null ), "Subdomain should be valid when allowTld=false" );
		assertTrue( validatorWithoutTld.isValid( "test@hibernate.org", null ), "Standard email should be valid when allowTld=false" );
	}

	@Test
	public void testNullAndEmptyAreAlwaysValid() {
		validatorWithTld = new EmailValidator();
		validatorWithTld.initialize( new EmailAnnotationWithAllowTldTrue() );

		assertTrue( validatorWithTld.isValid( null, null ), "Null should always be valid" );
		assertTrue( validatorWithTld.isValid( "", null ), "Empty string should always be valid" );

		validatorWithoutTld = new EmailValidator();
		validatorWithoutTld.initialize( new EmailAnnotationWithAllowTldFalse() );

		assertTrue( validatorWithoutTld.isValid( null, null ), "Null should always be valid" );
		assertTrue( validatorWithoutTld.isValid( "", null ), "Empty string should always be valid" );
	}

	@Test
	public void testInvalidEmailsRejectedRegardlessOfAllowTld() {
		validatorWithTld = new EmailValidator();
		validatorWithTld.initialize( new EmailAnnotationWithAllowTldTrue() );

		assertFalse( validatorWithTld.isValid( "invalid", null ), "Email without @ should be invalid" );
		assertFalse( validatorWithTld.isValid( "@domain.com", null ), "Email without local part should be invalid" );
		assertFalse( validatorWithTld.isValid( "user@", null ), "Email without domain should be invalid" );

		validatorWithoutTld = new EmailValidator();
		validatorWithoutTld.initialize( new EmailAnnotationWithAllowTldFalse() );

		assertFalse( validatorWithoutTld.isValid( "invalid", null ), "Email without @ should be invalid" );
		assertFalse( validatorWithoutTld.isValid( "@domain.com", null ), "Email without local part should be invalid" );
		assertFalse( validatorWithoutTld.isValid( "user@", null ), "Email without domain should be invalid" );
	}

	private static class EmailAnnotationWithAllowTldTrue implements Email {
		@Override
		public String message() {
			return "{org.hibernate.validator.constraints.Email.message}";
		}

		@Override
		public Class<?>[] groups() {
			return new Class<?>[0];
		}

		@Override
		public Class<?>[] payload() {
			return new Class<?>[0];
		}

		@Override
		public boolean allowTld() {
			return true;
		}

		@Override
		public String regexp() {
			return ".*";
		}

		@Override
		public jakarta.validation.constraints.Pattern.Flag[] flags() {
			return new jakarta.validation.constraints.Pattern.Flag[0];
		}

		@Override
		public Class<? extends java.lang.annotation.Annotation> annotationType() {
			return Email.class;
		}
	}

	private static class EmailAnnotationWithAllowTldFalse implements Email {
		@Override
		public String message() {
			return "{org.hibernate.validator.constraints.Email.message}";
		}

		@Override
		public Class<?>[] groups() {
			return new Class<?>[0];
		}

		@Override
		public Class<?>[] payload() {
			return new Class<?>[0];
		}

		@Override
		public boolean allowTld() {
			return false;
		}

		@Override
		public String regexp() {
			return ".*";
		}

		@Override
		public jakarta.validation.constraints.Pattern.Flag[] flags() {
			return new jakarta.validation.constraints.Pattern.Flag[0];
		}

		@Override
		public Class<? extends java.lang.annotation.Annotation> annotationType() {
			return Email.class;
		}
	}
}
