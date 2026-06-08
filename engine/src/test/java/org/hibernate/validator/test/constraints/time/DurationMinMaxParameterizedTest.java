/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.time.Duration;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Parameterized test for {@link DurationMin} and {@link DurationMax} annotations.
 *
 * @author [Your Name]
 */
public class DurationMinMaxParameterizedTest extends AbstractConstrainedTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = ValidatorUtil.getValidator();
	}

	// ==========================================
	// DurationMin tests
	// ==========================================

	@Test(dataProvider = "durationMinInclusiveData")
	public void testDurationMinInclusive(Duration duration, boolean expectedValid) {
		FooMinInclusive foo = new FooMinInclusive( duration );
		Set<ConstraintViolation<FooMinInclusive>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class ).withProperty( "duration" )
			);
		}
	}

	@Test(dataProvider = "durationMinExclusiveData")
	public void testDurationMinExclusive(Duration duration, boolean expectedValid) {
		FooMinExclusive foo = new FooMinExclusive( duration );
		Set<ConstraintViolation<FooMinExclusive>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class ).withProperty( "duration" )
			);
		}
	}

	@Test(dataProvider = "durationMinNegativeData")
	public void testDurationMinNegative(Duration duration, boolean expectedValid) {
		FooMinNegative foo = new FooMinNegative( duration );
		Set<ConstraintViolation<FooMinNegative>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class ).withProperty( "duration" )
			);
		}
	}

	@Test(dataProvider = "durationMinZeroData")
	public void testDurationMinZero(Duration duration, boolean expectedValid) {
		FooMinZero foo = new FooMinZero( duration );
		Set<ConstraintViolation<FooMinZero>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class ).withProperty( "duration" )
			);
		}
	}

	// ==========================================
	// DurationMax tests
	// ==========================================

	@Test(dataProvider = "durationMaxInclusiveData")
	public void testDurationMaxInclusive(Duration duration, boolean expectedValid) {
		FooMaxInclusive foo = new FooMaxInclusive( duration );
		Set<ConstraintViolation<FooMaxInclusive>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMax.class ).withProperty( "duration" )
			);
		}
	}

	@Test(dataProvider = "durationMaxExclusiveData")
	public void testDurationMaxExclusive(Duration duration, boolean expectedValid) {
		FooMaxExclusive foo = new FooMaxExclusive( duration );
		Set<ConstraintViolation<FooMaxExclusive>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMax.class ).withProperty( "duration" )
			);
		}
	}

	@Test(dataProvider = "durationMaxNegativeData")
	public void testDurationMaxNegative(Duration duration, boolean expectedValid) {
		FooMaxNegative foo = new FooMaxNegative( duration );
		Set<ConstraintViolation<FooMaxNegative>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMax.class ).withProperty( "duration" )
			);
		}
	}

	@Test(dataProvider = "durationMaxZeroData")
	public void testDurationMaxZero(Duration duration, boolean expectedValid) {
		FooMaxZero foo = new FooMaxZero( duration );
		Set<ConstraintViolation<FooMaxZero>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMax.class ).withProperty( "duration" )
			);
		}
	}

	// ==========================================
	// Combined tests
	// ==========================================

	@Test(dataProvider = "durationMinMaxCombinedData")
	public void testDurationMinMaxCombined(Duration duration, boolean expectedValid) {
		FooMinMaxCombined foo = new FooMinMaxCombined( duration );
		Set<ConstraintViolation<FooMinMaxCombined>> violations = validator.validate( foo );
		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).isNotEmpty();
		}
	}

	// ==========================================
	// Data Providers
	// ==========================================

	@DataProvider(name = "durationMinInclusiveData")
	private Object[][] durationMinInclusiveData() {
		return new Object[][] {
				// null is always valid
				{ null, true },
				// exactly at boundary (inclusive)
				{ Duration.ofHours( 2 ), true },
				// above boundary
				{ Duration.ofHours( 3 ), true },
				// just above boundary
				{ Duration.ofHours( 2 ).plusNanos( 1 ), true },
				// below boundary
				{ Duration.ofHours( 1 ), false },
				// just below boundary
				{ Duration.ofHours( 2 ).minusNanos( 1 ), false },
				// negative value
				{ Duration.ofHours( -2 ), false }
		};
	}

	@DataProvider(name = "durationMinExclusiveData")
	private Object[][] durationMinExclusiveData() {
		return new Object[][] {
				{ null, true },
				// exactly at boundary (exclusive)
				{ Duration.ofHours( 2 ), false },
				// above boundary
				{ Duration.ofHours( 3 ), true },
				// just above boundary
				{ Duration.ofHours( 2 ).plusNanos( 1 ), true },
				// below boundary
				{ Duration.ofHours( 1 ), false },
				// just below boundary
				{ Duration.ofHours( 2 ).minusNanos( 1 ), false },
				// negative value
				{ Duration.ofHours( -2 ), false }
		};
	}

	@DataProvider(name = "durationMinNegativeData")
	private Object[][] durationMinNegativeData() {
		return new Object[][] {
				{ null, true },
				// exactly at boundary (inclusive)
				{ Duration.ofHours( -2 ), true },
				// less negative (higher)
				{ Duration.ofHours( -1 ), true },
				// more negative (lower)
				{ Duration.ofHours( -3 ), false },
				// just less negative
				{ Duration.ofHours( -2 ).plusNanos( 1 ), true },
				// just more negative
				{ Duration.ofHours( -2 ).minusNanos( 1 ), false },
				// positive value
				{ Duration.ofHours( 2 ), true }
		};
	}

	@DataProvider(name = "durationMinZeroData")
	private Object[][] durationMinZeroData() {
		return new Object[][] {
				{ null, true },
				// zero (inclusive)
				{ Duration.ZERO, true },
				// positive
				{ Duration.ofNanos( 1 ), true },
				// negative
				{ Duration.ofNanos( -1 ), false }
		};
	}

	@DataProvider(name = "durationMaxInclusiveData")
	private Object[][] durationMaxInclusiveData() {
		return new Object[][] {
				{ null, true },
				// exactly at boundary (inclusive)
				{ Duration.ofHours( 2 ), true },
				// below boundary
				{ Duration.ofHours( 1 ), true },
				// just below boundary
				{ Duration.ofHours( 2 ).minusNanos( 1 ), true },
				// above boundary
				{ Duration.ofHours( 3 ), false },
				// just above boundary
				{ Duration.ofHours( 2 ).plusNanos( 1 ), false },
				// negative value
				{ Duration.ofHours( -2 ), true }
		};
	}

	@DataProvider(name = "durationMaxExclusiveData")
	private Object[][] durationMaxExclusiveData() {
		return new Object[][] {
				{ null, true },
				// exactly at boundary (exclusive)
				{ Duration.ofHours( 2 ), false },
				// below boundary
				{ Duration.ofHours( 1 ), true },
				// just below boundary
				{ Duration.ofHours( 2 ).minusNanos( 1 ), true },
				// above boundary
				{ Duration.ofHours( 3 ), false },
				// just above boundary
				{ Duration.ofHours( 2 ).plusNanos( 1 ), false },
				// negative value
				{ Duration.ofHours( -2 ), true }
		};
	}

	@DataProvider(name = "durationMaxNegativeData")
	private Object[][] durationMaxNegativeData() {
		return new Object[][] {
				{ null, true },
				// exactly at boundary (inclusive)
				{ Duration.ofHours( -2 ), true },
				// more negative (lower)
				{ Duration.ofHours( -3 ), true },
				// less negative (higher)
				{ Duration.ofHours( -1 ), false },
				// just more negative
				{ Duration.ofHours( -2 ).minusNanos( 1 ), true },
				// just less negative
				{ Duration.ofHours( -2 ).plusNanos( 1 ), false },
				// positive value
				{ Duration.ofHours( 2 ), false }
		};
	}

	@DataProvider(name = "durationMaxZeroData")
	private Object[][] durationMaxZeroData() {
		return new Object[][] {
				{ null, true },
				// zero (inclusive)
				{ Duration.ZERO, true },
				// negative
				{ Duration.ofNanos( -1 ), true },
				// positive
				{ Duration.ofNanos( 1 ), false }
		};
	}

	@DataProvider(name = "durationMinMaxCombinedData")
	private Object[][] durationMinMaxCombinedData() {
		return new Object[][] {
				{ null, true },
				// within range
				{ Duration.ofHours( 1 ).plusMinutes( 30 ), true },
				// at lower bound inclusive
				{ Duration.ofHours( 1 ), true },
				// at upper bound inclusive
				{ Duration.ofHours( 2 ), true },
				// below lower bound
				{ Duration.ofMinutes( 30 ), false },
				// above upper bound
				{ Duration.ofHours( 3 ), false },
				// just above lower bound
				{ Duration.ofHours( 1 ).plusNanos( 1 ), true },
				// just below upper bound
				{ Duration.ofHours( 2 ).minusNanos( 1 ), true }
		};
	}

	// ==========================================
	// Test entities
	// ==========================================

	private static class FooMinInclusive {
		@DurationMin(hours = 2, inclusive = true)
		private final Duration duration;

		FooMinInclusive(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMinExclusive {
		@DurationMin(hours = 2, inclusive = false)
		private final Duration duration;

		FooMinExclusive(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMinNegative {
		@DurationMin(hours = -2)
		private final Duration duration;

		FooMinNegative(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMinZero {
		@DurationMin
		private final Duration duration;

		FooMinZero(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMaxInclusive {
		@DurationMax(hours = 2, inclusive = true)
		private final Duration duration;

		FooMaxInclusive(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMaxExclusive {
		@DurationMax(hours = 2, inclusive = false)
		private final Duration duration;

		FooMaxExclusive(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMaxNegative {
		@DurationMax(hours = -2)
		private final Duration duration;

		FooMaxNegative(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMaxZero {
		@DurationMax
		private final Duration duration;

		FooMaxZero(Duration duration) {
			this.duration = duration;
		}
	}

	private static class FooMinMaxCombined {
		@DurationMin(hours = 1)
		@DurationMax(hours = 2)
		private final Duration duration;

		FooMinMaxCombined(Duration duration) {
			this.duration = duration;
		}
	}
}
