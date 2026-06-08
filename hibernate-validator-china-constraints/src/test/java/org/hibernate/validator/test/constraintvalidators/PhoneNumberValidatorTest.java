/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraintvalidators;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.hibernate.validator.constraints.PhoneNumber;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

public class PhoneNumberValidatorTest {

    private Validator validator;

    @BeforeMethod
    public void setUp() {
        validator = ValidatorUtil.getValidator();
    }

    @Test
    public void testValidPhoneNumbersInStrictMode() {
        Set<ConstraintViolation<PhoneNumberTestBean>> constraintViolations = validator.validate(
                new PhoneNumberTestBean( "13800138000", null, null, null )
        );
        assertThat( constraintViolations ).isEmpty();

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( null, "15912345678", null, null )
        );
        assertThat( constraintViolations ).isEmpty();

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( null, null, "17612345678", null )
        );
        assertThat( constraintViolations ).isEmpty();

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( null, null, null, "19912345678" )
        );
        assertThat( constraintViolations ).isEmpty();
    }

    @Test
    public void testInvalidPhoneNumbersInStrictMode() {
        Set<ConstraintViolation<PhoneNumberTestBean>> constraintViolations = validator.validate(
                new PhoneNumberTestBean( "12345678901", null, null, null )
        );
        assertThat( constraintViolations ).containsOnlyViolations(
                violationOf( PhoneNumber.class ).withProperty( "strictPhoneNumber1" )
        );

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( null, "23800138000", null, null )
        );
        assertThat( constraintViolations ).containsOnlyViolations(
                violationOf( PhoneNumber.class ).withProperty( "strictPhoneNumber2" )
        );

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( null, null, "1380013800", null )
        );
        assertThat( constraintViolations ).containsOnlyViolations(
                violationOf( PhoneNumber.class ).withProperty( "strictPhoneNumber3" )
        );

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( null, null, null, "138001380001" )
        );
        assertThat( constraintViolations ).containsOnlyViolations(
                violationOf( PhoneNumber.class ).withProperty( "strictPhoneNumber4" )
        );
    }

    @Test
    public void testValidPhoneNumbersInLooseMode() {
        Set<ConstraintViolation<PhoneNumberTestBean>> constraintViolations = validator.validate(
                new PhoneNumberTestBean( "12345678901", null, null, null, true )
        );
        assertThat( constraintViolations ).isEmpty();

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( "11111111111", null, null, null, true )
        );
        assertThat( constraintViolations ).isEmpty();
    }

    @Test
    public void testInvalidPhoneNumbersInLooseMode() {
        Set<ConstraintViolation<PhoneNumberTestBean>> constraintViolations = validator.validate(
                new PhoneNumberTestBean( "23800138000", null, null, null, true )
        );
        assertThat( constraintViolations ).containsOnlyViolations(
                violationOf( PhoneNumber.class ).withProperty( "loosePhoneNumber" )
        );

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( "1234567890", null, null, null, true )
        );
        assertThat( constraintViolations ).containsOnlyViolations(
                violationOf( PhoneNumber.class ).withProperty( "loosePhoneNumber" )
        );
    }

    @Test
    public void testNullAndEmptyValues() {
        Set<ConstraintViolation<PhoneNumberTestBean>> constraintViolations = validator.validate(
                new PhoneNumberTestBean( null, null, null, null )
        );
        assertThat( constraintViolations ).isEmpty();

        constraintViolations = validator.validate(
                new PhoneNumberTestBean( "", null, null, null )
        );
        assertThat( constraintViolations ).isEmpty();
    }

    private static class PhoneNumberTestBean {
        @PhoneNumber
        private String strictPhoneNumber1;

        @PhoneNumber
        private String strictPhoneNumber2;

        @PhoneNumber
        private String strictPhoneNumber3;

        @PhoneNumber
        private String strictPhoneNumber4;

        @PhoneNumber(strict = false)
        private String loosePhoneNumber;

        public PhoneNumberTestBean(String strictPhoneNumber1, String strictPhoneNumber2, String strictPhoneNumber3, String strictPhoneNumber4) {
            this.strictPhoneNumber1 = strictPhoneNumber1;
            this.strictPhoneNumber2 = strictPhoneNumber2;
            this.strictPhoneNumber3 = strictPhoneNumber3;
            this.strictPhoneNumber4 = strictPhoneNumber4;
        }

        public PhoneNumberTestBean(String loosePhoneNumber, String unused1, String unused2, String unused3, boolean unused) {
            this.loosePhoneNumber = loosePhoneNumber;
        }
    }
}
