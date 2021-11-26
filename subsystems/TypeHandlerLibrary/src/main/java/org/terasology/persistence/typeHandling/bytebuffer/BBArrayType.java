// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.bytebuffer;

/**
 * Type codes for types.
 */
public enum BBArrayType {
    BOOLEAN(0),
    FLOAT(1),
    DOUBLE(2),
    LONG(3),
    INTEGER(4),
    STRING(5),
    VALUE(6);

    private final byte code;

    BBArrayType(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
    }

    public BBType getPrimitiveType() {
        switch (this) {
            case BOOLEAN:
                return BBType.BOOLEAN;
            case FLOAT:
                return BBType.FLOAT;
            case DOUBLE:
                return BBType.DOUBLE;
            case LONG:
                return BBType.LONG;
            case INTEGER:
                return BBType.INTEGER;
            case STRING:
                return BBType.STRING;
        }
        return null;
    }

    public static BBArrayType parse(byte code) {
        for (BBArrayType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
