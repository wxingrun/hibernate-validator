/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import org.hibernate.validator.constraints.IdCard.List;

/**
 * Checks that the annotated character sequence is a valid Chinese citizen
 * identity card number (18-digit, Second Generation ID Card).
 * <p>
 * The validation includes:
 * <ul>
 * <li>length check (must be exactly 18 characters)</li>
 * <li>birth date check (must be a valid date between 1900 and 2100)</li>
 * <li>checksum check (last digit must match the calculated checksum,
 * supports X/x as checksum value 10)</li>
 * </ul>
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 *
 * @since 1.1.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface IdCard {

	String message() default "{org.hibernate.validator.constraints.IdCard.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @IdCard} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		IdCard[] value();
	}
}