/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling;

import gnu.trove.iterator.TDoubleIterator;
import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public interface SerializationContext {

    /**
     * Serializes a single string
     * @param value
     * @return A serialized string
     */
    PersistedData create(String value);

    /**
     * Serializes an array of strings.
     * @param values
     * @return A serialized array of strings.
     */
    PersistedData create(String... values);

    /**
     * Serializes a collection of strings. Null values will be stripped.
     * @param value
     * @return A serialized array of strings
     */
    PersistedData createStrings(Iterable<String> value);

    /**
     * Serializes a single float
     * @param value
     * @return A serialized float
     */
    PersistedData create(float value);

    /**
     * Serializes an array of floats
     * @param values
     * @return A serialized array of floats
     */
    PersistedData create(float... values);

    /**
     * Serializes a collection of floats
     * @param value
     * @return A serialized array of floats
     */
    PersistedData create(TFloatIterator value);

    /**
     * Serializes a single integer
     * @param value
     * @return A serialized integer
     */
    PersistedData create(int value);

    /**
     * Serializes an array of integers
     * @param values
     * @return A serialized array of integers
     */
    PersistedData create(int... values);

    /**
     * Serializes a collection of integers
     * @param value
     * @return A serialized array of integers
     */
    PersistedData create(TIntIterator value);

    /**
     * Serializes a single long
     * @param value
     * @return A serialized long
     */
    PersistedData create(long value);

    /**
     * Serializes an array of longs
     * @param values
     * @return A serialized array of longs
     */
    PersistedData create(long... values);

    /**
     * Serializes a collection of longs
     * @param value
     * @return A serialized array of longs
     */
    PersistedData create(TLongIterator value);

    /**
     * Serializes a single boolean
     * @param value
     * @return A serialized boolean
     */
    PersistedData create(boolean value);

    /**
     * Serializes an array of booleans
     * @param values
     * @return A serialized array of booleans
     */
    PersistedData create(boolean... values);

    /**
     * Serializes a single double
     * @param value
     * @return A serialized double
     */
    PersistedData create(double value);

    /**
     * Serializes an array of doubles
     * @param values
     * @return A serialized array double
     */
    PersistedData create(double... values);

    /**
     * Serializes a collection of doubles
     * @param value
     * @return A serialized array of double
     */
    PersistedData create(TDoubleIterator value);

    /**
     * Serializes an array of bytes
     * @param value
     * @return Serialized bytes
     */
    PersistedData create(byte[] value);

    /**
     * Serializes a buffer of bytes
     * @param value
     * @return Serialized bytes
     */
    PersistedData create(ByteBuffer value);

    /**
     * Serializes an array of values
     * @param values
     * @return
     */
    PersistedData create(PersistedData... values);

    /**
     * Serializes a collection of values
     * @param data
     * @return
     */
    PersistedData create(Iterable<PersistedData> data);

    /**
     * Serializes a map of name-value pairs
     * @param data
     * @return
     */
    PersistedData create(Map<String, PersistedData> data);

    /**
     * @return A 'null' PersistedData
     */
    PersistedData createNull();
}
