/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;
import org.hibernate.validator.internal.util.DomainNameUtil;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Validates that a given character sequence is a well-formed email address.
 * <p>
 * This validator extends the standard email validation by supporting the {@code allowTld}
 * option. When {@code allowTld} is {@code true}, email addresses with only a top-level
 * domain (e.g., {@code user@com}, {@code admin@org}) are considered valid.
 * </p>
 *
 * @author Hibernate Validator Team
 */
public class EmailValidator extends AbstractEmailValidator<Email> implements ConstraintValidator<Email, CharSequence> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private boolean allowTld;

	@Override
	public void initialize(Email emailAnnotation) {
		this.allowTld = emailAnnotation.allowTld();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}

		String stringValue = value.toString();
		int splitPosition = stringValue.lastIndexOf( '@' );

		if ( splitPosition < 0 ) {
			return false;
		}

		String localPart = stringValue.substring( 0, splitPosition );
		String domainPart = stringValue.substring( splitPosition + 1 );

		if ( !isValidEmailLocalPart( localPart ) ) {
			return false;
		}

		if ( allowTld ) {
			return DomainNameUtil.isValidEmailDomainAddressAllowTld( domainPart );
		}

		return DomainNameUtil.isValidEmailDomainAddress( domainPart );
	}

	private boolean isValidEmailLocalPart(String localPart) {
		if ( localPart.length() > 64 ) {
			return false;
		}
		java.util.regex.Matcher matcher = LOCAL_PART_PATTERN.matcher( localPart );
		return matcher.matches();
	}
}
