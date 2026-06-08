/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.spi.nodenameprovider;

import org.hibernate.validator.Incubating;

@Incubating
public interface RecordComponentProperty extends Property {

	Class<?> getDeclaringClass();
}
