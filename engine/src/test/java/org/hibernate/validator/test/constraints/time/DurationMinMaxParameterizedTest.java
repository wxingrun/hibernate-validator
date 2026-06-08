/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.time;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.validation.Validator;

import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DurationMinMaxParameterizedTest {

	private static final ZoneId ZONE_ID = ZoneId.of( "Europe/Berlin" );
	private static final ZonedDateTime REFERENCE_DATE_TIME = ZonedDateTime.of( 2024, 3, 15, 10, 30, 0, 0, ZONE_ID );
	private static final Duration MIN_MAX_THRESHOLD = Duration.ofHours( 1 );

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getConfiguration()
				.clockProvider( () -> Clock.fixed( REFERENCE_DATE_TIME.toInstant(), REFERENCE_DATE_TIME.getZone() ) )
				.buildValidatorFactory()
				.getValidator();
	}

	@Test(dataProvider = "validDurationMinInclusiveValues")
	public void testDurationMinInclusiveAcceptsValuesAtOrAboveTheBoundary(String scenario, Object bean) {
		assertNoViolations( validator.validate( bean ) );
	}

	@Test(dataProvider = "invalidDurationMinInclusiveValues")
	public void testDurationMinInclusiveRejectsValuesBelowTheBoundary(String scenario, Object bean, String property) {
		assertThat( validator.validate( bean ) ).containsOnlyViolations(
				violationOf( DurationMin.class ).withProperty( property )
		);
	}

	@Test(dataProvider = "validDurationMinExclusiveValues")
	public void testDurationMinExclusiveAcceptsValuesAboveTheBoundary(String scenario, Object bean) {
		assertNoViolations( validator.validate( bean ) );
	}

	@Test(dataProvider = "invalidDurationMinExclusiveValues")
	public void testDurationMinExclusiveRejectsBoundaryAndLowerValues(String scenario, Object bean, String property) {
		assertThat( validator.validate( bean ) ).containsOnlyViolations(
				violationOf( DurationMin.class ).withProperty( property )
		);
	}

	@Test(dataProvider = "validDurationMaxInclusiveValues")
	public void testDurationMaxInclusiveAcceptsValuesAtOrBelowTheBoundary(String scenario, Object bean) {
		assertNoViolations( validator.validate( bean ) );
	}

	@Test(dataProvider = "invalidDurationMaxInclusiveValues")
	public void testDurationMaxInclusiveRejectsValuesAboveTheBoundary(String scenario, Object bean, String property) {
		assertThat( validator.validate( bean ) ).containsOnlyViolations(
				violationOf( DurationMax.class ).withProperty( property )
		);
	}

	@Test(dataProvider = "validDurationMaxExclusiveValues")
	public void testDurationMaxExclusiveAcceptsValuesBelowTheBoundary(String scenario, Object bean) {
		assertNoViolations( validator.validate( bean ) );
	}

	@Test(dataProvider = "invalidDurationMaxExclusiveValues")
	public void testDurationMaxExclusiveRejectsBoundaryAndHigherValues(String scenario, Object bean, String property) {
		assertThat( validator.validate( bean ) ).containsOnlyViolations(
				violationOf( DurationMax.class ).withProperty( property )
		);
	}

	@DataProvider(name = "validDurationMinInclusiveValues")
	private static Object[][] validDurationMinInclusiveValues() {
		return new Object[][] {
				{ "Duration null", DurationMinInclusiveBean.withDuration( null ) },
				{ "Duration boundary", DurationMinInclusiveBean.withDuration( MIN_MAX_THRESHOLD ) },
				{ "Duration positive", DurationMinInclusiveBean.withDuration( MIN_MAX_THRESHOLD.plusMinutes( 30 ) ) },
				{ "Instant null", DurationMinInclusiveBean.withInstant( null ) },
				{ "Instant boundary", DurationMinInclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD ) ) },
				{ "Instant positive", DurationMinInclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD.plusMinutes( 30 ) ) ) },
				{ "LocalDateTime null", DurationMinInclusiveBean.withLocalDateTime( null ) },
				{ "LocalDateTime boundary", DurationMinInclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD ) ) },
				{ "LocalDateTime positive", DurationMinInclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD.plusMinutes( 30 ) ) ) },
				{ "ZonedDateTime null", DurationMinInclusiveBean.withZonedDateTime( null ) },
				{ "ZonedDateTime boundary", DurationMinInclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD ) ) },
				{ "ZonedDateTime positive", DurationMinInclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD.plusMinutes( 30 ) ) ) },
				{ "OffsetDateTime null", DurationMinInclusiveBean.withOffsetDateTime( null ) },
				{ "OffsetDateTime boundary", DurationMinInclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD ) ) },
				{ "OffsetDateTime positive", DurationMinInclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD.plusMinutes( 30 ) ) ) }
		};
	}

	@DataProvider(name = "invalidDurationMinInclusiveValues")
	private static Object[][] invalidDurationMinInclusiveValues() {
		return new Object[][] {
				{ "Duration below boundary", DurationMinInclusiveBean.withDuration( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ), "duration" },
				{ "Duration negative", DurationMinInclusiveBean.withDuration( Duration.ofMinutes( -30 ) ), "duration" },
				{ "Instant below boundary", DurationMinInclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ), "instant" },
				{ "Instant negative", DurationMinInclusiveBean.withInstant( instant( Duration.ofMinutes( -30 ) ) ), "instant" },
				{ "LocalDateTime below boundary", DurationMinInclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ), "localDateTime" },
				{ "LocalDateTime negative", DurationMinInclusiveBean.withLocalDateTime( localDateTime( Duration.ofMinutes( -30 ) ) ), "localDateTime" },
				{ "ZonedDateTime below boundary", DurationMinInclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ), "zonedDateTime" },
				{ "ZonedDateTime negative", DurationMinInclusiveBean.withZonedDateTime( zonedDateTime( Duration.ofMinutes( -30 ) ) ), "zonedDateTime" },
				{ "OffsetDateTime below boundary", DurationMinInclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ), "offsetDateTime" },
				{ "OffsetDateTime negative", DurationMinInclusiveBean.withOffsetDateTime( offsetDateTime( Duration.ofMinutes( -30 ) ) ), "offsetDateTime" }
		};
	}

	@DataProvider(name = "validDurationMinExclusiveValues")
	private static Object[][] validDurationMinExclusiveValues() {
		return new Object[][] {
				{ "Duration null", DurationMinExclusiveBean.withDuration( null ) },
				{ "Duration positive", DurationMinExclusiveBean.withDuration( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) },
				{ "Instant null", DurationMinExclusiveBean.withInstant( null ) },
				{ "Instant positive", DurationMinExclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ) },
				{ "LocalDateTime null", DurationMinExclusiveBean.withLocalDateTime( null ) },
				{ "LocalDateTime positive", DurationMinExclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ) },
				{ "ZonedDateTime null", DurationMinExclusiveBean.withZonedDateTime( null ) },
				{ "ZonedDateTime positive", DurationMinExclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ) },
				{ "OffsetDateTime null", DurationMinExclusiveBean.withOffsetDateTime( null ) },
				{ "OffsetDateTime positive", DurationMinExclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ) }
		};
	}

	@DataProvider(name = "invalidDurationMinExclusiveValues")
	private static Object[][] invalidDurationMinExclusiveValues() {
		return new Object[][] {
				{ "Duration boundary", DurationMinExclusiveBean.withDuration( MIN_MAX_THRESHOLD ), "duration" },
				{ "Duration negative", DurationMinExclusiveBean.withDuration( Duration.ofMinutes( -30 ) ), "duration" },
				{ "Instant boundary", DurationMinExclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD ) ), "instant" },
				{ "Instant negative", DurationMinExclusiveBean.withInstant( instant( Duration.ofMinutes( -30 ) ) ), "instant" },
				{ "LocalDateTime boundary", DurationMinExclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD ) ), "localDateTime" },
				{ "LocalDateTime negative", DurationMinExclusiveBean.withLocalDateTime( localDateTime( Duration.ofMinutes( -30 ) ) ), "localDateTime" },
				{ "ZonedDateTime boundary", DurationMinExclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD ) ), "zonedDateTime" },
				{ "ZonedDateTime negative", DurationMinExclusiveBean.withZonedDateTime( zonedDateTime( Duration.ofMinutes( -30 ) ) ), "zonedDateTime" },
				{ "OffsetDateTime boundary", DurationMinExclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD ) ), "offsetDateTime" },
				{ "OffsetDateTime negative", DurationMinExclusiveBean.withOffsetDateTime( offsetDateTime( Duration.ofMinutes( -30 ) ) ), "offsetDateTime" }
		};
	}

	@DataProvider(name = "validDurationMaxInclusiveValues")
	private static Object[][] validDurationMaxInclusiveValues() {
		return new Object[][] {
				{ "Duration null", DurationMaxInclusiveBean.withDuration( null ) },
				{ "Duration boundary", DurationMaxInclusiveBean.withDuration( MIN_MAX_THRESHOLD ) },
				{ "Duration positive", DurationMaxInclusiveBean.withDuration( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) },
				{ "Duration negative", DurationMaxInclusiveBean.withDuration( Duration.ofMinutes( -30 ) ) },
				{ "Instant null", DurationMaxInclusiveBean.withInstant( null ) },
				{ "Instant boundary", DurationMaxInclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD ) ) },
				{ "Instant positive", DurationMaxInclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "Instant negative", DurationMaxInclusiveBean.withInstant( instant( Duration.ofMinutes( -30 ) ) ) },
				{ "LocalDateTime null", DurationMaxInclusiveBean.withLocalDateTime( null ) },
				{ "LocalDateTime boundary", DurationMaxInclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD ) ) },
				{ "LocalDateTime positive", DurationMaxInclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "LocalDateTime negative", DurationMaxInclusiveBean.withLocalDateTime( localDateTime( Duration.ofMinutes( -30 ) ) ) },
				{ "ZonedDateTime null", DurationMaxInclusiveBean.withZonedDateTime( null ) },
				{ "ZonedDateTime boundary", DurationMaxInclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD ) ) },
				{ "ZonedDateTime positive", DurationMaxInclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "ZonedDateTime negative", DurationMaxInclusiveBean.withZonedDateTime( zonedDateTime( Duration.ofMinutes( -30 ) ) ) },
				{ "OffsetDateTime null", DurationMaxInclusiveBean.withOffsetDateTime( null ) },
				{ "OffsetDateTime boundary", DurationMaxInclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD ) ) },
				{ "OffsetDateTime positive", DurationMaxInclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "OffsetDateTime negative", DurationMaxInclusiveBean.withOffsetDateTime( offsetDateTime( Duration.ofMinutes( -30 ) ) ) }
		};
	}

	@DataProvider(name = "invalidDurationMaxInclusiveValues")
	private static Object[][] invalidDurationMaxInclusiveValues() {
		return new Object[][] {
				{ "Duration above boundary", DurationMaxInclusiveBean.withDuration( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ), "duration" },
				{ "Instant above boundary", DurationMaxInclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "instant" },
				{ "LocalDateTime above boundary", DurationMaxInclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "localDateTime" },
				{ "ZonedDateTime above boundary", DurationMaxInclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "zonedDateTime" },
				{ "OffsetDateTime above boundary", DurationMaxInclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "offsetDateTime" }
		};
	}

	@DataProvider(name = "validDurationMaxExclusiveValues")
	private static Object[][] validDurationMaxExclusiveValues() {
		return new Object[][] {
				{ "Duration null", DurationMaxExclusiveBean.withDuration( null ) },
				{ "Duration positive", DurationMaxExclusiveBean.withDuration( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) },
				{ "Duration negative", DurationMaxExclusiveBean.withDuration( Duration.ofMinutes( -30 ) ) },
				{ "Instant null", DurationMaxExclusiveBean.withInstant( null ) },
				{ "Instant positive", DurationMaxExclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "Instant negative", DurationMaxExclusiveBean.withInstant( instant( Duration.ofMinutes( -30 ) ) ) },
				{ "LocalDateTime null", DurationMaxExclusiveBean.withLocalDateTime( null ) },
				{ "LocalDateTime positive", DurationMaxExclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "LocalDateTime negative", DurationMaxExclusiveBean.withLocalDateTime( localDateTime( Duration.ofMinutes( -30 ) ) ) },
				{ "ZonedDateTime null", DurationMaxExclusiveBean.withZonedDateTime( null ) },
				{ "ZonedDateTime positive", DurationMaxExclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "ZonedDateTime negative", DurationMaxExclusiveBean.withZonedDateTime( zonedDateTime( Duration.ofMinutes( -30 ) ) ) },
				{ "OffsetDateTime null", DurationMaxExclusiveBean.withOffsetDateTime( null ) },
				{ "OffsetDateTime positive", DurationMaxExclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD.minusSeconds( 1 ) ) ) },
				{ "OffsetDateTime negative", DurationMaxExclusiveBean.withOffsetDateTime( offsetDateTime( Duration.ofMinutes( -30 ) ) ) }
		};
	}

	@DataProvider(name = "invalidDurationMaxExclusiveValues")
	private static Object[][] invalidDurationMaxExclusiveValues() {
		return new Object[][] {
				{ "Duration boundary", DurationMaxExclusiveBean.withDuration( MIN_MAX_THRESHOLD ), "duration" },
				{ "Duration above boundary", DurationMaxExclusiveBean.withDuration( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ), "duration" },
				{ "Instant boundary", DurationMaxExclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD ) ), "instant" },
				{ "Instant above boundary", DurationMaxExclusiveBean.withInstant( instant( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "instant" },
				{ "LocalDateTime boundary", DurationMaxExclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD ) ), "localDateTime" },
				{ "LocalDateTime above boundary", DurationMaxExclusiveBean.withLocalDateTime( localDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "localDateTime" },
				{ "ZonedDateTime boundary", DurationMaxExclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD ) ), "zonedDateTime" },
				{ "ZonedDateTime above boundary", DurationMaxExclusiveBean.withZonedDateTime( zonedDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "zonedDateTime" },
				{ "OffsetDateTime boundary", DurationMaxExclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD ) ), "offsetDateTime" },
				{ "OffsetDateTime above boundary", DurationMaxExclusiveBean.withOffsetDateTime( offsetDateTime( MIN_MAX_THRESHOLD.plusSeconds( 1 ) ) ), "offsetDateTime" }
		};
	}

	private static Instant instant(Duration offset) {
		return REFERENCE_DATE_TIME.toInstant().plus( offset );
	}

	private static LocalDateTime localDateTime(Duration offset) {
		return REFERENCE_DATE_TIME.toLocalDateTime().plus( offset );
	}

	private static ZonedDateTime zonedDateTime(Duration offset) {
		return REFERENCE_DATE_TIME.plus( offset );
	}

	private static OffsetDateTime offsetDateTime(Duration offset) {
		return REFERENCE_DATE_TIME.toOffsetDateTime().plus( offset );
	}

	private static class DurationMinInclusiveBean {

		@DurationMin(hours = 1)
		private final Duration duration;

		@DurationMin(hours = 1)
		private final Instant instant;

		@DurationMin(hours = 1)
		private final LocalDateTime localDateTime;

		@DurationMin(hours = 1)
		private final ZonedDateTime zonedDateTime;

		@DurationMin(hours = 1)
		private final OffsetDateTime offsetDateTime;

		private DurationMinInclusiveBean(Duration duration, Instant instant, LocalDateTime localDateTime, ZonedDateTime zonedDateTime,
				OffsetDateTime offsetDateTime) {
			this.duration = duration;
			this.instant = instant;
			this.localDateTime = localDateTime;
			this.zonedDateTime = zonedDateTime;
			this.offsetDateTime = offsetDateTime;
		}

		private static DurationMinInclusiveBean withDuration(Duration value) {
			return new DurationMinInclusiveBean( value, null, null, null, null );
		}

		private static DurationMinInclusiveBean withInstant(Instant value) {
			return new DurationMinInclusiveBean( null, value, null, null, null );
		}

		private static DurationMinInclusiveBean withLocalDateTime(LocalDateTime value) {
			return new DurationMinInclusiveBean( null, null, value, null, null );
		}

		private static DurationMinInclusiveBean withZonedDateTime(ZonedDateTime value) {
			return new DurationMinInclusiveBean( null, null, null, value, null );
		}

		private static DurationMinInclusiveBean withOffsetDateTime(OffsetDateTime value) {
			return new DurationMinInclusiveBean( null, null, null, null, value );
		}
	}

	private static class DurationMinExclusiveBean {

		@DurationMin(hours = 1, inclusive = false)
		private final Duration duration;

		@DurationMin(hours = 1, inclusive = false)
		private final Instant instant;

		@DurationMin(hours = 1, inclusive = false)
		private final LocalDateTime localDateTime;

		@DurationMin(hours = 1, inclusive = false)
		private final ZonedDateTime zonedDateTime;

		@DurationMin(hours = 1, inclusive = false)
		private final OffsetDateTime offsetDateTime;

		private DurationMinExclusiveBean(Duration duration, Instant instant, LocalDateTime localDateTime, ZonedDateTime zonedDateTime,
				OffsetDateTime offsetDateTime) {
			this.duration = duration;
			this.instant = instant;
			this.localDateTime = localDateTime;
			this.zonedDateTime = zonedDateTime;
			this.offsetDateTime = offsetDateTime;
		}

		private static DurationMinExclusiveBean withDuration(Duration value) {
			return new DurationMinExclusiveBean( value, null, null, null, null );
		}

		private static DurationMinExclusiveBean withInstant(Instant value) {
			return new DurationMinExclusiveBean( null, value, null, null, null );
		}

		private static DurationMinExclusiveBean withLocalDateTime(LocalDateTime value) {
			return new DurationMinExclusiveBean( null, null, value, null, null );
		}

		private static DurationMinExclusiveBean withZonedDateTime(ZonedDateTime value) {
			return new DurationMinExclusiveBean( null, null, null, value, null );
		}

		private static DurationMinExclusiveBean withOffsetDateTime(OffsetDateTime value) {
			return new DurationMinExclusiveBean( null, null, null, null, value );
		}
	}

	private static class DurationMaxInclusiveBean {

		@DurationMax(hours = 1)
		private final Duration duration;

		@DurationMax(hours = 1)
		private final Instant instant;

		@DurationMax(hours = 1)
		private final LocalDateTime localDateTime;

		@DurationMax(hours = 1)
		private final ZonedDateTime zonedDateTime;

		@DurationMax(hours = 1)
		private final OffsetDateTime offsetDateTime;

		private DurationMaxInclusiveBean(Duration duration, Instant instant, LocalDateTime localDateTime, ZonedDateTime zonedDateTime,
				OffsetDateTime offsetDateTime) {
			this.duration = duration;
			this.instant = instant;
			this.localDateTime = localDateTime;
			this.zonedDateTime = zonedDateTime;
			this.offsetDateTime = offsetDateTime;
		}

		private static DurationMaxInclusiveBean withDuration(Duration value) {
			return new DurationMaxInclusiveBean( value, null, null, null, null );
		}

		private static DurationMaxInclusiveBean withInstant(Instant value) {
			return new DurationMaxInclusiveBean( null, value, null, null, null );
		}

		private static DurationMaxInclusiveBean withLocalDateTime(LocalDateTime value) {
			return new DurationMaxInclusiveBean( null, null, value, null, null );
		}

		private static DurationMaxInclusiveBean withZonedDateTime(ZonedDateTime value) {
			return new DurationMaxInclusiveBean( null, null, null, value, null );
		}

		private static DurationMaxInclusiveBean withOffsetDateTime(OffsetDateTime value) {
			return new DurationMaxInclusiveBean( null, null, null, null, value );
		}
	}

	private static class DurationMaxExclusiveBean {

		@DurationMax(hours = 1, inclusive = false)
		private final Duration duration;

		@DurationMax(hours = 1, inclusive = false)
		private final Instant instant;

		@DurationMax(hours = 1, inclusive = false)
		private final LocalDateTime localDateTime;

		@DurationMax(hours = 1, inclusive = false)
		private final ZonedDateTime zonedDateTime;

		@DurationMax(hours = 1, inclusive = false)
		private final OffsetDateTime offsetDateTime;

		private DurationMaxExclusiveBean(Duration duration, Instant instant, LocalDateTime localDateTime, ZonedDateTime zonedDateTime,
				OffsetDateTime offsetDateTime) {
			this.duration = duration;
			this.instant = instant;
			this.localDateTime = localDateTime;
			this.zonedDateTime = zonedDateTime;
			this.offsetDateTime = offsetDateTime;
		}

		private static DurationMaxExclusiveBean withDuration(Duration value) {
			return new DurationMaxExclusiveBean( value, null, null, null, null );
		}

		private static DurationMaxExclusiveBean withInstant(Instant value) {
			return new DurationMaxExclusiveBean( null, value, null, null, null );
		}

		private static DurationMaxExclusiveBean withLocalDateTime(LocalDateTime value) {
			return new DurationMaxExclusiveBean( null, null, value, null, null );
		}

		private static DurationMaxExclusiveBean withZonedDateTime(ZonedDateTime value) {
			return new DurationMaxExclusiveBean( null, null, null, value, null );
		}

		private static DurationMaxExclusiveBean withOffsetDateTime(OffsetDateTime value) {
			return new DurationMaxExclusiveBean( null, null, null, null, value );
		}
	}
}
