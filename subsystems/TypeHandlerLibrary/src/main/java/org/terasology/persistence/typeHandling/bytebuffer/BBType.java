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
    ARRAY(9),
    VALUEMAP(10);

    private final byte code;

    BBType(int code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
    }

    public static BBType parse(byte code) {
        for (BBType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
