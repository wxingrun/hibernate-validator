/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.IdCard;

/**
 * Validates a Chinese citizen identity card number (18-digit, Second Generation ID Card).
 * <p>
 * The validation algorithm checks:
 * <ol>
 * <li>Length is exactly 18 characters</li>
 * <li>First 17 characters are all digits</li>
 * <li>Birth date (encoded in positions 6-13) is a valid date between 1900-01-01 and 2100-12-31</li>
 * <li>The checksum digit (position 17) matches the calculated value per GB 11643-1999</li>
 * </ol>
 *
 * @since 1.1.2
 */
public class IdCardValidator implements ConstraintValidator<IdCard, CharSequence> {

	private static final int[] WEIGHTS = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	private static final char[] CHECK_CODES = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };

	private static final int LENGTH = 18;

	private static final int MIN_YEAR = 1900;

	private static final int MAX_YEAR = 2100;

	@Override
	public boolean isValid(CharSequence idCardNumber, ConstraintValidatorContext context) {
		if ( idCardNumber == null ) {
			return true;
		}

		String value = idCardNumber.toString();

		if ( value.length() != LENGTH ) {
			return false;
		}

		if ( !checkBirthDate( value ) ) {
			return false;
		}

		if ( !checkChecksum( value ) ) {
			return false;
		}

		return true;
	}

	private boolean checkBirthDate(String value) {
		int year;
		int month;
		int day;
		try {
			year = Integer.parseInt( value.substring( 6, 10 ) );
			month = Integer.parseInt( value.substring( 10, 12 ) );
			day = Integer.parseInt( value.substring( 12, 14 ) );
		}
		catch (NumberFormatException e) {
			return false;
		}

		if ( year < MIN_YEAR || year > MAX_YEAR ) {
			return false;
		}

		if ( month < 1 || month > 12 ) {
			return false;
		}

		if ( day < 1 || day > 31 ) {
			return false;
		}

		try {
			LocalDate.of( year, month, day );
		}
		catch (java.time.DateTimeException e) {
			return false;
		}

		return true;
	}

	private boolean checkChecksum(String value) {
		int sum = 0;
		for ( int i = 0; i < 17; i++ ) {
			char c = value.charAt( i );
			if ( c < '0' || c > '9' ) {
				return false;
			}
			sum += ( c - '0' ) * WEIGHTS[i];
		}

		int remainder = sum % 11;
		char expectedCheckCode = CHECK_CODES[remainder];
		char actualCheckCode = value.charAt( 17 );

		if ( expectedCheckCode == 'X' ) {
			return actualCheckCode == 'X' || actualCheckCode == 'x';
		}
		return actualCheckCode == expectedCheckCode;
	}
}