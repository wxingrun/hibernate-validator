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

import org.hibernate.validator.constraints.Email.List;

/**
 * Validates the annotated string is a well-formed email address.
 * <p>
 * The exact rules of a valid email address can be found in
 * <a href="http://www.faqs.org/rfcs/rfc2822.html">RFC 2822</a>.
 * This validator aims to check most email addresses in use while not being 100% compliant with the specification.
 * </p>
 * <p>
 * By default, the domain part of the email address must contain at least one dot (e.g. {@code user@example.com}).
 * Use {@link #allowTld()} to also allow top-level domain only addresses (e.g. {@code user@com}).
 * </p>
 *
 * @since 8.0.1
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface Email {

	String message() default "{org.hibernate.validator.constraints.Email.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return {@code true} if top-level domain only addresses (e.g. {@code user@com}, {@code admin@org}) are allowed.
	 * Per default, TLD-only addresses are not allowed and the domain part must contain at least one dot.
	 */
	boolean allowTld() default false;

	/**
	 * Defines several {@code @Email} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		Email[] value();
	}
}