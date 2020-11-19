// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;


import java.nio.ByteBuffer;

/**
 */
public interface PersistedData {

    /**
     * Retrieves this data as a String value. This requires that the value is a string, or is an array of size == 1 containing a string
     *
     * @return this data as a string value.
     * @throws ClassCastException    if the data is not a valid string value.
     * @throws IllegalStateException if the element is an array of size != 1
     */
    String getAsString();

    /**
     * Retrieves this data as a double. This requires that the value is numeric, or is an array of size == 1 containing a numeric value
     *
     * @return this data as a double
     * @throws ClassCastException    if the data is not a valid number.
     * @throws IllegalStateException If the data is an array of size != 1
     */
    double getAsDouble();

    /**
     * Retrieves this data as a float. This requires that the value is numeric, or is an array of size == 1 containing a numeric value
     *
     * @return this data as a float
     * @throws ClassCastException    if the data is not a valid number.
     * @throws IllegalStateException If the data is an array of size != 1
     */
    float getAsFloat();

    /**
     * Retrieves this data as an integer. This requires that the value is numeric, or is an array of size == 1 containing a numeric value
     *
     * @return this data as an integer
     * @throws ClassCastException    if the data is not a valid number.
     * @throws IllegalStateException If the data is an array of size != 1
     */
    int getAsInteger();

    /**
     * Retrieves this data as a long. This requires that the value is numeric, or is an array of size == 1 containing a numeric value
     *
     * @return this data as a long
     * @throws ClassCastException    if the data is not a valid number.
     * @throws IllegalStateException If the data is an array of size != 1
     */
    long getAsLong();

    /**
     * Retrieves this data as a boolean value. This requires that the value is a boolean, or is an array of size == 1 containing a boolean
     *
     * @return this data as a boolean value.
     * @throws ClassCastException    if the data is not boolean
     * @throws IllegalStateException if the data is an array of size != 1
     */
    boolean getAsBoolean();

    /**
     * Retrieves this data as a byte array. This requires that the data is bytes.
     *
     * @return this data as a bytes value
     * @throws DeserializationException if the data is not bytes
     */
    byte[] getAsBytes();

    /**
     * Retrieves this data as a ByteBuffer. This requires that the data is bytes.
     *
     * @return this data as a boolean value
     * @throws ClassCastException if the data is not bytes
     */
    ByteBuffer getAsByteBuffer();

    /**
     * Retrieves this data as a list of PersistedData. This requires that the data is an array.
     *
     * @return The contents of this value
     * @throws IllegalStateException if this data is not an array
     */
    PersistedDataArray getAsArray();

    /**
     * Retrieves this data as a map of names to PersistedData (values). This requires that data is a value map.
     *
     * @return The contents of this value
     * @throws java.lang.IllegalStateException if this data is not a value map
     */
    PersistedDataMap getAsValueMap();

    boolean isString();

    boolean isNumber();

    boolean isBoolean();

    boolean isBytes();

    boolean isArray();

    boolean isValueMap();

    boolean isNull();

}
