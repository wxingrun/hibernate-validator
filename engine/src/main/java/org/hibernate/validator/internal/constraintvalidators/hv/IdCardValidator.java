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

/**
 * Validates that a given character sequence is a valid Chinese Identity Card (ID card) number.
 */
public class IdCardValidator implements ConstraintValidator<IdCard, CharSequence> {

	private static final Pattern ID_CARD_PATTERN = Pattern.compile( "\\d{17}[\\dxX]" );

	private static final int[] WEIGHTS = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };
	private static final char[] CHECK_CHARS = { '1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2' };

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		String idCard = value.toString();

		if ( !ID_CARD_PATTERN.matcher( idCard ).matches() ) {
			return false;
		}

		if ( !isValidDate( idCard ) ) {
			return false;
		}

		return isValidChecksum( idCard );
	}

	private boolean isValidDate(String idCard) {
		try {
			int year = Integer.parseInt( idCard.substring( 6, 10 ) );
			int month = Integer.parseInt( idCard.substring( 10, 12 ) );
			int day = Integer.parseInt( idCard.substring( 12, 14 ) );

			if ( year < 1900 || year > 2100 ) {
				return false;
			}

			LocalDate date = LocalDate.of( year, month, day );
			return date.getYear() == year && date.getMonthValue() == month && date.getDayOfMonth() == day;
		}
		catch (DateTimeException | NumberFormatException e) {
			return false;
		}
	}

	private boolean isValidChecksum(String idCard) {
		int sum = 0;
		for ( int i = 0; i < 17; i++ ) {
			sum += ( idCard.charAt( i ) - '0' ) * WEIGHTS[i];
		}

		char expectedCheckChar = CHECK_CHARS[sum % 11];
		char actualCheckChar = Character.toUpperCase( idCard.charAt( 17 ) );

		return expectedCheckChar == actualCheckChar;
	}
}
