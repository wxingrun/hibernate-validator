/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraints.PhoneNumber;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, CharSequence> {

    private static final Pattern STRICT_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern LOOSE_PATTERN = Pattern.compile("^1\\d{10}$");

    private boolean strict;

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        this.strict = constraintAnnotation.strict();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if ( value == null || value.length() == 0 ) {
            return true;
        }

        Pattern pattern = strict ? STRICT_PATTERN : LOOSE_PATTERN;
        return pattern.matcher( value ).matches();
    }
}
