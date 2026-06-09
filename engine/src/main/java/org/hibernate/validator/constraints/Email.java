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
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email.List;

/**
 * Validates the annotated character sequence is a well-formed email address.
 * <p>
 * This constraint extends the Jakarta Validation {@code @Email} constraint by providing
 * additional options for email validation.
 * </p>
 * <p>
 * When {@link #allowTld()} is set to {@code true}, email addresses with only a top-level
 * domain (e.g., {@code user@com}, {@code admin@org}) are considered valid. By default,
 * such addresses are invalid.
 * </p>
 *
 * @author Hibernate Validator Team
 * @see jakarta.validation.constraints.Email
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@ReportAsSingleViolation
@jakarta.validation.constraints.Email(regexp = ".*")
public @interface Email {

	String message() default "{org.hibernate.validator.constraints.Email.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Whether to allow email addresses with only a top-level domain (e.g., {@code user@com},
	 * {@code admin@org}). When set to {@code true}, such addresses are considered valid.
	 * <p>
	 * Default is {@code false}, which requires the domain part to contain at least one dot
	 * (e.g., {@code user@example.com}).
	 * </p>
	 *
	 * @return {@code true} if TLD-only domains are allowed, {@code false} otherwise
	 */
	boolean allowTld() default false;

	/**
	 * @return an additional regular expression the annotated email address must match.
	 *         The default is any string ('.*')
	 */
	@OverridesAttribute(constraint = jakarta.validation.constraints.Email.class, name = "regexp")
	String regexp() default ".*";

	/**
	 * @return used in combination with {@link #regexp()} in order to specify a regular expression option
	 */
	@OverridesAttribute(constraint = jakarta.validation.constraints.Email.class, name = "flags")
	Pattern.Flag[] flags() default { };

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
