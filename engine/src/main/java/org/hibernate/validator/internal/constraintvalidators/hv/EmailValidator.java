/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;
import org.hibernate.validator.internal.util.DomainNameUtil;

/**
 * Checks that a given character sequence (e.g. string) is a well-formed email address.
 * Supports the {@code allowTld} parameter to control whether top-level domain only addresses are allowed.
 *
 * @since 8.0.1
 */
public class EmailValidator extends AbstractEmailValidator<Email> {

	private boolean allowTld;

	@Override
	public void initialize(Email emailAnnotation) {
		super.initialize( emailAnnotation );
		this.allowTld = emailAnnotation.allowTld();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}

		if ( !super.isValid( value, context ) ) {
			return false;
		}

		if ( !allowTld ) {
			String stringValue = value.toString();
			int splitPosition = stringValue.lastIndexOf( '@' );
			String domainPart = stringValue.substring( splitPosition + 1 );

			if ( DomainNameUtil.isTldOnlyDomain( domainPart ) ) {
				return false;
			}
		}

		return true;
	}
}