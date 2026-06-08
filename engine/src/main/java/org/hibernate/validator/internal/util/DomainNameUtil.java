/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.util;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

import java.net.IDN;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public final class DomainNameUtil {

	private static final int MAX_DOMAIN_PART_LENGTH = 255;

	private static final String DOMAIN_CHARS_WITHOUT_DASH = "[a-z\u0080-\uFFFF0-9!#$%&'*+/=?^_`{|}~]";
	private static final String DOMAIN_LABEL = DOMAIN_CHARS_WITHOUT_DASH + "++(?:-++" + DOMAIN_CHARS_WITHOUT_DASH + "++)*+";
	private static final String DOMAIN = DOMAIN_LABEL + "(?:\\." + DOMAIN_LABEL + ")*+";

	private static final String IP_DOMAIN = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";
	private static final String IP_V6_DOMAIN =
			"(?:(?:[0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,7}:|(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|(?:[0-9a-fA-F]{1,4}:){1,3}(?::[0-9a-fA-F]{1,4}){1,4}|(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})|:(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(?:ffff(:0{1,4}){0,1}:){0,1}(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])|(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9]))";

	private static final Pattern DOMAIN_PATTERN = Pattern.compile(
			DOMAIN + "|\\[" + IP_V6_DOMAIN + "\\]", CASE_INSENSITIVE
	);

	private static final Pattern EMAIL_DOMAIN_PATTERN = Pattern.compile(
			DOMAIN + "|\\[" + IP_DOMAIN + "\\]|" + "\\[IPv6:" + IP_V6_DOMAIN + "\\]", CASE_INSENSITIVE
	);

	private static final Set<String> SINGLE_LABEL_TOP_LEVEL_EMAIL_DOMAINS = Set.of(
			"aero",
			"arpa",
			"asia",
			"biz",
			"cat",
			"com",
			"coop",
			"edu",
			"gov",
			"info",
			"int",
			"jobs",
			"mil",
			"mobi",
			"museum",
			"name",
			"net",
			"org",
			"post",
			"pro",
			"tel",
			"travel",
			"xxx"
	);

	private DomainNameUtil() {
	}

	public static boolean isValidEmailDomainAddress(String domain) {
		return isValidEmailDomainAddress( domain, false );
	}

	public static boolean isValidEmailDomainAddress(String domain, boolean allowTld) {
		if ( !isValidDomainAddress( domain, EMAIL_DOMAIN_PATTERN ) ) {
			return false;
		}

		if ( allowTld || domain.indexOf( '.' ) >= 0 || isIpDomain( domain ) ) {
			return true;
		}

		String asciiString = toAscii( domain );
		return asciiString != null && !SINGLE_LABEL_TOP_LEVEL_EMAIL_DOMAINS.contains( asciiString.toLowerCase( Locale.ROOT ) );
	}

	public static boolean isValidDomainAddress(String domain) {
		return isValidDomainAddress( domain, DOMAIN_PATTERN );
	}

	private static boolean isValidDomainAddress(String domain, Pattern pattern) {
		if ( domain.endsWith( "." ) ) {
			return false;
		}

		String asciiString = toAscii( domain );
		if ( asciiString == null || asciiString.length() > MAX_DOMAIN_PART_LENGTH ) {
			return false;
		}

		Matcher matcher = pattern.matcher( domain );
		return matcher.matches();
	}

	private static boolean isIpDomain(String domain) {
		return domain.startsWith( "[" ) && domain.endsWith( "]" );
	}

	private static String toAscii(String domain) {
		try {
			return IDN.toASCII( domain );
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
}
