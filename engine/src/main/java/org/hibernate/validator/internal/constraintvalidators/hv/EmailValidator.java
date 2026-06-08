package org.hibernate.validator.internal.constraintvalidators.hv;

import java.lang.invoke.MethodHandles;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.internal.constraintvalidators.AbstractEmailValidator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

public class EmailValidator extends AbstractEmailValidator<Email> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private java.util.regex.Pattern pattern;
	private boolean allowTld;

	@Override
	public void initialize(Email emailAnnotation) {
		super.initialize( emailAnnotation );
		this.allowTld = emailAnnotation.allowTld();

		Pattern.Flag[] flags = emailAnnotation.flags();
		int intFlag = 0;
		for ( Pattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		if ( !".*".equals( emailAnnotation.regexp() ) || emailAnnotation.flags().length > 0 ) {
			try {
				pattern = java.util.regex.Pattern.compile( emailAnnotation.regexp(), intFlag );
			}
			catch (PatternSyntaxException e) {
				throw LOG.getInvalidRegularExpressionException( e );
			}
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		boolean isValid = super.isValid( value, context );
		if ( !isValid ) {
			return false;
		}
		
		if ( !allowTld ) {
			String stringValue = value.toString();
			int splitPosition = stringValue.lastIndexOf( '@' );
			if ( splitPosition >= 0 ) {
				String domainPart = stringValue.substring( splitPosition + 1 );
				if ( !domainPart.contains( "." ) && !domainPart.startsWith( "[" ) ) {
					return false;
				}
			}
		}

		if ( pattern == null ) {
			return true;
		}

		Matcher m = pattern.matcher( value );
		return m.matches();
	}
}