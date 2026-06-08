/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.time.DateTimeException;
import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.IdCard;

/**
 * Checks that a given character sequence is a valid Chinese Resident Identity Card number
 * (2nd generation, 18 digits) per GB 11643-1999.
 *
 * @since 9.0
 */
public class IdCardValidator implements ConstraintValidator<IdCard, CharSequence> {

	private static final int ID_CARD_LENGTH = 18;

	private static final int[] WEIGHTS = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	private static final char[] CHECK_CODES = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };

	@Override
	public void initialize(IdCard constraintAnnotation) {
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String idCard = value.toString();

		if ( idCard.length() != ID_CARD_LENGTH ) {
			return false;
		}

		if ( !isValidFormat( idCard ) ) {
			return false;
		}

		if ( !isValidBirthDate( idCard ) ) {
			return false;
		}

		return isValidCheckCode( idCard );
	}

	private static boolean isValidFormat(String idCard) {
		for ( int i = 0; i < ID_CARD_LENGTH - 1; i++ ) {
			if ( !Character.isDigit( idCard.charAt( i ) ) ) {
				return false;
			}
		}

		char lastChar = idCard.charAt( ID_CARD_LENGTH - 1 );
		return Character.isDigit( lastChar ) || lastChar == 'X' || lastChar == 'x';
	}

	private static boolean isValidBirthDate(String idCard) {
		int year;
		int month;
		int day;

		try {
			year = Integer.parseInt( idCard.substring( 6, 10 ) );
			month = Integer.parseInt( idCard.substring( 10, 12 ) );
			day = Integer.parseInt( idCard.substring( 12, 14 ) );
		}
		catch (NumberFormatException e) {
			return false;
		}

		if ( year < 1900 || year > 2100 ) {
			return false;
		}

		if ( month < 1 || month > 12 ) {
			return false;
		}

		if ( day < 1 ) {
			return false;
		}

		try {
			LocalDate.of( year, month, day );
		}
		catch (DateTimeException e) {
			return false;
		}

		return true;
	}

	private static boolean isValidCheckCode(String idCard) {
		int sum = 0;
		for ( int i = 0; i < ID_CARD_LENGTH - 1; i++ ) {
			sum += ( idCard.charAt( i ) - '0' ) * WEIGHTS[i];
		}

		int remainder = sum % 11;
		char expectedCheckCode = CHECK_CODES[remainder];

		char actualCheckCode = Character.toUpperCase( idCard.charAt( ID_CARD_LENGTH - 1 ) );
		return expectedCheckCode == actualCheckCode;
	}
}
