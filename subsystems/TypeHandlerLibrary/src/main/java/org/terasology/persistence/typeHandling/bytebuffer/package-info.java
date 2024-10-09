// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

/**
 * ByteBuffer serializer provide possible to read and write bytebuffer directly.
 * <p>
 * <pre>
 *         Format:
 *         Types(BBType):
 *            NULL(0) - size = 1 byte, format = type(byte value = 0)
 *            BOOLEAN(1) - size = 2 bytes, format = type(byte value = 1) + value(1 byte). ( yeah a bit not optimal)
 *            FLOAT(2) - size = 5 bytes, format = type(byte value = 2) + value(1 float = 4 bytes)
 *            DOUBLE(3) - size = 9 bytes, format = type(byte value = 3) + value(1 double = 8 bytes)
 *            LONG(4) -  size = 9 bytes, format = type(byte value = 4) + value(1 long = 8 bytes)
 *            INTEGER(5) - size = 5 bytes, format = type(byte value = 5) + value(1 int = 4 bytes)
 *            STRING(6) - size = 5..n bytes, format =  type(byte value = 6) + size(1 int = 4 bytes) + value($size bytes)
 *            BYTES(7) - size = 5..n bytes, format =  type(byte value = 7) + size(1 int = 4 bytes) + value($size bytes)
 *            BYTEBUFFER(8) - size = 5..n bytes, format =  type(byte value = 8) + size(1 int = 4 bytes) + value($size bytes)
 *            BOOLEAN(9) - size = 5..n bytes.
 *                       format = type(byte value = 9) + size(1 int = 4 bytes) + data(($size % 8 + 1) bytes)
 *            FLOAT(10) - size = 5 .. n bytes.
 *                       format = type(byte value = 10) + size(1 int = 4 bytes) + data($size floats = $size * 4 bytes)
 *            DOUBLE(11) - size = 5 .. n bytes.
 *                       format = type(byte value = 11) + size(1 int = 4 bytes) + data($size double = $size * 8 bytes)
 *            LONG(12) - size = 5 .. n bytes.
 *                       format = type(byte value = 12) + size(1 int = 4 bytes) + data($size longs = $size * 8 bytes)
 *            INTEGER(13) - size = 5 .. n bytes.
 *                       format = type(byte value = 13) + size(1 int = 4 bytes) + data($size ints = $size * 4 bytes)
 *            STRING(14) - size = 5 .. n bytes.
 *                       format = type(byte value = 14) +
 *                                size(1 int = 4 bytes) +
 *                                sizeArray(format = STRING(6).size = 1 int * $size = 4 byte * $size) +
 *                                stringdata(format = (STRING(6) - 1 byte($STRING.type)) * $size)
 *            VALUE(15) - size = 5 .. n bytes.
 *                       format = type(byte value = 15) +
 *                                size(1 int = 4 bytes) +
 *                                sizeArray(format = (any BBType whole size) = 1 int * $size = 4 byte * $size) +
 *                                stringdata(format = (any BBType) * $size)
 *            VALUEMAP(16) -
 *                  size = 5..n bytes.
 *                  format =  type(byte value = 16) +
 *                            size(1 int = 4 bytes) +
 *                            refmap(format = (keyRef(1 int) + valueRef(1 int)) * $size) +
 *                            keydata(format = (STRING(6) - 1 byte($STRING.type) * $size ) +
 *                            valuedata(format = (any BBType) * $size)
 *
 *     </pre>
 */
package org.terasology.persistence.typeHandling.bytebuffer;
