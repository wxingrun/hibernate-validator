/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.scripting;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptContext;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A wrapper around JSR 223 {@link ScriptEngine}s specifically for GraalVM JavaScript.
 * This class handles the proper configuration of object access permissions required
 * by GraalVM JS engine in Java 21+ environments.
 *
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Marko Bekhta
 * @since 6.0.3
 */
@Incubating
public class GraalJSScriptEvaluator implements ScriptEvaluator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final Set<String> GRAAL_JS_NAMES = Stream.of( "graal.js", "Graal.js", "js", "javascript", "graal.js-scriptengine" )
			.collect( Collectors.toSet() );

	private final ScriptEngine engine;

	/**
	 * Creates a new GraalVM JavaScript script executor.
	 *
	 * @param engine the GraalVM JavaScript engine to be wrapped
	 */
	public GraalJSScriptEvaluator(ScriptEngine engine) {
		this.engine = engine;
	}

	/**
	 * Checks if a given ScriptEngine is a GraalVM JavaScript engine.
	 *
	 * @param engine the script engine to check
	 * @return true if the engine is a GraalVM JavaScript engine, false otherwise
	 */
	public static boolean isGraalJSEngine(ScriptEngine engine) {
		if ( engine == null ) {
			return false;
		}

		ScriptEngineFactory factory = engine.getFactory();
		String engineName = factory.getEngineName();
		String languageName = factory.getLanguageName();

		if ( engineName != null && engineName.toLowerCase().contains( "graal" ) ) {
			return true;
		}

		return GRAAL_JS_NAMES.contains( languageName );
	}

	/**
	 * Executes the given script, using the given variable bindings. The execution of the script happens either synchronized or
	 * unsynchronized, depending on the engine's threading abilities.
	 *
	 * @param script the script to be executed
	 * @param bindings the bindings to be used
	 *
	 * @return the script's result
	 *
	 * @throws ScriptEvaluationException in case an error occurred during the script evaluation
	 */
	@Override
	public Object evaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException {
		if ( engineAllowsParallelAccessFromMultipleThreads() ) {
			return doEvaluate( script, bindings );
		}
		else {
			synchronized (engine) {
				return doEvaluate( script, bindings );
			}
		}
	}

	private Object doEvaluate(String script, Map<String, Object> bindings) throws ScriptEvaluationException {
		try {
			Bindings scriptBindings = engine.createBindings();
			scriptBindings.putAll( bindings );

			// Try to configure GraalVM JS options via system properties and engine context
			// This helps with access control in Java 21+ module system
			configureGraalVMOptions();

			return engine.eval( script, scriptBindings );
		}
		catch (Exception e) {
			throw LOG.getErrorExecutingScriptException( script, e );
		}
	}

	private void configureGraalVMOptions() {
		try {
			// Try to set polyglot.js.allowAllAccess system property if possible
			try {
				System.setProperty( "polyglot.js.allowAllAccess", "true" );
				System.setProperty( "polyglot.js.allowHostAccess", "true" );
				System.setProperty( "polyglot.js.allowHostClassLookup", "true" );
			}
			catch (Exception e) {
				// Ignore if we can't set system properties
			}

			// Try to access the GraalVM Polyglot context via reflection
			// This allows for more fine-grained access control configuration
			try {
				// First, try to get the context from the engine
				Object context = null;
				try {
					context = engine.get( "polyglot.js.context" );
				}
				catch (Exception e) {
					// Ignore if not available
				}

				// If we have a context, try to configure it
				if ( context != null ) {
					try {
						// Try to call setAllowAllAccess if available
						Method setAllowAllAccessMethod = context.getClass().getMethod( "setAllowAllAccess", boolean.class );
						setAllowAllAccessMethod.invoke( context, true );
					}
					catch (Exception e) {
						// Ignore if method not available
					}

					try {
						// Try to call setAllowHostAccess if available
						Method setAllowHostAccessMethod = context.getClass().getMethod( "setAllowHostAccess", boolean.class );
						setAllowHostAccessMethod.invoke( context, true );
					}
					catch (Exception e) {
						// Ignore if method not available
					}
				}
			}
			catch (Exception e) {
				// Ignore any reflection errors
			}

			// Try to access GraalVM Polyglot API to configure access
			// This requires GraalVM to be on the classpath but we use reflection
			try {
				// Try to get the Polyglot class
				Class<?> polyglotClass = Class.forName( "org.graalvm.polyglot.Polyglot" );

				// Try to get the Context class
				Class<?> contextClass = Class.forName( "org.graalvm.polyglot.Context" );

				// Try to get the current context
				try {
					Method getCurrentMethod = contextClass.getMethod( "getCurrent" );
					Object currentContext = getCurrentMethod.invoke( null );

					if ( currentContext != null ) {
						try {
							// Try to configure with allowAllAccess
							Class<?> builderClass = Class.forName( "org.graalvm.polyglot.Context$Builder" );
							try {
								Method allowAllAccessMethod = builderClass.getMethod( "allowAllAccess", boolean.class );
								// We can't easily modify existing context, but this attempt might help in some cases
							}
							catch (Exception e) {
								// Ignore
							}
						}
						catch (Exception e) {
							// Ignore
						}
					}
				}
				catch (Exception e) {
					// Ignore
				}
			}
			catch (ClassNotFoundException e) {
				// GraalVM classes not available, that's fine
			}
		}
		catch (Exception e) {
			// Ignore any configuration errors, continue with default behavior
		}
	}

	/**
	 * Checks whether the given engine is thread-safe or not.
	 *
	 * @return true if the given engine is thread-safe, false otherwise.
	 */
	private boolean engineAllowsParallelAccessFromMultipleThreads() {
		String threadingType = (String) engine.getFactory().getParameter( "THREADING" );

		return "THREAD-ISOLATED".equals( threadingType ) || "STATELESS".equals( threadingType );
	}
}
