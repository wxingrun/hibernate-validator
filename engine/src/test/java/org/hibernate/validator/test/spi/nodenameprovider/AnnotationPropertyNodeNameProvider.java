/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.spi.nodenameprovider;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.nodenameprovider.JavaBeanProperty;
import org.hibernate.validator.spi.nodenameprovider.Property;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProvider;
import org.hibernate.validator.spi.nodenameprovider.PropertyNodeNameProviderContext;
import org.hibernate.validator.spi.nodenameprovider.RecordComponentProperty;

class AnnotationPropertyNodeNameProvider implements PropertyNodeNameProvider, Serializable {
	private static final String VALUE = "value";
	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Class<? extends Annotation> annotationType;
	private final String annotationMemberName;

	AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType) {
		this( annotationType, VALUE );
	}

	AnnotationPropertyNodeNameProvider(Class<? extends Annotation> annotationType, String annotationMemberName) {
		this.annotationType = Objects.requireNonNull( annotationType );
		this.annotationMemberName = Objects.requireNonNull( annotationMemberName );
	}

	@Override
	public String getName(Property property, PropertyNodeNameProviderContext context) {
		if ( property instanceof RecordComponentProperty recordComponentProperty ) {
			return getRecordComponentPropertyName( recordComponentProperty );
		}

		if ( property instanceof JavaBeanProperty javaBeanProperty ) {
			return getJavaBeanPropertyName( javaBeanProperty );
		}

		return getDefaultName( property );
	}

	private String getJavaBeanPropertyName(JavaBeanProperty property) {
		Optional<Field> field = getField( property );

		if ( field.isPresent() && field.get().isAnnotationPresent( annotationType ) ) {
			return getAnnotationMemberValue( field.get(), annotationMemberName )
					.orElse( getDefaultName( property ) );
		}
		else {
			return getDefaultName( property );
		}
	}

	private String getRecordComponentPropertyName(RecordComponentProperty property) {
		Optional<RecordComponent> recordComponent = getRecordComponent( property );

		if ( recordComponent.isPresent() && recordComponent.get().isAnnotationPresent( annotationType ) ) {
			return getAnnotationMemberValue( recordComponent.get(), annotationMemberName )
					.orElse( getDefaultName( property ) );
		}
		else {
			return getDefaultName( property );
		}
	}

	private Optional<Field> getField(JavaBeanProperty property) {
		return Arrays.stream( property.getDeclaringClass().getFields() )
				.peek( field -> field.setAccessible( true ) )
				.filter( field -> property.getName().equals( field.getName() ) )
				.findFirst();
	}

	private Optional<RecordComponent> getRecordComponent(RecordComponentProperty property) {
		if ( !property.getDeclaringClass().isRecord() ) {
			return Optional.empty();
		}

		return Arrays.stream( property.getDeclaringClass().getRecordComponents() )
				.filter( recordComponent -> property.getName().equals( recordComponent.getName() ) )
				.findFirst();
	}

	private Optional<String> getAnnotationMemberValue(AnnotatedElementWrapper annotatedElementWrapper, String annotationMemberName) {
		Annotation annotation = annotatedElementWrapper.getAnnotation( annotationType );

		try {
			return Optional.of(
					(String) annotation.annotationType().getMethod( annotationMemberName ).invoke( annotation ) );
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LOG.error( "Unable to get annotation member value", e );

			return Optional.empty();
		}
	}

	private Optional<String> getAnnotationMemberValue(Field field, String annotationMemberName) {
		return getAnnotationMemberValue( new FieldWrapper( field ), annotationMemberName );
	}

	private Optional<String> getAnnotationMemberValue(RecordComponent recordComponent, String annotationMemberName) {
		return getAnnotationMemberValue( new RecordComponentWrapper( recordComponent ), annotationMemberName );
	}

	private String getDefaultName(Property property) {
		return property.getName();
	}

	private interface AnnotatedElementWrapper {
		Annotation getAnnotation(Class<? extends Annotation> annotationType);
	}

	private static class FieldWrapper implements AnnotatedElementWrapper {
		private final Field field;

		private FieldWrapper(Field field) {
			this.field = field;
		}

		@Override
		public Annotation getAnnotation(Class<? extends Annotation> annotationType) {
			return field.getAnnotation( annotationType );
		}
	}

	private static class RecordComponentWrapper implements AnnotatedElementWrapper {
		private final RecordComponent recordComponent;

		private RecordComponentWrapper(RecordComponent recordComponent) {
			this.recordComponent = recordComponent;
		}

		@Override
		public Annotation getAnnotation(Class<? extends Annotation> annotationType) {
			return recordComponent.getAnnotation( annotationType );
		}
	}
}
