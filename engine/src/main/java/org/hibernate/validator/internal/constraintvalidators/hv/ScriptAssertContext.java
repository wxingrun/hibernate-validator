/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.scripting.ScriptEvaluationException;
import org.hibernate.validator.spi.scripting.ScriptEvaluator;

/**
 * Context used by validator implementations dealing with script expressions. Instances are thread-safe and can be re-used
 * several times to evaluate different bindings against one given given script expression.
 *
 * @author Gunnar Morling
 * @author Marko Bekhta
 */
class ScriptAssertContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String script;
	private final ScriptEvaluator scriptEvaluator;

	public ScriptAssertContext(String script, ScriptEvaluator scriptEvaluator) {
		this.script = script;
		this.scriptEvaluator = scriptEvaluator;
	}

	public boolean evaluateScriptAssertExpression(Object object, String alias) {
		Map<String, Object> bindings = newHashMap();
		bindings.put( alias, object );

		return evaluateScriptAssertExpression( bindings );
	}

	public boolean evaluateScriptAssertExpression(Map<String, Object> bindings) {
		// Wrap objects in bindings to make them more accessible to GraalVM JS engine
		Map<String, Object> wrappedBindings = new HashMap<>( bindings.size() );
		for ( Map.Entry<String, Object> entry : bindings.entrySet() ) {
			wrappedBindings.put( entry.getKey(), wrapObjectForScripting( entry.getValue() ) );
		}

		Object result;

		try {
			result = scriptEvaluator.evaluate( script, wrappedBindings );
		}
		catch (ScriptEvaluationException e) {
			throw LOG.getErrorDuringScriptExecutionException( script, e );
		}

		return handleResult( result );
	}

	/**
	 * Wraps an object to make its properties and methods more easily accessible from script engines,
	 * especially GraalVM JavaScript in Java 21+ environments.
	 *
	 * @param object the object to wrap
	 * @return the wrapped object, or the original object if wrapping fails
	 */
	private Object wrapObjectForScripting(Object object) {
		if ( object == null ) {
			return null;
		}

		// Try to wrap the object in a Map-based proxy for easier property access
		try {
			return new ObjectWrapper( object );
		}
		catch (Exception e) {
			// If wrapping fails, return original object
			return object;
		}
	}

	private boolean handleResult(Object evaluationResult) {
		if ( evaluationResult == null ) {
			throw LOG.getScriptMustReturnTrueOrFalseException( script );
		}

		if ( !( evaluationResult instanceof Boolean ) ) {
			throw LOG.getScriptMustReturnTrueOrFalseException(
					script,
					evaluationResult,
					evaluationResult.getClass().getCanonicalName()
			);
		}

		return Boolean.TRUE.equals( evaluationResult );
	}

	/**
	 * A wrapper class that exposes an object's properties in a way that's more accessible
	 * to script engines, particularly GraalVM JS in Java 21+.
	 * Implements Map interface for seamless integration with JavaScript engines.
	 */
	static class ObjectWrapper implements Map<String, Object> {
		private final Object target;
		private final Map<String, Object> properties;

		public ObjectWrapper(Object target) {
			this.target = target;
			this.properties = new HashMap<>();
			populateProperties( target );
		}

		private void populateProperties(Object object) {
			// Add all public fields
			for ( Field field : object.getClass().getFields() ) {
				try {
					properties.put( field.getName(), field.get( object ) );
				}
				catch (Exception e) {
					// Ignore if we can't access the field
				}
			}

			// Add all getters
			for ( Method method : object.getClass().getMethods() ) {
				if ( method.getParameterCount() == 0 ) {
					String methodName = method.getName();
					if ( methodName.startsWith( "get" ) && methodName.length() > 3 ) {
						String propertyName = Character.toLowerCase( methodName.charAt( 3 ) ) + methodName.substring( 4 );
						try {
							Object value = method.invoke( object );
							properties.put( propertyName, value );
						}
						catch (Exception e) {
							// Ignore if we can't access the property
						}
					}
					else if ( methodName.startsWith( "is" ) && methodName.length() > 2 ) {
						String propertyName = Character.toLowerCase( methodName.charAt( 2 ) ) + methodName.substring( 3 );
						try {
							Object value = method.invoke( object );
							properties.put( propertyName, value );
						}
						catch (Exception e) {
							// Ignore if we can't access the property
						}
					}
				}
			}
		}

		/**
		 * Gets a property value by name. This method is used by script engines to access object properties.
		 */
		@Override
		public Object get(Object key) {
			if ( key == null ) {
				return null;
			}
			return properties.get( key.toString() );
		}

		/**
		 * Gets a property value by name. This method is used by script engines to access object properties.
		 */
		public Object getProperty(String propertyName) {
			return properties.get( propertyName );
		}

		/**
		 * Checks if a property exists.
		 */
		public boolean hasProperty(String propertyName) {
			return properties.containsKey( propertyName );
		}

		/**
		 * Gets all property names.
		 */
		public String[] getPropertyNames() {
			return properties.keySet().toArray( new String[properties.size()] );
		}

		/**
		 * Gets the original target object.
		 */
		public Object getTarget() {
			return target;
		}

		// Map interface implementations
		@Override
		public int size() {
			return properties.size();
		}

		@Override
		public boolean isEmpty() {
			return properties.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return properties.containsKey( key );
		}

		@Override
		public boolean containsValue(Object value) {
			return properties.containsValue( value );
		}

		@Override
		public Object put(String key, Object value) {
			return properties.put( key, value );
		}

		@Override
		public Object remove(Object key) {
			return properties.remove( key );
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> m) {
			properties.putAll( m );
		}

		@Override
		public void clear() {
			properties.clear();
		}

		@Override
		public Set<String> keySet() {
			return properties.keySet();
		}

		@Override
		public Collection<Object> values() {
			return properties.values();
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			return properties.entrySet();
		}

		@Override
		public String toString() {
			return "ObjectWrapper{" +
					"target=" + target +
					", properties=" + properties +
					'}';
		}
	}
}
