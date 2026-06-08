/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.IdCard;

public class IdCardValidator implements ConstraintValidator<IdCard, CharSequence> {

	private static final Pattern ID_CARD_PATTERN = Pattern.compile( "\\d{17}[\\dXx]" );
	private static final int[] CHECKSUM_WEIGHTS = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
	private static final char[] CHECKSUM_CODES = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };
	private static final int MIN_YEAR = 1900;
	private static final int MAX_YEAR = 2100;

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String idCard = value.toString();
		if ( !ID_CARD_PATTERN.matcher( idCard ).matches() ) {
			return false;
		}

		return isValidBirthDate( idCard ) && isValidChecksum( idCard );
	}

	private boolean isValidBirthDate(String idCard) {
		int year = parseNumber( idCard, 6, 10 );
		if ( year < MIN_YEAR || year > MAX_YEAR ) {
			return false;
		}

		int month = parseNumber( idCard, 10, 12 );
		int day = parseNumber( idCard, 12, 14 );

		try {
			LocalDate.of( year, month, day );
			return true;
		}
		catch (DateTimeException e) {
			return false;
		}
	}

	private boolean isValidChecksum(String idCard) {
		int sum = 0;
		for ( int i = 0; i < CHECKSUM_WEIGHTS.length; i++ ) {
			sum += Character.getNumericValue( idCard.charAt( i ) ) * CHECKSUM_WEIGHTS[i];
		}

		char expectedChecksum = CHECKSUM_CODES[sum % CHECKSUM_CODES.length];
		char actualChecksum = Character.toUpperCase( idCard.charAt( idCard.length() - 1 ) );
		return expectedChecksum == actualChecksum;
	}

	private int parseNumber(String value, int startInclusive, int endExclusive) {
		return Integer.parseInt( value.substring( startInclusive, endExclusive ) );
	}
}
