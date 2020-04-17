/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.md5;

import org.joml.Matrix3f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;

/**
 */
public final class MD5ParserCommon {

    public static final Matrix3f CORRECTION_MATRIX;
    public static final Quaternionf CORRECTION_QUATERNION;

    private MD5ParserCommon() {
    }

    static {
        CORRECTION_MATRIX = new Matrix3f(-1, 0, 0, 0, 0, 1, 0, 1, 0);
        CORRECTION_QUATERNION = new Quaternionf();
        CORRECTION_MATRIX.getNormalizedRotation(CORRECTION_QUATERNION);
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

    public static Quaternionf readQuat4f(String xValue, String yValue, String zValue) throws NumberFormatException {
        float x = Float.parseFloat(xValue);
        float y = Float.parseFloat(yValue);
        float z = Float.parseFloat(zValue);
        return correctQuat4f(completeQuat4f(x, y, z));
    }

    public static Quaternionf completeQuat4f(float x, float y, float z) {
        float t = 1.0f - (x * x) - (y * y) - (z * z);
        float w = 0;
        if (t > 0.0f) {
            w = (float) -Math.sqrt(t);
        }
        return new Quaternionf(x, y, z, w).normalize();
    }

    public static Quaternionf correctQuat4f(Quaternionf rot) {
        return new Quaternionf(CORRECTION_QUATERNION).mul(rot);
    }

    public static Vector3f correctOffset(Vector3f offset) {
        return new Vector3f(offset).rotate(CORRECTION_QUATERNION);
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
