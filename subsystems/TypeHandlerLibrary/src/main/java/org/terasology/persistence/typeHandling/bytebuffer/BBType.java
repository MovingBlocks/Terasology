// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

/**
 * Type codes for array types.
 */
public enum BBType {
    NULL(0),
    BOOLEAN(1),
    FLOAT(2),
    DOUBLE(3),
    LONG(4),
    INTEGER(5),
    STRING(6),
    BYTES(7),
    BYTEBUFFER(8),
    BOOLEAN_ARRAY(9),
    FLOAT_ARRAY(10),
    DOUBLE_ARRAY(11),
    LONG_ARRAY(12),
    INTEGER_ARRAY(13),
    STRING_ARRAY(14),
    VALUE_ARRAY(15),
    VALUEMAP(16);

    private final byte code;

    BBType(int code) {
        this.code = (byte) code;
    }

    public static BBType parse(byte code) {
        for (BBType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }


    public BBType getPrimitiveType() {
        switch (this) {
            case BOOLEAN_ARRAY:
                return BBType.BOOLEAN;
            case FLOAT_ARRAY:
                return BBType.FLOAT;
            case DOUBLE_ARRAY:
                return BBType.DOUBLE;
            case LONG_ARRAY:
                return BBType.LONG;
            case INTEGER_ARRAY:
                return BBType.INTEGER;
            case STRING_ARRAY:
                return BBType.STRING;
        }
        return null;
    }

    public boolean isArray() {
        switch (this) {
            case BOOLEAN_ARRAY:
            case FLOAT_ARRAY:
            case DOUBLE_ARRAY:
            case LONG_ARRAY:
            case INTEGER_ARRAY:
            case STRING_ARRAY:
            case VALUE_ARRAY:
                return true;
            default:
                return false;
        }
    }

    public byte getCode() {
        return code;
    }
}
