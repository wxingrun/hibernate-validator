package org.hibernate.validator.test.constraints.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DurationMinMaxParameterizedTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = ValidatorUtil.getValidator();
	}

	@DataProvider(name = "durationMinData")
	public Object[][] durationMinData() {
		return new Object[][] {
				// Duration based
				{ Duration.ofSeconds( 10 ), true },
				{ Duration.ofSeconds( 11 ), true },
				{ Duration.ofSeconds( 9 ), false },
				{ Duration.ofSeconds( 0 ), false },
				{ Duration.ofSeconds( -5 ), false },
				{ null, true }, // null values are valid

				// Instant based
				{ Duration.between( Instant.EPOCH, Instant.EPOCH.plusSeconds( 10 ) ), true },
				{ Duration.between( Instant.EPOCH, Instant.EPOCH.plusSeconds( 9 ) ), false },

				// LocalDateTime based
				{ Duration.between( LocalDateTime.MIN, LocalDateTime.MIN.plusSeconds( 10 ) ), true },
				{ Duration.between( LocalDateTime.MIN, LocalDateTime.MIN.plusSeconds( 9 ) ), false },

				// ZonedDateTime based
				{ Duration.between( ZonedDateTime.now(), ZonedDateTime.now().plusSeconds( 10 ) ), true },
				{ Duration.between( ZonedDateTime.now(), ZonedDateTime.now().plusSeconds( 9 ) ), false },

				// OffsetDateTime based
				{ Duration.between( OffsetDateTime.now(), OffsetDateTime.now().plusSeconds( 10 ) ), true },
				{ Duration.between( OffsetDateTime.now(), OffsetDateTime.now().plusSeconds( 9 ) ), false },
		};
	}

	@Test(dataProvider = "durationMinData")
	public void testDurationMinInclusive(Duration duration, boolean expectedValid) {
		MinInclusiveTask task = new MinInclusiveTask( duration );
		Set<ConstraintViolation<MinInclusiveTask>> violations = validator.validate( task );

		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations( violationOf( DurationMin.class ) );
		}
	}

	@DataProvider(name = "durationMinExclusiveData")
	public Object[][] durationMinExclusiveData() {
		return new Object[][] {
				{ Duration.ofSeconds( 11 ), true },
				{ Duration.ofSeconds( 10 ), false }, // Exclusive boundary
				{ Duration.ofSeconds( 9 ), false },
				{ null, true }
		};
	}

	@Test(dataProvider = "durationMinExclusiveData")
	public void testDurationMinExclusive(Duration duration, boolean expectedValid) {
		MinExclusiveTask task = new MinExclusiveTask( duration );
		Set<ConstraintViolation<MinExclusiveTask>> violations = validator.validate( task );

		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations( violationOf( DurationMin.class ) );
		}
	}

	@DataProvider(name = "durationMaxData")
	public Object[][] durationMaxData() {
		return new Object[][] {
				// Duration based
				{ Duration.ofSeconds( 10 ), true },
				{ Duration.ofSeconds( 9 ), true },
				{ Duration.ofSeconds( 11 ), false },
				{ Duration.ofSeconds( 0 ), true },
				{ Duration.ofSeconds( -5 ), true },
				{ null, true }, // null values are valid

				// Instant based
				{ Duration.between( Instant.EPOCH, Instant.EPOCH.plusSeconds( 10 ) ), true },
				{ Duration.between( Instant.EPOCH, Instant.EPOCH.plusSeconds( 11 ) ), false },

				// LocalDateTime based
				{ Duration.between( LocalDateTime.MIN, LocalDateTime.MIN.plusSeconds( 10 ) ), true },
				{ Duration.between( LocalDateTime.MIN, LocalDateTime.MIN.plusSeconds( 11 ) ), false },

				// ZonedDateTime based
				{ Duration.between( ZonedDateTime.now(), ZonedDateTime.now().plusSeconds( 10 ) ), true },
				{ Duration.between( ZonedDateTime.now(), ZonedDateTime.now().plusSeconds( 11 ) ), false },

				// OffsetDateTime based
				{ Duration.between( OffsetDateTime.now(), OffsetDateTime.now().plusSeconds( 10 ) ), true },
				{ Duration.between( OffsetDateTime.now(), OffsetDateTime.now().plusSeconds( 11 ) ), false },
		};
	}

	@Test(dataProvider = "durationMaxData")
	public void testDurationMaxInclusive(Duration duration, boolean expectedValid) {
		MaxInclusiveTask task = new MaxInclusiveTask( duration );
		Set<ConstraintViolation<MaxInclusiveTask>> violations = validator.validate( task );

		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations( violationOf( DurationMax.class ) );
		}
	}

	@DataProvider(name = "durationMaxExclusiveData")
	public Object[][] durationMaxExclusiveData() {
		return new Object[][] {
				{ Duration.ofSeconds( 9 ), true },
				{ Duration.ofSeconds( 10 ), false }, // Exclusive boundary
				{ Duration.ofSeconds( 11 ), false },
				{ null, true }
		};
	}

	@Test(dataProvider = "durationMaxExclusiveData")
	public void testDurationMaxExclusive(Duration duration, boolean expectedValid) {
		MaxExclusiveTask task = new MaxExclusiveTask( duration );
		Set<ConstraintViolation<MaxExclusiveTask>> violations = validator.validate( task );

		if ( expectedValid ) {
			assertNoViolations( violations );
		}
		else {
			assertThat( violations ).containsOnlyViolations( violationOf( DurationMax.class ) );
		}
	}

	private static class MinInclusiveTask {
		@DurationMin(seconds = 10, inclusive = true)
		private final Duration duration;

		MinInclusiveTask(Duration duration) {
			this.duration = duration;
		}
	}

	private static class MinExclusiveTask {
		@DurationMin(seconds = 10, inclusive = false)
		private final Duration duration;

		MinExclusiveTask(Duration duration) {
			this.duration = duration;
		}
	}

	private static class MaxInclusiveTask {
		@DurationMax(seconds = 10, inclusive = true)
		private final Duration duration;

		MaxInclusiveTask(Duration duration) {
			this.duration = duration;
		}
	}

	private static class MaxExclusiveTask {
		@DurationMax(seconds = 10, inclusive = false)
		private final Duration duration;

		MaxExclusiveTask(Duration duration) {
			this.duration = duration;
		}
	}
}
