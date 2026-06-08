/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.time.Duration;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.time.DurationMinValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Parameterized tests for {@link DurationMin} and {@link DurationMax} constraint annotations.
 * <p>
 * Covers boundary values, null values, positive and negative values,
 * inclusive and exclusive boundary settings, and combinations of time units.
 *
 * @author Marko Bekhta
 */
public class DurationMinMaxParameterizedTest {

	private static final Duration ZERO = Duration.ZERO;
	private static final Duration ONE_NANO = Duration.ofNanos( 1 );
	private static final Duration NEGATIVE_ONE_NANO = Duration.ofNanos( -1 );
	private static final Duration HUNDRED_NANOS = Duration.ofNanos( 100 );
	private static final Duration ONE_SECOND = Duration.ofSeconds( 1 );
	private static final Duration TEN_SECONDS = Duration.ofSeconds( 10 );
	private static final Duration ONE_MINUTE = Duration.ofMinutes( 1 );
	private static final Duration ONE_HOUR = Duration.ofHours( 1 );
	private static final Duration ONE_DAY = Duration.ofDays( 1 );
	private static final Duration NEGATIVE_ONE_DAY = Duration.ofDays( -1 );

	// ==============================================
	// DurationMin - Validator-level parameterized tests
	// ==============================================

	@Test(dataProvider = "durationMinValidData")
	public void testDurationMinValid(long nanos, boolean inclusive, Duration value) {
		DurationMin annotation = buildDurationMinAnnotation( nanos, inclusive );
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );
		Assert.assertTrue( validator.isValid( value, null ),
				String.format( "Expected valid: nanos=%d, inclusive=%s, value=%s", nanos, inclusive, value ) );
	}

	@DataProvider(name = "durationMinValidData")
	public static Object[][] durationMinValidData() {
		return new Object[][] {
				{ 100L, true, null },
				{ 100L, false, null },
				{ 0L, true, ZERO },
				{ 0L, true, ONE_NANO },
				{ 0L, false, ONE_NANO },
				{ 100L, true, HUNDRED_NANOS },
				{ 100L, true, Duration.ofNanos( 1000L ) },
				{ 100L, false, Duration.ofNanos( 101L ) },
				{ 0L, true, Duration.ofSeconds( 1 ) },
				{ 0L, true, ONE_MINUTE },
				{ 0L, true, ONE_HOUR },
				{ 0L, true, ONE_DAY },
				{ -100L, true, ZERO },
				{ -100L, true, ONE_NANO },
				{ -100L, true, HUNDRED_NANOS },
				{ -100L, false, ZERO },
				{ -100L, false, ONE_NANO },
		};
	}

	@Test(dataProvider = "durationMinInvalidData")
	public void testDurationMinInvalid(long nanos, boolean inclusive, Duration value) {
		DurationMin annotation = buildDurationMinAnnotation( nanos, inclusive );
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );
		Assert.assertFalse( validator.isValid( value, null ),
				String.format( "Expected invalid: nanos=%d, inclusive=%s, value=%s", nanos, inclusive, value ) );
	}

	@DataProvider(name = "durationMinInvalidData")
	public static Object[][] durationMinInvalidData() {
		return new Object[][] {
				{ 100L, true, Duration.ofNanos( 99L ) },
				{ 100L, false, HUNDRED_NANOS },
				{ 100L, false, Duration.ofNanos( 10L ) },
				{ 100L, true, ZERO },
				{ 100L, true, Duration.ofNanos( -1L ) },
				{ 0L, false, ZERO },
				{ 0L, false, NEGATIVE_ONE_NANO },
				{ 0L, true, NEGATIVE_ONE_NANO },
				{ 0L, true, NEGATIVE_ONE_DAY },
				{ 10L, true, Duration.ofNanos( 9L ) },
		};
	}

	// ==============================================
	// DurationMax - Validator-level parameterized tests
	// ==============================================

	@Test(dataProvider = "durationMaxValidData")
	public void testDurationMaxValid(long nanos, boolean inclusive, Duration value) {
		DurationMax annotation = buildDurationMaxAnnotation( nanos, inclusive );
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );
		Assert.assertTrue( validator.isValid( value, null ),
				String.format( "Expected valid: nanos=%d, inclusive=%s, value=%s", nanos, inclusive, value ) );
	}

	@DataProvider(name = "durationMaxValidData")
	public static Object[][] durationMaxValidData() {
		return new Object[][] {
				{ 100L, true, null },
				{ 100L, false, null },
				{ 0L, true, ZERO },
				{ 0L, true, NEGATIVE_ONE_NANO },
				{ 0L, false, NEGATIVE_ONE_NANO },
				{ 100L, true, HUNDRED_NANOS },
				{ 100L, true, Duration.ofNanos( 10L ) },
				{ 100L, false, Duration.ofNanos( 99L ) },
				{ 100L, true, ZERO },
				{ 0L, true, Duration.ofDays( -1L ) },
				{ -100L, true, Duration.ofNanos( -200L ) },
				{ -100L, false, Duration.ofNanos( -101L ) },
		};
	}

	@Test(dataProvider = "durationMaxInvalidData")
	public void testDurationMaxInvalid(long nanos, boolean inclusive, Duration value) {
		DurationMax annotation = buildDurationMaxAnnotation( nanos, inclusive );
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );
		Assert.assertFalse( validator.isValid( value, null ),
				String.format( "Expected invalid: nanos=%d, inclusive=%s, value=%s", nanos, inclusive, value ) );
	}

	@DataProvider(name = "durationMaxInvalidData")
	public static Object[][] durationMaxInvalidData() {
		return new Object[][] {
				{ 100L, true, Duration.ofNanos( 101L ) },
				{ 100L, false, HUNDRED_NANOS },
				{ 100L, false, Duration.ofNanos( 101L ) },
				{ 100L, true, Duration.ofSeconds( 1 ) },
				{ 100L, true, ONE_DAY },
				{ 0L, true, ONE_NANO },
				{ 0L, false, ZERO },
				{ 0L, false, ONE_NANO },
				{ -100L, true, ZERO },
				{ -100L, true, ONE_NANO },
		};
	}

	// ==============================================
	// DurationMin - Boundary value parameterized tests
	// ==============================================

	@Test(dataProvider = "durationMinBoundaryData")
	public void testDurationMinBoundary(long days, long hours, long minutes, long seconds, long millis, long nanos,
			boolean inclusive, Duration value, boolean expectedValid) {
		DurationMin annotation = buildDurationMinAnnotation( days, hours, minutes, seconds, millis, nanos, inclusive );
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );
		if ( expectedValid ) {
			Assert.assertTrue( validator.isValid( value, null ),
					String.format( "Expected valid at boundary: days=%d, inclusive=%s, value=%s", days, inclusive, value ) );
		}
		else {
			Assert.assertFalse( validator.isValid( value, null ),
					String.format( "Expected invalid at boundary: days=%d, inclusive=%s, value=%s", days, inclusive, value ) );
		}
	}

	@DataProvider(name = "durationMinBoundaryData")
	public static Object[][] durationMinBoundaryData() {
		return new Object[][] {
				{ 1L, 0L, 0L, 0L, 0L, 0L, true, ONE_DAY, true },
				{ 1L, 0L, 0L, 0L, 0L, 0L, false, ONE_DAY, false },
				{ 1L, 0L, 0L, 0L, 0L, 0L, false, ONE_DAY.plus( ONE_NANO ), true },
				{ 1L, 0L, 0L, 0L, 0L, 0L, true, ONE_DAY.minus( ONE_NANO ), false },
				{ 0L, 1L, 0L, 0L, 0L, 0L, true, ONE_HOUR, true },
				{ 0L, 1L, 0L, 0L, 0L, 0L, false, ONE_HOUR, false },
				{ 0L, 0L, 1L, 0L, 0L, 0L, true, ONE_MINUTE, true },
				{ 0L, 0L, 1L, 0L, 0L, 0L, false, ONE_MINUTE, false },
				{ 0L, 0L, 0L, 1L, 0L, 0L, true, ONE_SECOND, true },
				{ 0L, 0L, 0L, 1L, 0L, 0L, false, ONE_SECOND, false },
				{ 0L, 0L, 0L, 0L, 1L, 0L, true, Duration.ofMillis( 1 ), true },
				{ 0L, 0L, 0L, 0L, 1L, 0L, false, Duration.ofMillis( 1 ), false },
				{ 0L, 0L, 0L, 0L, 0L, 1L, true, ONE_NANO, true },
				{ 0L, 0L, 0L, 0L, 0L, 1L, false, ONE_NANO, false },
				{ 0L, 0L, 0L, 0L, 0L, 0L, true, ZERO, true },
				{ 0L, 0L, 0L, 0L, 0L, 0L, false, ZERO, false },
				{ 0L, 0L, 0L, 0L, 0L, 0L, false, ONE_NANO, true },
				{ 0L, 0L, 0L, 0L, 0L, 0L, true, NEGATIVE_ONE_NANO, false },
				{ 1L, 2L, 3L, 4L, 5L, 6L, true, Duration.ofDays( 1 ).plusHours( 2 ).plusMinutes( 3 ).plusSeconds( 4 ).plusMillis( 5 ).plusNanos( 6 ), true },
				{ 1L, 2L, 3L, 4L, 5L, 6L, false, Duration.ofDays( 1 ).plusHours( 2 ).plusMinutes( 3 ).plusSeconds( 4 ).plusMillis( 5 ).plusNanos( 6 ), false },
		};
	}

	// ==============================================
	// DurationMax - Boundary value parameterized tests
	// ==============================================

	@Test(dataProvider = "durationMaxBoundaryData")
	public void testDurationMaxBoundary(long days, long hours, long minutes, long seconds, long millis, long nanos,
			boolean inclusive, Duration value, boolean expectedValid) {
		DurationMax annotation = buildDurationMaxAnnotation( days, hours, minutes, seconds, millis, nanos, inclusive );
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );
		if ( expectedValid ) {
			Assert.assertTrue( validator.isValid( value, null ),
					String.format( "Expected valid at boundary: days=%d, inclusive=%s, value=%s", days, inclusive, value ) );
		}
		else {
			Assert.assertFalse( validator.isValid( value, null ),
					String.format( "Expected invalid at boundary: days=%d, inclusive=%s, value=%s", days, inclusive, value ) );
		}
	}

	@DataProvider(name = "durationMaxBoundaryData")
	public static Object[][] durationMaxBoundaryData() {
		return new Object[][] {
				{ 1L, 0L, 0L, 0L, 0L, 0L, true, ONE_DAY, true },
				{ 1L, 0L, 0L, 0L, 0L, 0L, false, ONE_DAY, false },
				{ 1L, 0L, 0L, 0L, 0L, 0L, false, ONE_DAY.minus( ONE_NANO ), true },
				{ 1L, 0L, 0L, 0L, 0L, 0L, true, ONE_DAY.plus( ONE_NANO ), false },
				{ 0L, 0L, 0L, 0L, 0L, 0L, true, ZERO, true },
				{ 0L, 0L, 0L, 0L, 0L, 0L, false, ZERO, false },
				{ 0L, 0L, 0L, 0L, 0L, 0L, false, NEGATIVE_ONE_NANO, true },
				{ 0L, 0L, 0L, 0L, 0L, 0L, true, ONE_NANO, false },
				{ -1L, 0L, 0L, 0L, 0L, 0L, true, NEGATIVE_ONE_DAY, true },
				{ -1L, 0L, 0L, 0L, 0L, 0L, false, NEGATIVE_ONE_DAY, false },
				{ -1L, 0L, 0L, 0L, 0L, 0L, false, NEGATIVE_ONE_DAY.minus( ONE_NANO ), true },
				{ 0L, 0L, 0L, 0L, 0L, 100L, true, HUNDRED_NANOS, true },
				{ 0L, 0L, 0L, 0L, 0L, 100L, false, HUNDRED_NANOS, false },
		};
	}

	// ==============================================
	// Integrated validator tests - DurationMin (exclusive)
	// ==============================================

	@Test(dataProvider = "durationMinIntegratedExclusiveData")
	public void testDurationMinIntegratedExclusive(Duration fieldValue, boolean expectViolation) {
		Validator validator = getValidator();
		MinBean bean = new MinBean( fieldValue );
		Set<ConstraintViolation<MinBean>> violations = validator.validate( bean );
		if ( expectViolation ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class )
			);
		}
		else {
			assertNoViolations( violations );
		}
	}

	@DataProvider(name = "durationMinIntegratedExclusiveData")
	public static Object[][] durationMinIntegratedExclusiveData() {
		return new Object[][] {
				{ null, false },
				{ TEN_SECONDS, false },
				{ ONE_SECOND, true },
				{ ONE_SECOND.plus( ONE_NANO ), false },
				{ ONE_SECOND.minus( ONE_NANO ), true },
				{ ZERO, true },
				{ Duration.ofSeconds( 2 ), false },
				{ Duration.ofNanos( 999999999 ), true },
		};
	}

	// ==============================================
	// Integrated validator tests - DurationMin (inclusive)
	// ==============================================

	@Test(dataProvider = "durationMinIntegratedInclusiveData")
	public void testDurationMinIntegratedInclusive(Duration fieldValue, boolean expectViolation) {
		Validator validator = getValidator();
		MinBeanInclusive bean = new MinBeanInclusive( fieldValue );
		Set<ConstraintViolation<MinBeanInclusive>> violations = validator.validate( bean );
		if ( expectViolation ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class )
			);
		}
		else {
			assertNoViolations( violations );
		}
	}

	@DataProvider(name = "durationMinIntegratedInclusiveData")
	public static Object[][] durationMinIntegratedInclusiveData() {
		return new Object[][] {
				{ null, false },
				{ TEN_SECONDS, false },
				{ ONE_SECOND, false },
				{ ONE_SECOND.plus( ONE_NANO ), false },
				{ ONE_SECOND.minus( ONE_NANO ), true },
				{ ZERO, true },
				{ Duration.ofSeconds( 2 ), false },
				{ Duration.ofNanos( 999999999 ), true },
		};
	}

	// ==============================================
	// Integrated validator tests - DurationMax
	// ==============================================

	@Test(dataProvider = "durationMaxIntegratedData")
	public void testDurationMaxIntegrated(Duration fieldValue, boolean expectViolation) {
		Validator validator = getValidator();
		MaxBean bean = new MaxBean( fieldValue );
		Set<ConstraintViolation<MaxBean>> violations = validator.validate( bean );
		if ( expectViolation ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMax.class )
			);
		}
		else {
			assertNoViolations( violations );
		}
	}

	@DataProvider(name = "durationMaxIntegratedData")
	public static Object[][] durationMaxIntegratedData() {
		return new Object[][] {
				{ null, false },
				{ ONE_SECOND, false },
				{ TEN_SECONDS, false },
				{ Duration.ofSeconds( 11 ), true },
				{ Duration.ofDays( 5 ), true },
				{ ZERO, false },
				{ NEGATIVE_ONE_NANO, false },
				{ TEN_SECONDS.plus( ONE_NANO ), true },
				{ TEN_SECONDS.minus( ONE_NANO ), false },
		};
	}

	// ==============================================
	// Combined DurationMin + DurationMax tests
	// ==============================================

	@Test(dataProvider = "combinedMinMaxData")
	public void testCombinedMinMax(Duration value, boolean expectMinViolation, boolean expectMaxViolation) {
		Validator validator = getValidator();
		MinMaxBean bean = new MinMaxBean( value );
		Set<ConstraintViolation<MinMaxBean>> violations = validator.validate( bean );

		if ( !expectMinViolation && !expectMaxViolation ) {
			assertNoViolations( violations );
		}
		else if ( expectMinViolation && expectMaxViolation ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class ).withProperty( "duration" ),
					violationOf( DurationMax.class ).withProperty( "duration" )
			);
		}
		else if ( expectMinViolation ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMin.class ).withProperty( "duration" )
			);
		}
		else {
			assertThat( violations ).containsOnlyViolations(
					violationOf( DurationMax.class ).withProperty( "duration" )
			);
		}
	}

	@DataProvider(name = "combinedMinMaxData")
	public static Object[][] combinedMinMaxData() {
		return new Object[][] {
				{ null, false, false },
				{ ONE_SECOND, false, false },
				{ TEN_SECONDS, false, false },
				{ ONE_MINUTE, false, false },
				{ ONE_HOUR, false, false },
				{ Duration.ofSeconds( 9 ), true, false },
				{ Duration.ofHours( 3 ), false, true },
				{ ZERO, true, false },
				{ Duration.ofDays( 5 ), false, true },
				{ TEN_SECONDS.plus( ONE_NANO ), false, true },
				{ ONE_SECOND.minus( ONE_NANO ), true, false },
		};
	}

	// ==============================================
	// Negative values tests
	// ==============================================

	@Test(dataProvider = "negativeValuesData")
	public void testDurationMinNegativeValues(long nanos, boolean inclusive, Duration value, boolean expectedValid) {
		DurationMin annotation = buildDurationMinAnnotation( nanos, inclusive );
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );
		Assert.assertEquals( validator.isValid( value, null ), expectedValid,
				String.format( "DurationMin: nanos=%d, inclusive=%s, value=%s", nanos, inclusive, value ) );
	}

	@DataProvider(name = "negativeValuesData")
	public static Object[][] negativeValuesData() {
		return new Object[][] {
				{ -100L, true, ZERO, true },
				{ -100L, true, Duration.ofNanos( -100L ), true },
				{ -100L, false, Duration.ofNanos( -100L ), false },
				{ -100L, false, Duration.ofNanos( -99L ), true },
				{ -100L, true, Duration.ofNanos( -200L ), false },
				{ -100L, false, Duration.ofNanos( -200L ), false },
				{ -1L, true, ZERO, true },
				{ -1L, true, NEGATIVE_ONE_NANO, true },
				{ -1L, false, NEGATIVE_ONE_NANO, false },
				{ -1L, true, Duration.ofNanos( -2L ), false },
		};
	}

	@Test(dataProvider = "negativeValuesMaxData")
	public void testDurationMaxNegativeValues(long nanos, boolean inclusive, Duration value, boolean expectedValid) {
		DurationMax annotation = buildDurationMaxAnnotation( nanos, inclusive );
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );
		Assert.assertEquals( validator.isValid( value, null ), expectedValid,
				String.format( "DurationMax: nanos=%d, inclusive=%s, value=%s", nanos, inclusive, value ) );
	}

	@DataProvider(name = "negativeValuesMaxData")
	public static Object[][] negativeValuesMaxData() {
		return new Object[][] {
				{ -100L, true, ZERO, false },
				{ -100L, true, Duration.ofNanos( -100L ), true },
				{ -100L, false, Duration.ofNanos( -100L ), false },
				{ -100L, false, Duration.ofNanos( -101L ), true },
				{ -100L, true, Duration.ofNanos( -200L ), true },
				{ 0L, true, NEGATIVE_ONE_NANO, true },
				{ 0L, false, NEGATIVE_ONE_NANO, true },
				{ -1L, true, ZERO, false },
				{ -1L, true, NEGATIVE_ONE_NANO, false },
				{ -1L, false, NEGATIVE_ONE_NANO, false },
		};
	}

	// ==============================================
	// Empty annotation (default values) tests
	// ==============================================

	@Test
	public void testDurationMinWithDefaultAnnotationValues() {
		DurationMin annotation = buildDurationMinAnnotation( 0L, true );
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( ZERO, null ) );
		Assert.assertTrue( validator.isValid( ONE_NANO, null ) );
		Assert.assertTrue( validator.isValid( TEN_SECONDS, null ) );
		Assert.assertTrue( validator.isValid( ONE_DAY, null ) );
		Assert.assertFalse( validator.isValid( NEGATIVE_ONE_NANO, null ) );
		Assert.assertFalse( validator.isValid( NEGATIVE_ONE_DAY, null ) );
	}

	@Test
	public void testDurationMaxWithDefaultAnnotationValues() {
		DurationMax annotation = buildDurationMaxAnnotation( 0L, true );
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( ZERO, null ) );
		Assert.assertTrue( validator.isValid( NEGATIVE_ONE_NANO, null ) );
		Assert.assertTrue( validator.isValid( NEGATIVE_ONE_DAY, null ) );
		Assert.assertFalse( validator.isValid( ONE_NANO, null ) );
		Assert.assertFalse( validator.isValid( TEN_SECONDS, null ) );
		Assert.assertFalse( validator.isValid( ONE_DAY, null ) );
	}

	@Test
	public void testDurationMinWithDefaultAnnotationValuesExclusive() {
		DurationMin annotation = buildDurationMinAnnotation( 0L, false );
		DurationMinValidator validator = new DurationMinValidator();
		validator.initialize( annotation );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertFalse( validator.isValid( ZERO, null ) );
		Assert.assertTrue( validator.isValid( ONE_NANO, null ) );
		Assert.assertTrue( validator.isValid( TEN_SECONDS, null ) );
		Assert.assertFalse( validator.isValid( NEGATIVE_ONE_NANO, null ) );
	}

	@Test
	public void testDurationMaxWithDefaultAnnotationValuesExclusive() {
		DurationMax annotation = buildDurationMaxAnnotation( 0L, false );
		DurationMaxValidator validator = new DurationMaxValidator();
		validator.initialize( annotation );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertFalse( validator.isValid( ZERO, null ) );
		Assert.assertTrue( validator.isValid( NEGATIVE_ONE_NANO, null ) );
		Assert.assertTrue( validator.isValid( NEGATIVE_ONE_DAY, null ) );
	}

	// ==============================================
	// Helper methods
	// ==============================================

	private static DurationMin buildDurationMinAnnotation(long nanos, boolean inclusive) {
		return buildDurationMinAnnotation( 0L, 0L, 0L, 0L, 0L, nanos, inclusive );
	}

	private static DurationMin buildDurationMinAnnotation(long days, long hours, long minutes, long seconds, long millis, long nanos, boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMin> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( DurationMin.class );
		descriptorBuilder.setAttribute( "days", days );
		descriptorBuilder.setAttribute( "hours", hours );
		descriptorBuilder.setAttribute( "minutes", minutes );
		descriptorBuilder.setAttribute( "seconds", seconds );
		descriptorBuilder.setAttribute( "millis", millis );
		descriptorBuilder.setAttribute( "nanos", nanos );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		return descriptorBuilder.build().getAnnotation();
	}

	private static DurationMax buildDurationMaxAnnotation(long nanos, boolean inclusive) {
		return buildDurationMaxAnnotation( 0L, 0L, 0L, 0L, 0L, nanos, inclusive );
	}

	private static DurationMax buildDurationMaxAnnotation(long days, long hours, long minutes, long seconds, long millis, long nanos, boolean inclusive) {
		ConstraintAnnotationDescriptor.Builder<DurationMax> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( DurationMax.class );
		descriptorBuilder.setAttribute( "days", days );
		descriptorBuilder.setAttribute( "hours", hours );
		descriptorBuilder.setAttribute( "minutes", minutes );
		descriptorBuilder.setAttribute( "seconds", seconds );
		descriptorBuilder.setAttribute( "millis", millis );
		descriptorBuilder.setAttribute( "nanos", nanos );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		return descriptorBuilder.build().getAnnotation();
	}

	// ==============================================
	// Inner bean classes for integrated tests
	// ==============================================

	private static class MinBean {

		@DurationMin(seconds = 1, inclusive = false)
		private final Duration duration;

		public MinBean(Duration duration) {
			this.duration = duration;
		}
	}

	private static class MinBeanInclusive {

		@DurationMin(seconds = 1)
		private final Duration duration;

		public MinBeanInclusive(Duration duration) {
			this.duration = duration;
		}
	}

	private static class MaxBean {

		@DurationMax(seconds = 10)
		private final Duration duration;

		public MaxBean(Duration duration) {
			this.duration = duration;
		}
	}

	private static class MinMaxBean {

		@DurationMin(seconds = 1)
		@DurationMax(hours = 2)
		private final Duration duration;

		public MinMaxBean(Duration duration) {
			this.duration = duration;
		}
	}
}