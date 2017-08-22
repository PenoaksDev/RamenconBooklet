/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package io.amelia.android.configuration.serialization;

import java.util.Map;

/**
 * Represents an object that may be serialized.
 * <p />
 * These objects MUST implement one of the following, in addition to the methods as defined by this interface:
 * <ul>
 * <li>A static method "deserialize" that accepts a single {@link Map}&lt;{@link String}, {@link Object}> and returns the class.</li>
 * <li>A static method "valueOf" that accepts a single {@link Map}&lt;{@link String}, {@link Object}> and returns the class.</li>
 * <li>A constructor that accepts a single {@link Map}&lt;{@link String}, {@link Object}>.</li>
 * </ul>
 * In addition to implementing this interface, you must register the class with {@link ConfigurationSerialization#registerClass(Class)}.
 *
 * @see DelegateDeserialization
 * @see SerializableAs
 */
public interface ConfigurationSerializable
{
	/**
	 * Creates a Map representation of this class.
	 * <p />
	 * This class must provide a method to restore this class, as defined in the {@link ConfigurationSerializable} interface javadocs.
	 *
	 * @return Map containing the current state of this class
	 */
	Map<String, Object> serialize();
}
