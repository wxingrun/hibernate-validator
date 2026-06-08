package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import jakarta.validation.Validator;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class EmailValidatorTest {

	private Validator validator;

	@BeforeClass
	public void setUp() {
		validator = ValidatorUtil.getValidator();
	}

	@Test
	public void testEmailWithTldNotAllowed() {
		EmailContainer container = new EmailContainer();
		
		container.email = "user@com";
		assertFalse( validator.validate( container ).isEmpty(), "user@com should be invalid" );

		container.email = "admin@org";
		assertFalse( validator.validate( container ).isEmpty(), "admin@org should be invalid" );

		container.email = "valid@example.com";
		assertTrue( validator.validate( container ).isEmpty(), "valid@example.com should be valid" );
		
		container.email = "user@localhost";
		assertFalse( validator.validate( container ).isEmpty(), "user@localhost should be invalid when allowTld is false" );
	}

	@Test
	public void testEmailWithTldAllowed() {
		EmailContainerAllowTld container = new EmailContainerAllowTld();
		
		container.email = "user@com";
		assertTrue( validator.validate( container ).isEmpty(), "user@com should be valid" );

		container.email = "admin@org";
		assertTrue( validator.validate( container ).isEmpty(), "admin@org should be valid" );

		container.email = "valid@example.com";
		assertTrue( validator.validate( container ).isEmpty(), "valid@example.com should be valid" );
		
		container.email = "user@localhost";
		assertTrue( validator.validate( container ).isEmpty(), "user@localhost should be valid when allowTld is true" );
	}

	@Test
	public void testIpAddressAllowed() {
		EmailContainer container = new EmailContainer();
		container.email = "user@[192.168.1.1]";
		assertTrue( validator.validate( container ).isEmpty(), "IP address domain should be valid" );
		
		EmailContainerAllowTld containerAllowTld = new EmailContainerAllowTld();
		containerAllowTld.email = "user@[192.168.1.1]";
		assertTrue( validator.validate( containerAllowTld ).isEmpty(), "IP address domain should be valid" );
	}

	private static class EmailContainer {
		@Email
		public String email;
	}

	private static class EmailContainerAllowTld {
		@Email(allowTld = true)
		public String email;
	}
}