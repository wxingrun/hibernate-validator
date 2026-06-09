/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Integration tests for the {@link Email} annotation with {@code allowTld} parameter.
 *
 * @author Hibernate Validator Team
 */
public class EmailAllowTldIntegrationTest {

	private Validator validator;

	@BeforeClass
	public void setUp() {
		validator = getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-allowTld")
	public void testAllowTldTrueWithTldOnlyDomain() {
		EmailContainerWithAllowTldTrue container = new EmailContainerWithAllowTldTrue();
		container.setEmail( "user@com" );

		Set<ConstraintViolation<EmailContainerWithAllowTldTrue>> violations = validator.validate( container );
		assertThat( violations ).isEmpty();
	}

	@Test
	@TestForIssue(jiraKey = "HV-allowTld")
	public void testAllowTldTrueWithNormalDomain() {
		EmailContainerWithAllowTldTrue container = new EmailContainerWithAllowTldTrue();
		container.setEmail( "user@example.com" );

		Set<ConstraintViolation<EmailContainerWithAllowTldTrue>> violations = validator.validate( container );
		assertThat( violations ).isEmpty();
	}

	@Test
	@TestForIssue(jiraKey = "HV-allowTld")
	public void testAllowTldFalseWithTldOnlyDomain() {
		EmailContainerWithAllowTldFalse container = new EmailContainerWithAllowTldFalse();
		container.setEmail( "user@com" );

		Set<ConstraintViolation<EmailContainerWithAllowTldFalse>> violations = validator.validate( container );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-allowTld")
	public void testAllowTldFalseWithNormalDomain() {
		EmailContainerWithAllowTldFalse container = new EmailContainerWithAllowTldFalse();
		container.setEmail( "user@example.com" );

		Set<ConstraintViolation<EmailContainerWithAllowTldFalse>> violations = validator.validate( container );
		assertThat( violations ).isEmpty();
	}

	@Test
	@TestForIssue(jiraKey = "HV-allowTld")
	public void testDefaultBehaviorRejectsTldOnlyDomain() {
		EmailContainerDefault container = new EmailContainerDefault();
		container.setEmail( "user@com" );

		Set<ConstraintViolation<EmailContainerDefault>> violations = validator.validate( container );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-allowTld")
	public void testDefaultBehaviorAcceptsNormalDomain() {
		EmailContainerDefault container = new EmailContainerDefault();
		container.setEmail( "user@example.com" );

		Set<ConstraintViolation<EmailContainerDefault>> violations = validator.validate( container );
		assertThat( violations ).isEmpty();
	}

	@SuppressWarnings("unused")
	private static class EmailContainerWithAllowTldTrue {
		@Email(allowTld = true)
		private String email;

		public void setEmail(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}
	}

	@SuppressWarnings("unused")
	private static class EmailContainerWithAllowTldFalse {
		@Email(allowTld = false)
		private String email;

		public void setEmail(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}
	}

	@SuppressWarnings("unused")
	private static class EmailContainerDefault {
		@Email
		private String email;

		public void setEmail(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}
	}
}
