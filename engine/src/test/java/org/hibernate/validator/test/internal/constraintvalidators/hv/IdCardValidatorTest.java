/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.constraints.IdCard;
import org.hibernate.validator.internal.constraintvalidators.hv.IdCardValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdCardValidatorTest {

	private IdCardValidator validator;

	@BeforeMethod
	public void setUp() {
		validator = new IdCardValidator();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( IdCard.class ).build().getAnnotation() );
	}

	@Test
	public void validIdCard() {
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( "11010519491231002X", null ) );
		assertTrue( validator.isValid( "11010519491231002x", null ) );
	}

	@Test
	public void invalidIdCard() {
		assertFalse( validator.isValid( "110105194912310021", null ) );
		assertFalse( validator.isValid( "11010518991231002X", null ) );
		assertFalse( validator.isValid( "11010519490231002X", null ) );
		assertFalse( validator.isValid( "11010519491231002", null ) );
		assertFalse( validator.isValid( "11010519491231002A", null ) );
	}

	@Test
	public void constraintIsRegisteredViaConstraintDefinitionContributor() {
		Validator beanValidator = ValidatorUtil.getValidator();

		assertNoViolations( beanValidator.validate( new Person( "11010519491231002X" ) ) );

		Set<ConstraintViolation<Person>> violations = beanValidator.validate( new Person( "110105194912310021" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( IdCard.class ).withProperty( "idCard" ).withMessage( "invalid Chinese Resident Identity Card number" )
		);
	}

	@Test
	public void supportsGroups() {
		Validator beanValidator = ValidatorUtil.getValidator();

		assertNoViolations( beanValidator.validate( new GroupedPerson( "110105194912310021" ) ) );

		Set<ConstraintViolation<GroupedPerson>> violations = beanValidator.validate( new GroupedPerson( "110105194912310021" ), Strict.class );
		assertThat( violations ).containsOnlyViolations(
				violationOf( IdCard.class ).withProperty( "idCard" )
		);
	}

	private interface Strict {
	}

	private static class Person {

		@IdCard
		private final String idCard;

		private Person(String idCard) {
			this.idCard = idCard;
		}
	}

	private static class GroupedPerson {

		@IdCard(groups = Strict.class)
		private final String idCard;

		private GroupedPerson(String idCard) {
			this.idCard = idCard;
		}
	}
}
