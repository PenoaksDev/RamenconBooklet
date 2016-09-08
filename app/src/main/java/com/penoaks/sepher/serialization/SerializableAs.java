/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * <p/>
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.penoaks.sepher.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents an "alias" that a {@link ConfigurationSerializable} may be stored as.
 * If this is not present on a {@link ConfigurationSerializable} class, it will use the
 * fully qualified name of the class.
 * <p />
 * This value will be stored in the configuration so that the configuration deserialization can determine what type it is.
 * <p />
 * Using this annotation on any other class than a {@link ConfigurationSerializable} will have no effect.
 *
 * @see ConfigurationSerialization#registerClass(Class, String)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializableAs
{
	/**
	 * This is the name your class will be stored and retrieved as.
	 * <p />
	 * This name MUST be unique. We recommend using names such as "MyClassThing" instead of "Thing".
	 *
	 * @return Name to serialize the class as.
	 */
	String value();
}
