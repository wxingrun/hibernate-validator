/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.records;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * Test for nested Record cascading validation property paths.
 * Verifies that property paths use the property name (e.g., "outer.inner.field")
 * instead of class names (e.g., "outerRecord.innerRecord.field").
 * 
 * @author Assistant
 */
public class NestedRecordPropertyPathTest extends AbstractConstrainedTest {

	@Test
	public void testNestedRecordPropertyPath() {
		// Test single level nesting - property path should be "inner.field"
		Set<ConstraintViolation<OuterRecord>> violations = validator.validate(
				new OuterRecord( new InnerRecord( null ) )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotBlank.class ).withPropertyPath( pathWith()
						.property( "inner" )
						.property( "field" )
				)
		);
	}

	@Test
	public void testDoubleNestedRecordPropertyPath() {
		// Test double level nesting - property path should be "outer.inner.field"
		Set<ConstraintViolation<DoubleOuterRecord>> violations = validator.validate(
				new DoubleOuterRecord( new OuterRecord( new InnerRecord( null ) ) )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotBlank.class ).withPropertyPath( pathWith()
						.property( "outer" )
						.property( "inner" )
						.property( "field" )
				)
		);
	}

	@Test
	public void testMixedBeanAndRecordPropertyPath() {
		// Test mixing regular Bean and Record - property path should be "beanRecord.inner.field"
		Set<ConstraintViolation<BeanWithNestedRecord>> violations = validator.validate(
				new BeanWithNestedRecord( new OuterRecord( new InnerRecord( null ) ) )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotBlank.class ).withPropertyPath( pathWith()
						.property( "beanRecord" )
						.property( "inner" )
						.property( "field" )
				)
		);
	}

	@Test
	public void testTripleNestedRecordPropertyPath() {
		// Test triple level nesting - property path should be "level1.level2.level3.field"
		Set<ConstraintViolation<Level1Record>> violations = validator.validate(
				new Level1Record( new Level2Record( new Level3Record( null ) ) )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotBlank.class ).withPropertyPath( pathWith()
						.property( "level1" )
						.property( "level2" )
						.property( "level3" )
						.property( "field" )
				)
		);
	}

	@Test
	public void testRecordWithMultipleFieldsPropertyPath() {
		// Test Record with multiple fields - both should have correct property paths
		Set<ConstraintViolation<MultiFieldOuterRecord>> violations = validator.validate(
				new MultiFieldOuterRecord( new InnerRecord( null ), new InnerRecord( null ) )
		);

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotBlank.class ).withPropertyPath( pathWith()
						.property( "first" )
						.property( "field" )
				),
				violationOf( NotBlank.class ).withPropertyPath( pathWith()
						.property( "second" )
						.property( "field" )
				)
		);
	}

	// Record definitions
	private record InnerRecord(@NotBlank String field) {
	}

	private record OuterRecord(@Valid @NotNull InnerRecord inner) {
	}

	private record DoubleOuterRecord(@Valid @NotNull OuterRecord outer) {
	}

	private record Level1Record(@Valid @NotNull Level2Record level1) {
	}

	private record Level2Record(@Valid @NotNull Level3Record level2) {
	}

	private record Level3Record(@NotBlank String level3) {
	}

	private record MultiFieldOuterRecord(@Valid @NotNull InnerRecord first, @Valid @NotNull InnerRecord second) {
	}

	// Regular Java Bean with nested Record
	private static class BeanWithNestedRecord {
		@Valid
		@NotNull
		private final OuterRecord beanRecord;

		public BeanWithNestedRecord(OuterRecord beanRecord) {
			this.beanRecord = beanRecord;
		}

		public OuterRecord getBeanRecord() {
			return beanRecord;
		}
	}
}
