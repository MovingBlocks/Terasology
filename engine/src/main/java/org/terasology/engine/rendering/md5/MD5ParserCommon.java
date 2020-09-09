// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.md5;

import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;

/**
 */
public final class MD5ParserCommon {

    public static final Matrix3f CORRECTION_MATRIX;
    public static final Quat4f CORRECTION_QUATERNION;

    private MD5ParserCommon() {
    }

    static {
        CORRECTION_MATRIX = new Matrix3f(-1, 0, 0, 0, 0, 1, 0, 1, 0);
        CORRECTION_QUATERNION = new Quat4f(0, 0, 0, 1);
        CORRECTION_QUATERNION.set(CORRECTION_MATRIX);
    }

    public static Vector2f readUV(String u, String v) throws NumberFormatException {
        return new Vector2f(Float.parseFloat(u), Float.parseFloat(v));
    }

    public static Vector3f readVector3f(String x, String y, String z) throws NumberFormatException {
        return new Vector3f(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
    }

    public static Vector3f readVector3fAndCorrect(String x, String y, String z) throws NumberFormatException {
        Vector3f result = new Vector3f(Float.parseFloat(x), Float.parseFloat(y), Float.parseFloat(z));
        CORRECTION_MATRIX.transform(result);
        return result;
    }

    public static Quat4f readQuat4f(String xValue, String yValue, String zValue) throws NumberFormatException {
        float x = Float.parseFloat(xValue);
        float y = Float.parseFloat(yValue);
        float z = Float.parseFloat(zValue);
        return correctQuat4f(completeQuat4f(x, y, z));
    }

    public static Quat4f completeQuat4f(float x, float y, float z) {
        float t = 1.0f - (x * x) - (y * y) - (z * z);
        float w = 0;
        if (t > 0.0f) {
            w = (float) -Math.sqrt(t);
        }
        Quat4f result = new Quat4f(x, y, z, w);
        result.normalize();
        return result;
    }

    public static Quat4f correctQuat4f(Quat4f rot) {
        Quat4f result = new Quat4f(CORRECTION_QUATERNION);
        result.mul(rot);
        return result;
    }

    public static Vector3f correctOffset(Vector3f offset) {
        return CORRECTION_QUATERNION.rotate(offset, new Vector3f());
    }


    public static String readToLine(BufferedReader reader, String startsWith) throws IOException {
        String line = readNextLine(reader);
        while (line != null && !line.trim().startsWith(startsWith)) {
            line = readNextLine(reader);
        }
        if (line == null) {
            throw new IOException("Failed to find expected line: \"" + startsWith + "\"");
        }
        return line;
    }

    public static String readNextLine(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        while (line != null && (line.isEmpty() || line.trim().startsWith("//"))) {
            line = reader.readLine();
        }
        return line;
    }
}
