/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.EmailDef;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.hibernate.validator.internal.util.DomainNameUtil;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class EmailValidatorTest {
	private static final String noOrgEmailAddressRegexp = "^((?!\\.org).)*$";
	private static EmailValidator validator;

	@BeforeClass
	public static void init() {
		validator = new EmailValidator();
	}

	@Test
	public void testNullAndEmptyString() throws Exception {
		isValidEmail( "" );
		isValidEmail( null );
	}

	@Test
	public void testValidEmail() throws Exception {
		isValidEmail( "emmanuel@hibernate.org" );
		isValidEmail( "emmanuel@hibernate" );
		isValidEmail( "emma-n_uel@hibernate" );
		isValidEmail( "emma+nuel@hibernate.org" );
		isValidEmail( "emma=nuel@hibernate.org" );
		isValidEmail( "emmanuel@[123.12.2.11]" );
		isValidEmail( "*@example.net" );
		isValidEmail( "fred&barny@example.com" );
		isValidEmail( "---@example.com" );
		isValidEmail( "foo-bar@example.net" );
		isValidEmail( "mailbox.sub1.sub2@this-domain" );
		isValidEmail( "prettyandsimple@example.com" );
		isValidEmail( "very.common@example.com" );
		isValidEmail( "disposable.style.email.with+symbol@example.com" );
		isValidEmail( "other.email-with-dash@example.com" );
		isValidEmail( "x@example.com" );
		isValidEmail( "\"much.more unusual\"@example.com" );
		isValidEmail( "\"very.unusual.@.unusual.com\"@example.com" );
		isValidEmail( "\"very.(),:;<>[]\\\".VERY.\\\"very@\\\\ \\\"very\\\".unusual\"@strange.example.com" );
		isValidEmail( "\"some \\".\\" strange \\".\\" part*:; \"@strange.example.com" );
		isValidEmail( "example-indeed@strange-example.com" );
		isValidEmail( "admin@mailserver1" );
		isValidEmail( "#!$%&'*+-/=?^_`{}|~@example.org" );
		isValidEmail( "\"()<>[]:,;@\\\"!#$%&'-/=?^_`{}| ~.a\"@example.org" );
		isValidEmail( "\" \"@example.org" );
		isValidEmail( "example@localhost" );
		isValidEmail( "example@s.solutions" );
		isValidEmail( "user@localserver" );
		isValidEmail( "user@tt" );
		isValidEmail( "user@[IPv6:2001:DB8::1]" );
		isValidEmail( "xn--80ahgue5b@xn--p-8sbkgc5ag7bhce.xn--ba-lmcq" );
		isValidEmail( "nothing@xn--fken-gra.no" );
	}

	@Test
	public void testInValidEmail() throws Exception {
		isInvalidEmail( "emmanuel.hibernate.org" );
		isInvalidEmail( "emma nuel@hibernate.org" );
		isInvalidEmail( "emma(nuel@hibernate.org" );
		isInvalidEmail( "emmanuel@" );
		isInvalidEmail( "emma\nnuel@hibernate.org" );
		isInvalidEmail( "emma@nuel@hibernate.org" );
		isInvalidEmail( "emma@nuel@.hibernate.org" );
		isInvalidEmail( "Just a string" );
		isInvalidEmail( "string" );
		isInvalidEmail( "me@" );
		isInvalidEmail( "@example.com" );
		isInvalidEmail( "me.@example.com" );
		isInvalidEmail( ".me@example.com" );
		isInvalidEmail( "me@example..com" );
		isInvalidEmail( "me\\@example.com" );
		isInvalidEmail( "Abc.example.com" );
		isInvalidEmail( "A@b@c@example.com" );
		isInvalidEmail( "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com" );
		isInvalidEmail( "just\"not\"right@example.com" );
		isInvalidEmail( "this is\"not\\allowed@example.com" );
		isInvalidEmail( "this\\ still\\\"not\\\\allowed@example.com" );
		isInvalidEmail( "john..doe@example.com" );
		isInvalidEmail( "john.doe@example..com" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-339")
	public void testAccent() {
		isValidEmail( "Test^Email@example.com" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testValidEmailCharSequence() throws Exception {
		isValidEmail( new MyCustomStringImpl( "emmanuel@hibernate.org" ) );
		isInvalidEmail( new MyCustomStringImpl( "@example.com" ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-472")
	public void testMailWithInternationalDomainName() throws Exception {
		isValidEmail( "myname@östereich.at", "A valid email address with umlaut" );
		isValidEmail( "θσερ@εχαμπλε.ψομ", "A valid greek email address" );
		isInvalidEmail( "θσερ.εχαμπλε.ψομ", "Email does not contain an @ character and should be invalid" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-554")
	public void testEmailRegExp() {
		final String email = "hardy@hibernate.org";

		isValidEmail( email );

		Validator validator = ValidatorUtil.getValidator();
		EmailContainer container = new EmailContainerAnnotated();
		container.setEmail( email );
		Set<ConstraintViolation<EmailContainer>> violations = validator.validate( container );
		assertOrgAddressesAreNotValid( violations );

		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( EmailContainer.class )
				.getter( "email" )
				.constraint(
						new EmailDef().regexp( noOrgEmailAddressRegexp )
								.message( "ORG addresses are not valid" )
				);
		config.addMapping( mapping );
		validator = config.buildValidatorFactory().getValidator();

		container = new EmailContainerNoAnnotations();
		container.setEmail( email );
		violations = validator.validate( container );
		assertOrgAddressesAreNotValid( violations );
	}

	@Test
	public void testSingleLabelTopLevelDomainIsInvalidByDefault() {
		isInvalidEmail( "user@com" );
		isInvalidEmail( "admin@org" );
		isValidEmail( "user@localserver" );
	}

	@Test
	public void testSingleLabelTopLevelDomainIsValidWhenAllowTldEnabled() {
		EmailValidator allowTldValidator = newInitializedValidator( true );
		assertTrue( allowTldValidator.isValid( "user@com", null ) );
		assertTrue( allowTldValidator.isValid( "admin@org", null ) );
	}

	@Test
	public void testSingleLabelTopLevelDomainStillRequiresValidEmailWhenAllowTldEnabled() {
		EmailValidator allowTldValidator = newInitializedValidator( true );
		assertFalse( allowTldValidator.isValid( "@org", null ) );
		assertFalse( allowTldValidator.isValid( "user@org.", null ) );
	}

	@Test
	public void testAllowTldOnConstraintAnnotation() {
		Validator validator = ValidatorUtil.getValidator();
		EmailContainer container = new EmailContainerAllowTldAnnotated();
		container.setEmail( "user@com" );
		assertTrue( validator.validate( container ).isEmpty() );
	}

	@Test
	public void testAllowTldWithProgrammaticConfiguration() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( EmailContainer.class )
				.getter( "email" )
				.constraint( new EmailDef().allowTld( true ) );
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		EmailContainer container = new EmailContainerNoAnnotations();
		container.setEmail( "admin@org" );
		assertTrue( validator.validate( container ).isEmpty() );
	}

	@Test
	public void testAllowTldWithProgrammaticConfigurationStillHonorsRegexp() {
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( EmailContainer.class )
				.getter( "email" )
				.constraint(
						new EmailDef().allowTld( true )
								.regexp( noOrgEmailAddressRegexp )
								.message( "ORG addresses are not valid" )
				);
		config.addMapping( mapping );

		Validator validator = config.buildValidatorFactory().getValidator();
		EmailContainer container = new EmailContainerNoAnnotations();
		container.setEmail( "admin@org" );
		assertOrgAddressesAreNotValid( validator.validate( container ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-591")
	public void testEmailAddressLength() {
		isValidEmail( "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa@hibernate.org" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-810")
	public void testEMailWithTrailingAt() throws Exception {
		isInvalidEmail( "validation@hibernate.com@" );
		isInvalidEmail( "validation@hibernate.com@@" );
		isInvalidEmail( "validation@hibernate.com@@@" );
	}

	@Test
	@TestForIssue(jiraKey = { "HV-1005", "HV-1066" })
	public void testEmailWithUpTo64CharacterLocalPartIsValid() {
		for ( int length = 1; length <= 64; length++ ) {
			isValidEmail( stringOfLength( length ) + "@foo.com" );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-1005")
	public void testEmailWith65CharacterLocalPartIsInvalid() {
		isInvalidEmail( stringOfLength( 65 ) + "@foo.com" );
	}

	@Test
	@TestForIssue(jiraKey = { "HV-1005", "HV-1066" })
	public void testEmailWithUpTo255CharacterDomainPartIsValid() {
		for ( int length = 1; length <= 251; length++ ) {
			isValidEmail( "foo@" + domainOfLength( length ) + ".com" );
		}
	}

	@Test
	@TestForIssue(jiraKey = { "HV-1005", "HV-1066" })
	public void testEmailWith63CharactersDomainPartIsValid() {
		isValidEmail( "foo@" + stringOfLength( 63 ) + "." + stringOfLength( 63 ) + ".com" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1005")
	public void testEmailWith256CharacterDomainPartIsInvalid() {
		isInvalidEmail( "foo@" + domainOfLength( 252 ) + ".com" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1833")
	public void testLongEmail() {
		assertEquals( false, DomainNameUtil.isValidEmailDomainAddress( stringOfLength( 5000 ) + ".com" ) );
	}

	private String stringOfLength(int length) {
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < length; i++ ) {
			builder.append( 'a' );
		}
		String s = builder.toString();
		assertEquals( s.getBytes().length, length );
		return s;
	}

	private String domainOfLength(int length) {
		StringBuilder builder = new StringBuilder();
		for ( int i = 0; i < length; i++ ) {
			if ( i % 32 == 0 && i > 0 && i < length - 1 ) {
				builder.append( "." );
			}
			else {
				builder.append( 'a' );
			}
		}
		String s = builder.toString();
		assertEquals( s.getBytes().length, length );
		return s;
	}

	private EmailValidator newInitializedValidator(boolean allowTld) {
		EmailValidator emailValidator = new EmailValidator();
		ConstraintAnnotationDescriptor.Builder<Email> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Email.class );
		descriptorBuilder.setAttribute( "allowTld", allowTld );
		emailValidator.initialize( descriptorBuilder.build().getAnnotation() );
		return emailValidator;
	}

	private void assertOrgAddressesAreNotValid(Set<ConstraintViolation<EmailContainer>> violations) {
		assertThat( violations ).containsOnlyViolations(
				violationOf( Email.class ).withMessage( "ORG addresses are not valid" )
		);
	}

	private void isValidEmail(CharSequence email, String message) {
		assertTrue( validator.isValid( email, null ), StringHelper.format( message, email ) );
	}

	private void isValidEmail(CharSequence email) {
		isValidEmail( email, "Expected %1$s to be a valid email." );
	}

	private void isInvalidEmail(CharSequence email, String message) {
		assertFalse( validator.isValid( email, null ), StringHelper.format( message, email ) );
	}

	private void isInvalidEmail(CharSequence email) {
		isInvalidEmail( email, "Expected %1$s to be an invalid email." );
	}

	@SuppressWarnings("unused")
	private abstract static class EmailContainer {
		public String email;

		public void setEmail(String email) {
			this.email = email;
		}

		public String getEmail() {
			return email;
		}
	}

	private static class EmailContainerAnnotated extends EmailContainer {
		@Override
		@Email(regexp = EmailValidatorTest.noOrgEmailAddressRegexp, message = "ORG addresses are not valid")
		public String getEmail() {
			return email;
		}
	}

	private static class EmailContainerAllowTldAnnotated extends EmailContainer {
		@Override
		@Email(allowTld = true)
		public String getEmail() {
			return email;
		}
	}

	private static class EmailContainerNoAnnotations extends EmailContainer {
	}
}
