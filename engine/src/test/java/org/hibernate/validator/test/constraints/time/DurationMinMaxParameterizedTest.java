/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.DurationMaxDef;
import org.hibernate.validator.cfg.defs.DurationMinDef;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMinValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DurationMinMaxParameterizedTest {

	private static Locale PREVIOUS_LOCALE;

	@BeforeClass
	public static void saveLocale() {
		PREVIOUS_LOCALE = Locale.getDefault();
	}

	@AfterClass
	public static void restoreLocale() {
		Locale.setDefault( PREVIOUS_LOCALE );
	}

	@DataProvider(name = "durationMinInclusiveValidDurations")
	private static Object[][] durationMinInclusiveValidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 1000L ), "Duration - nanos above boundary" },
				{ Duration.ofNanos( 100L ), "Duration - nanos at boundary (inclusive)" },
				{ Duration.ofSeconds( 100L ), "Duration - seconds far above boundary" },
				{ Duration.ofMinutes( 5L ), "Duration - minutes above boundary" },
				{ Duration.ofHours( 2L ), "Duration - hours above boundary" },
				{ Duration.ofDays( 1L ), "Duration - days above boundary" },
				{ Duration.ofMillis( 1L ), "Duration - millis above boundary" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000100Z" ) ), "Instant - at boundary (inclusive)" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:01Z" ) ), "Instant - above boundary" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 0, 100 ) ), "LocalDateTime - at boundary (inclusive)" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 1, 0 ) ), "LocalDateTime - above boundary" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - at boundary (inclusive)" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 2, 0, 0, 0, 0, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - above boundary" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneOffset.UTC ) ), "OffsetDateTime - at boundary (inclusive)" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC ) ), "OffsetDateTime - above boundary" },
				{ null, "null value is always valid" }
		};
	}

	@DataProvider(name = "durationMinInclusiveInvalidDurations")
	private static Object[][] durationMinInclusiveInvalidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 10L ), "Duration - nanos below boundary" },
				{ Duration.ofNanos( 99L ), "Duration - nanos just below boundary" },
				{ Duration.ZERO, "Duration - zero" },
				{ Duration.ofDays( -1L ), "Duration - negative days" },
				{ Duration.ofSeconds( -100L ), "Duration - negative seconds" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000099Z" ) ), "Instant - just below boundary" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0 ) ), "LocalDateTime - zero duration" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 99, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - just below boundary" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 99, ZoneOffset.UTC ) ), "OffsetDateTime - just below boundary" }
		};
	}

	@DataProvider(name = "durationMinExclusiveValidDurations")
	private static Object[][] durationMinExclusiveValidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 101L ), "Duration - nanos above boundary (exclusive)" },
				{ Duration.ofNanos( 1000L ), "Duration - nanos well above boundary" },
				{ Duration.ofSeconds( 100L ), "Duration - seconds far above boundary" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000101Z" ) ), "Instant - above boundary (exclusive)" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 0, 101 ) ), "LocalDateTime - above boundary (exclusive)" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 101, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - above boundary (exclusive)" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 101, ZoneOffset.UTC ) ), "OffsetDateTime - above boundary (exclusive)" },
				{ null, "null value is always valid" }
		};
	}

	@DataProvider(name = "durationMinExclusiveInvalidDurations")
	private static Object[][] durationMinExclusiveInvalidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 100L ), "Duration - nanos at boundary (exclusive fails)" },
				{ Duration.ofNanos( 10L ), "Duration - nanos below boundary" },
				{ Duration.ZERO, "Duration - zero" },
				{ Duration.ofDays( -1L ), "Duration - negative" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000100Z" ) ), "Instant - at boundary (exclusive fails)" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 0, 100 ) ), "LocalDateTime - at boundary (exclusive fails)" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - at boundary (exclusive fails)" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneOffset.UTC ) ), "OffsetDateTime - at boundary (exclusive fails)" }
		};
	}

	@DataProvider(name = "durationMaxInclusiveValidDurations")
	private static Object[][] durationMaxInclusiveValidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 10L ), "Duration - nanos below boundary" },
				{ Duration.ofNanos( 99L ), "Duration - nanos just below boundary" },
				{ Duration.ofNanos( 100L ), "Duration - nanos at boundary (inclusive)" },
				{ Duration.ZERO, "Duration - zero" },
				{ Duration.ofNanos( 0L ), "Duration - zero nanos" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000100Z" ) ), "Instant - at boundary (inclusive)" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000010Z" ) ), "Instant - below boundary" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 0, 100 ) ), "LocalDateTime - at boundary (inclusive)" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 0, 10 ) ), "LocalDateTime - below boundary" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - at boundary (inclusive)" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 10, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - below boundary" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneOffset.UTC ) ), "OffsetDateTime - at boundary (inclusive)" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 10, ZoneOffset.UTC ) ), "OffsetDateTime - below boundary" },
				{ null, "null value is always valid" }
		};
	}

	@DataProvider(name = "durationMaxInclusiveInvalidDurations")
	private static Object[][] durationMaxInclusiveInvalidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 101L ), "Duration - nanos just above boundary" },
				{ Duration.ofNanos( 1000L ), "Duration - nanos well above boundary" },
				{ Duration.ofSeconds( 100L ), "Duration - seconds far above boundary" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000101Z" ) ), "Instant - above boundary" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 1 ) ), "LocalDateTime - above boundary" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 1, 0, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - above boundary" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 1, 0, ZoneOffset.UTC ) ), "OffsetDateTime - above boundary" }
		};
	}

	@DataProvider(name = "durationMaxExclusiveValidDurations")
	private static Object[][] durationMaxExclusiveValidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 99L ), "Duration - nanos just below boundary (exclusive)" },
				{ Duration.ofNanos( 10L ), "Duration - nanos well below boundary" },
				{ Duration.ZERO, "Duration - zero" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000099Z" ) ), "Instant - below boundary (exclusive)" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 0, 99 ) ), "LocalDateTime - below boundary (exclusive)" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 99, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - below boundary (exclusive)" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 99, ZoneOffset.UTC ) ), "OffsetDateTime - below boundary (exclusive)" },
				{ null, "null value is always valid" }
		};
	}

	@DataProvider(name = "durationMaxExclusiveInvalidDurations")
	private static Object[][] durationMaxExclusiveInvalidDurations() {
		return new Object[][] {
				{ Duration.ofNanos( 100L ), "Duration - nanos at boundary (exclusive fails)" },
				{ Duration.ofNanos( 101L ), "Duration - nanos above boundary" },
				{ Duration.ofSeconds( 100L ), "Duration - seconds far above boundary" },
				{ Duration.between( Instant.parse( "2020-01-01T00:00:00Z" ), Instant.parse( "2020-01-01T00:00:00.000000100Z" ) ), "Instant - at boundary (exclusive fails)" },
				{ Duration.between( LocalDateTime.of( 2020, 1, 1, 0, 0 ), LocalDateTime.of( 2020, 1, 1, 0, 0, 0, 100 ) ), "LocalDateTime - at boundary (exclusive fails)" },
				{ Duration.between( ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) ), ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneId.of( "UTC" ) ) ), "ZonedDateTime - at boundary (exclusive fails)" },
				{ Duration.between( OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC ), OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 100, ZoneOffset.UTC ) ), "OffsetDateTime - at boundary (exclusive fails)" }
		};
	}

	@DataProvider(name = "durationMinCombinedUnitsValid")
	private static Object[][] durationMinCombinedUnitsValid() {
		return new Object[][] {
				{ Duration.ofDays( 2 ).plusHours( 3 ).plusMinutes( 30 ).plusSeconds( 10 ).plusMillis( 500 ).plusNanos( 2 ), "Duration - combined units above boundary" },
				{ Duration.ofDays( 1 ).plusHours( 1 ).plusMinutes( 1 ).plusSeconds( 1 ).plusMillis( 1 ).plusNanos( 1 ), "Duration - combined units at boundary (inclusive)" },
				{ null, "null value is always valid" }
		};
	}

	@DataProvider(name = "durationMinCombinedUnitsInvalid")
	private static Object[][] durationMinCombinedUnitsInvalid() {
		return new Object[][] {
				{ Duration.ofDays( 1 ).plusHours( 1 ).plusMinutes( 1 ).plusSeconds( 1 ).plusMillis( 1 ), "Duration - combined units just below boundary" },
				{ Duration.ofDays( 1 ), "Duration - only days, below boundary" },
				{ Duration.ZERO, "Duration - zero" },
				{ Duration.ofDays( -5 ), "Duration - negative" }
		};
	}

	@DataProvider(name = "durationMaxCombinedUnitsValid")
	private static Object[][] durationMaxCombinedUnitsValid() {
		return new Object[][] {
				{ Duration.ofDays( 1 ).plusHours( 1 ).plusMinutes( 1 ).plusSeconds( 1 ).plusMillis( 1 ), "Duration - combined units just below boundary" },
				{ Duration.ofDays( 1 ), "Duration - only days, below boundary" },
				{ Duration.ZERO, "Duration - zero" },
				{ Duration.ofDays( 1 ).plusHours( 1 ).plusMinutes( 1 ).plusSeconds( 1 ).plusMillis( 1 ).plusNanos( 1 ), "Duration - combined units at boundary (inclusive)" },
				{ null, "null value is always valid" }
		};
	}

	@DataProvider(name = "durationMaxCombinedUnitsInvalid")
	private static Object[][] durationMaxCombinedUnitsInvalid() {
		return new Object[][] {
				{ Duration.ofDays( 2 ).plusHours( 3 ).plusMinutes( 30 ).plusSeconds( 10 ).plusMillis( 500 ).plusNanos( 2 ), "Duration - combined units above boundary" },
				{ Duration.ofDays( 1 ).plusHours( 1 ).plusMinutes( 1 ).plusSeconds( 1 ).plusMillis( 1 ).plusNanos( 2 ), "Duration - combined units just above boundary" },
				{ Duration.ofDays( 5 ), "Duration - far above boundary" }
		};
	}

	@DataProvider(name = "durationMinNegativeDurations")
	private static Object[][] durationMinNegativeDurations() {
		return new Object[][] {
				{ Duration.ofNanos( -1L ), "Duration - negative nanos" },
				{ Duration.ofMillis( -1L ), "Duration - negative millis" },
				{ Duration.ofSeconds( -1L ), "Duration - negative seconds" },
				{ Duration.ofMinutes( -1L ), "Duration - negative minutes" },
				{ Duration.ofHours( -1L ), "Duration - negative hours" },
				{ Duration.ofDays( -1L ), "Duration - negative days" },
				{ Duration.ofSeconds( -100L ), "Duration - large negative seconds" },
				{ Duration.ofDays( -365L ), "Duration - negative year-scale" }
		};
	}

	@DataProvider(name = "durationMaxNegativeDurations")
	private static Object[][] durationMaxNegativeDurations() {
		return new Object[][] {
				{ Duration.ofNanos( -1L ), "Duration - negative nanos, valid for max=100ns" },
				{ Duration.ofMillis( -1L ), "Duration - negative millis, valid for max=100ns" },
				{ Duration.ofSeconds( -1L ), "Duration - negative seconds, valid for max=100ns" },
				{ Duration.ofDays( -1L ), "Duration - negative days, valid for max=100ns" },
				{ Duration.ofDays( -365L ), "Duration - negative year-scale, valid for max=100ns" }
		};
	}

	@DataProvider(name = "durationMinZeroBoundary")
	private static Object[][] durationMinZeroBoundary() {
		return new Object[][] {
				{ Duration.ZERO, true, true, "zero at zero boundary inclusive" },
				{ Duration.ZERO, false, false, "zero at zero boundary exclusive" },
				{ Duration.ofNanos( 1L ), true, true, "1ns above zero boundary inclusive" },
				{ Duration.ofNanos( 1L ), false, true, "1ns above zero boundary exclusive" },
				{ Duration.ofNanos( -1L ), true, false, "-1ns below zero boundary inclusive" },
				{ Duration.ofNanos( -1L ), false, false, "-1ns below zero boundary exclusive" },
				{ null, true, true, "null at zero boundary inclusive" },
				{ null, false, true, "null at zero boundary exclusive" }
		};
	}

	@DataProvider(name = "durationMaxZeroBoundary")
	private static Object[][] durationMaxZeroBoundary() {
		return new Object[][] {
				{ Duration.ZERO, true, true, "zero at zero boundary inclusive" },
				{ Duration.ZERO, false, false, "zero at zero boundary exclusive" },
				{ Duration.ofNanos( -1L ), true, true, "-1ns below zero boundary inclusive" },
				{ Duration.ofNanos( -1L ), false, true, "-1ns below zero boundary exclusive" },
				{ Duration.ofNanos( 1L ), true, false, "1ns above zero boundary inclusive" },
				{ Duration.ofNanos( 1L ), false, false, "1ns above zero boundary exclusive" },
				{ null, true, true, "null at zero boundary inclusive" },
				{ null, false, true, "null at zero boundary exclusive" }
		};
	}

	@DataProvider(name = "durationMinLargeValues")
	private static Object[][] durationMinLargeValues() {
		return new Object[][] {
				{ Duration.ofDays( 365L ), true, true, "1 year above 100 days inclusive" },
				{ Duration.ofDays( 100L ), true, true, "100 days at boundary inclusive" },
				{ Duration.ofDays( 100L ), false, false, "100 days at boundary exclusive" },
				{ Duration.ofDays( 99L ), true, false, "99 days below boundary inclusive" },
				{ Duration.ofDays( 99L ), false, false, "99 days below boundary exclusive" }
		};
	}

	@DataProvider(name = "durationMaxLargeValues")
	private static Object[][] durationMaxLargeValues() {
		return new Object[][] {
				{ Duration.ofDays( 50L ), true, true, "50 days below 100 days inclusive" },
				{ Duration.ofDays( 100L ), true, true, "100 days at boundary inclusive" },
				{ Duration.ofDays( 100L ), false, false, "100 days at boundary exclusive" },
				{ Duration.ofDays( 101L ), true, false, "101 days above boundary inclusive" },
				{ Duration.ofDays( 101L ), false, false, "101 days above boundary exclusive" }
		};
	}

	@DataProvider(name = "durationMinChronoUnitDurations")
	private static Object[][] durationMinChronoUnitDurations() {
		return new Object[][] {
				{ ChronoUnit.NANOS.getDuration().multipliedBy( 1000 ), true, true, "1000 nanos via ChronoUnit inclusive" },
				{ ChronoUnit.NANOS.getDuration().multipliedBy( 100 ), true, true, "100 nanos via ChronoUnit at boundary inclusive" },
				{ ChronoUnit.NANOS.getDuration().multipliedBy( 100 ), false, false, "100 nanos via ChronoUnit at boundary exclusive" },
				{ ChronoUnit.MICROS.getDuration().multipliedBy( 1 ), true, true, "1 micro via ChronoUnit inclusive" },
				{ ChronoUnit.MILLIS.getDuration().multipliedBy( 1 ), true, true, "1 milli via ChronoUnit inclusive" },
				{ ChronoUnit.SECONDS.getDuration().multipliedBy( 1 ), true, true, "1 second via ChronoUnit inclusive" },
				{ ChronoUnit.MINUTES.getDuration().multipliedBy( 1 ), true, true, "1 minute via ChronoUnit inclusive" },
				{ ChronoUnit.HOURS.getDuration().multipliedBy( 1 ), true, true, "1 hour via ChronoUnit inclusive" },
				{ ChronoUnit.DAYS.getDuration().multipliedBy( 1 ), true, true, "1 day via ChronoUnit inclusive" }
		};
	}

	@DataProvider(name = "durationMaxChronoUnitDurations")
	private static Object[][] durationMaxChronoUnitDurations() {
		return new Object[][] {
				{ ChronoUnit.NANOS.getDuration().multipliedBy( 10 ), true, true, "10 nanos via ChronoUnit inclusive" },
				{ ChronoUnit.NANOS.getDuration().multipliedBy( 100 ), true, true, "100 nanos via ChronoUnit at boundary inclusive" },
				{ ChronoUnit.NANOS.getDuration().multipliedBy( 100 ), false, false, "100 nanos via ChronoUnit at boundary exclusive" },
				{ ChronoUnit.MICROS.getDuration().multipliedBy( 1 ), true, false, "1 micro via ChronoUnit above boundary inclusive" },
				{ ChronoUnit.MILLIS.getDuration().multipliedBy( 1 ), true, false, "1 milli via ChronoUnit above boundary inclusive" },
				{ ChronoUnit.SECONDS.getDuration().multipliedBy( 1 ), true, false, "1 second via ChronoUnit above boundary inclusive" }
		};
	}

	@Test(dataProvider = "durationMinInclusiveValidDurations")
	public void durationMinInclusive_valid(Duration value, String description) {
		DurationMinValidator validator = createDurationMinValidator( 100L, true );
		assertTrue( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMinInclusiveInvalidDurations")
	public void durationMinInclusive_invalid(Duration value, String description) {
		DurationMinValidator validator = createDurationMinValidator( 100L, true );
		assertFalse( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMinExclusiveValidDurations")
	public void durationMinExclusive_valid(Duration value, String description) {
		DurationMinValidator validator = createDurationMinValidator( 100L, false );
		assertTrue( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMinExclusiveInvalidDurations")
	public void durationMinExclusive_invalid(Duration value, String description) {
		DurationMinValidator validator = createDurationMinValidator( 100L, false );
		assertFalse( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMaxInclusiveValidDurations")
	public void durationMaxInclusive_valid(Duration value, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 100L, true );
		assertTrue( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMaxInclusiveInvalidDurations")
	public void durationMaxInclusive_invalid(Duration value, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 100L, true );
		assertFalse( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMaxExclusiveValidDurations")
	public void durationMaxExclusive_valid(Duration value, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 100L, false );
		assertTrue( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMaxExclusiveInvalidDurations")
	public void durationMaxExclusive_invalid(Duration value, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 100L, false );
		assertFalse( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMinCombinedUnitsValid")
	public void durationMinCombinedUnits_valid(Duration value, String description) {
		DurationMinValidator validator = createDurationMinValidator( 1, 1, 1, 1, 1, 1, true );
		assertTrue( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMinCombinedUnitsInvalid")
	public void durationMinCombinedUnits_invalid(Duration value, String description) {
		DurationMinValidator validator = createDurationMinValidator( 1, 1, 1, 1, 1, 1, true );
		assertFalse( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMaxCombinedUnitsValid")
	public void durationMaxCombinedUnits_valid(Duration value, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 1, 1, 1, 1, 1, 1, true );
		assertTrue( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMaxCombinedUnitsInvalid")
	public void durationMaxCombinedUnits_invalid(Duration value, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 1, 1, 1, 1, 1, 1, true );
		assertFalse( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMinNegativeDurations")
	public void durationMin_negativeDurationsAreInvalid(Duration value, String description) {
		DurationMinValidator validator = createDurationMinValidator( 100L, true );
		assertFalse( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMaxNegativeDurations")
	public void durationMax_negativeDurationsAreValid(Duration value, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 100L, true );
		assertTrue( validator.isValid( value, null ), description );
	}

	@Test(dataProvider = "durationMinZeroBoundary")
	public void durationMin_zeroBoundary(Duration value, boolean inclusive, boolean expectedValid, String description) {
		DurationMinValidator validator = createDurationMinValidator( 0L, inclusive );
		assertEquals( validator.isValid( value, null ), expectedValid, description );
	}

	@Test(dataProvider = "durationMaxZeroBoundary")
	public void durationMax_zeroBoundary(Duration value, boolean inclusive, boolean expectedValid, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 0L, inclusive );
		assertEquals( validator.isValid( value, null ), expectedValid, description );
	}

	@Test(dataProvider = "durationMinLargeValues")
	public void durationMin_largeValues(Duration value, boolean inclusive, boolean expectedValid, String description) {
		DurationMinValidator validator = createDurationMinValidator( 100, 0, 0, 0, 0, 0, inclusive );
		assertEquals( validator.isValid( value, null ), expectedValid, description );
	}

	@Test(dataProvider = "durationMaxLargeValues")
	public void durationMax_largeValues(Duration value, boolean inclusive, boolean expectedValid, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 100, 0, 0, 0, 0, 0, inclusive );
		assertEquals( validator.isValid( value, null ), expectedValid, description );
	}

	@Test(dataProvider = "durationMinChronoUnitDurations")
	public void durationMin_chronoUnitDurations(Duration value, boolean inclusive, boolean expectedValid, String description) {
		DurationMinValidator validator = createDurationMinValidator( 100L, inclusive );
		assertEquals( validator.isValid( value, null ), expectedValid, description );
	}

	@Test(dataProvider = "durationMaxChronoUnitDurations")
	public void durationMax_chronoUnitDurations(Duration value, boolean inclusive, boolean expectedValid, String description) {
		DurationMaxValidator validator = createDurationMaxValidator( 100L, inclusive );
		assertEquals( validator.isValid( value, null ), expectedValid, description );
	}

	@Test
	public void durationMin_fullValidation_withAnnotatedClass() {
		Validator validator = getConfiguration( HibernateValidator.class ).buildValidatorFactory().getValidator();

		assertNoViolations( validator.validate( new TaskWithDurationMin( null ) ) );
		assertNoViolations( validator.validate( new TaskWithDurationMin( Duration.ofSeconds( 11 ) ) ) );
		assertThat( validator.validate( new TaskWithDurationMin( Duration.ofSeconds( 9 ) ) ) )
				.containsOnlyViolations( violationOf( DurationMin.class ) );
		assertNoViolations( validator.validate( new TaskWithDurationMin( Duration.ofSeconds( 10 ) ) ) );
		assertThat( validator.validate( new TaskWithDurationMin( Duration.ZERO ) ) )
				.containsOnlyViolations( violationOf( DurationMin.class ) );
		assertThat( validator.validate( new TaskWithDurationMin( Duration.ofSeconds( -1 ) ) ) )
				.containsOnlyViolations( violationOf( DurationMin.class ) );
	}

	@Test
	public void durationMax_fullValidation_withAnnotatedClass() {
		Validator validator = getConfiguration( HibernateValidator.class ).buildValidatorFactory().getValidator();

		assertNoViolations( validator.validate( new TaskWithDurationMax( null ) ) );
		assertNoViolations( validator.validate( new TaskWithDurationMax( Duration.ofSeconds( 1 ) ) ) );
		assertThat( validator.validate( new TaskWithDurationMax( Duration.ofSeconds( 11 ) ) ) )
				.containsOnlyViolations( violationOf( DurationMax.class ) );
		assertNoViolations( validator.validate( new TaskWithDurationMax( Duration.ofSeconds( 10 ) ) ) );
		assertNoViolations( validator.validate( new TaskWithDurationMax( Duration.ZERO ) ) );
		assertNoViolations( validator.validate( new TaskWithDurationMax( Duration.ofSeconds( -1 ) ) ) );
	}

	@Test
	public void durationMin_programmaticConstraint() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMinDef()
						.days( 1 ).hours( 1 )
						.minutes( 1 ).seconds( 1 )
						.millis( 1 ).nanos( 1 ).inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( 1 ) ) ) )
				.containsOnlyViolations( violationOf( DurationMin.class ) );
		assertNoViolations( validator.validate( new PlainTask( Duration.ofDays( 2 ) ) ) );
		assertNoViolations( validator.validate( new PlainTask( null ) ) );
	}

	@Test
	public void durationMax_programmaticConstraint() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMaxDef()
						.days( 1 ).hours( 1 )
						.minutes( 1 ).seconds( 1 )
						.millis( 1 ).nanos( 1 ).inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( 2 ) ) ) )
				.containsOnlyViolations( violationOf( DurationMax.class ) );
		assertNoViolations( validator.validate( new PlainTask( Duration.ofDays( 1 ) ) ) );
		assertNoViolations( validator.validate( new PlainTask( null ) ) );
	}

	@Test
	public void durationMin_message_inclusive() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMinDef()
						.days( 30 ).hours( 12 ).minutes( 50 )
						.inclusive( true )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( 2 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMin.class ).withMessage( "must be longer than or equal to 30 days 12 hours 50 minutes" )
				);
	}

	@Test
	public void durationMin_message_exclusive() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMinDef()
						.days( 30 ).hours( 12 ).minutes( 50 )
						.inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( 2 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMin.class ).withMessage( "must be longer than 30 days 12 hours 50 minutes" )
				);
	}

	@Test
	public void durationMax_message_inclusive() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMaxDef()
						.days( 1 ).nanos( 100 )
						.inclusive( true )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( 2 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMax.class ).withMessage( "must be shorter than or equal to 1 day 100 nanos" )
				);
	}

	@Test
	public void durationMax_message_exclusive() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMaxDef()
						.days( 1 ).nanos( 100 )
						.inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( 2 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMax.class ).withMessage( "must be shorter than 1 day 100 nanos" )
				);
	}

	@Test
	public void durationMin_message_zeroBoundary() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMinDef()
						.inclusive( true )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( -2 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMin.class ).withMessage( "must be longer than or equal to 0" )
				);
	}

	@Test
	public void durationMax_message_zeroBoundary() {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class, Locale.ENGLISH );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( PlainTask.class )
				.field( "duration" )
				.constraint( new DurationMaxDef()
						.inclusive( false )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		assertThat( validator.validate( new PlainTask( Duration.ofDays( 2 ) ) ) )
				.containsOnlyViolations(
						violationOf( DurationMax.class ).withMessage( "must be shorter than 0" )
				);
	}

	@Test
	public void durationMin_instantDerivedDurations() {
		DurationMinValidator validator = createDurationMinValidator( 0, 0, 0, 10, 0, 0, true );

		Instant start = Instant.parse( "2020-01-01T00:00:00Z" );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void durationMin_localDateTimeDerivedDurations() {
		DurationMinValidator validator = createDurationMinValidator( 0, 0, 0, 10, 0, 0, true );

		LocalDateTime start = LocalDateTime.of( 2020, 1, 1, 0, 0 );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
	}

	@Test
	public void durationMin_zonedDateTimeDerivedDurations() {
		DurationMinValidator validator = createDurationMinValidator( 0, 0, 0, 10, 0, 0, true );

		ZonedDateTime start = ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
	}

	@Test
	public void durationMin_offsetDateTimeDerivedDurations() {
		DurationMinValidator validator = createDurationMinValidator( 0, 0, 0, 10, 0, 0, true );

		OffsetDateTime start = OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
	}

	@Test
	public void durationMax_instantDerivedDurations() {
		DurationMaxValidator validator = createDurationMaxValidator( 0, 0, 0, 10, 0, 0, true );

		Instant start = Instant.parse( "2020-01-01T00:00:00Z" );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void durationMax_localDateTimeDerivedDurations() {
		DurationMaxValidator validator = createDurationMaxValidator( 0, 0, 0, 10, 0, 0, true );

		LocalDateTime start = LocalDateTime.of( 2020, 1, 1, 0, 0 );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
	}

	@Test
	public void durationMax_zonedDateTimeDerivedDurations() {
		DurationMaxValidator validator = createDurationMaxValidator( 0, 0, 0, 10, 0, 0, true );

		ZonedDateTime start = ZonedDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneId.of( "UTC" ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
	}

	@Test
	public void durationMax_offsetDateTimeDerivedDurations() {
		DurationMaxValidator validator = createDurationMaxValidator( 0, 0, 0, 10, 0, 0, true );

		OffsetDateTime start = OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 10 ) ), null ) );
		assertTrue( validator.isValid( Duration.between( start, start.plusSeconds( 9 ) ), null ) );
		assertFalse( validator.isValid( Duration.between( start, start.plusSeconds( 11 ) ), null ) );
	}

	@Test
	public void durationMin_zonedDateTime_crossZoneDurations() {
		DurationMinValidator validator = createDurationMinValidator( 0, 1, 0, 0, 0, 0, true );

		ZonedDateTime paris = ZonedDateTime.of( 2020, 6, 1, 12, 0, 0, 0, ZoneId.of( "Europe/Paris" ) );
		ZonedDateTime tokyo = ZonedDateTime.of( 2020, 6, 1, 19, 0, 0, 0, ZoneId.of( "Asia/Tokyo" ) );
		Duration crossZone = Duration.between( paris, tokyo );
		assertTrue( validator.isValid( crossZone, null ), "Cross-zone duration should be valid" );
	}

	@Test
	public void durationMax_zonedDateTime_crossZoneDurations() {
		DurationMaxValidator validator = createDurationMaxValidator( 0, 1, 0, 0, 0, 0, true );

		ZonedDateTime paris = ZonedDateTime.of( 2020, 6, 1, 12, 0, 0, 0, ZoneId.of( "Europe/Paris" ) );
		ZonedDateTime tokyo = ZonedDateTime.of( 2020, 6, 1, 19, 0, 0, 0, ZoneId.of( "Asia/Tokyo" ) );
		Duration crossZone = Duration.between( paris, tokyo );
		assertFalse( validator.isValid( crossZone, null ), "Cross-zone duration should exceed max" );
	}

	@Test
	public void durationMin_offsetDateTime_differentOffsets() {
		DurationMinValidator validator = createDurationMinValidator( 0, 0, 0, 3600, 0, 0, true );

		OffsetDateTime utc = OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC );
		OffsetDateTime plusOne = OffsetDateTime.of( 2020, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours( 1 ) );
		Duration betweenOffsets = Duration.between( utc, plusOne );
		assertTrue( validator.isValid( betweenOffsets, null ), "Duration between different offsets should be valid" );
	}

	@Test
	public void durationMax_offsetDateTime_differentOffsets() {
		DurationMaxValidator validator = createDurationMaxValidator( 0, 0, 0, 3600, 0, 0, true );

		OffsetDateTime utc = OffsetDateTime.of( 2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC );
		OffsetDateTime plusOne = OffsetDateTime.of( 2020, 1, 1, 2, 0, 0, 0, ZoneOffset.ofHours( 1 ) );
		Duration betweenOffsets = Duration.between( utc, plusOne );
		assertTrue( validator.isValid( betweenOffsets, null ), "Duration between offsets should be within max" );
	}

	private static DurationMinValidator createDurationMinValidator(long nanos, boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DurationMin.class );
		descriptorBuilder.setAttribute( "nanos", nanos );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		DurationMin annotation = descriptorBuilder.build().getAnnotation();
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );
		return validator;
	}

	private static DurationMinValidator createDurationMinValidator(long days, long hours, long minutes, long seconds, long millis, long nanos, boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DurationMin.class );
		descriptorBuilder.setAttribute( "days", days );
		descriptorBuilder.setAttribute( "hours", hours );
		descriptorBuilder.setAttribute( "minutes", minutes );
		descriptorBuilder.setAttribute( "seconds", seconds );
		descriptorBuilder.setAttribute( "millis", millis );
		descriptorBuilder.setAttribute( "nanos", nanos );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		DurationMin annotation = descriptorBuilder.build().getAnnotation();
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );
		return validator;
	}

	private static DurationMaxValidator createDurationMaxValidator(long nanos, boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DurationMax.class );
		descriptorBuilder.setAttribute( "nanos", nanos );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		DurationMax annotation = descriptorBuilder.build().getAnnotation();
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );
		return validator;
	}

	private static DurationMaxValidator createDurationMaxValidator(long days, long hours, long minutes, long seconds, long millis, long nanos, boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DurationMax.class );
		descriptorBuilder.setAttribute( "days", days );
		descriptorBuilder.setAttribute( "hours", hours );
		descriptorBuilder.setAttribute( "minutes", minutes );
		descriptorBuilder.setAttribute( "seconds", seconds );
		descriptorBuilder.setAttribute( "millis", millis );
		descriptorBuilder.setAttribute( "nanos", nanos );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		DurationMax annotation = descriptorBuilder.build().getAnnotation();
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );
		return validator;
	}

	private static class TaskWithDurationMin {

		@DurationMin(seconds = 10)
		private final Duration duration;

		public TaskWithDurationMin(Duration duration) {
			this.duration = duration;
		}
	}

	private static class TaskWithDurationMax {

		@DurationMax(seconds = 10)
		private final Duration duration;

		public TaskWithDurationMax(Duration duration) {
			this.duration = duration;
		}
	}

	private static class PlainTask {

		private final Duration duration;

		public PlainTask(Duration duration) {
			this.duration = duration;
		}
	}
}
