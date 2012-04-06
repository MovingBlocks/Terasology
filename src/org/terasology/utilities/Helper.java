/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.utilities;

import org.lwjgl.BufferUtils;

import javax.vecmath.Matrix4f;
import java.io.File;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.glGetFloat;

/**
 * A simple helper class for various tasks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class Helper {

    // Prevent instantiation
    private Helper() {
    }

    /**
     * Returns true if the flag at the given byte position
     * is set.
     *
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return True if the flag is set
     */
    public static boolean isFlagSet(byte value, short index) {
        return (value & (1 << index)) != 0;
    }

    /**
     * Sets a flag at a given byte position.
     *
     * @param value Byte value storing the flags
     * @param index Index position of the flag
     * @return The byte value containing the modified flag
     */
    public static byte setFlag(byte value, short index) {
        return (byte) (value | (1 << index));
    }

    public static void readMatrix(int type, Matrix4f target) {
        FloatBuffer matrix = BufferUtils.createFloatBuffer(16);
        glGetFloat(type, matrix);

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                target.setElement(j, i, matrix.get());
    }

    public static FloatBuffer matrixToBuffer(Matrix4f mat) {
        FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                matrixBuffer.put(mat.getElement(j, i));

        matrixBuffer.flip();
        return matrixBuffer;
    }

    /**
     * Simple and somewhat hacky way to determine if running from source (which should be out of a "Terasology" dir)
     * or from jar file / applet in which case we might want to use the user's temp dir instead for saving stuff
     * @param f the file to check and fix path for
     * @return the file with the path fixed if needed
    */
    public static File fixSavePath(File f) {
        // Terasology.getInstance().getLogger().log(Level.INFO, "Suggested absolute save path is: " + f.getAbsolutePath());
        if (!f.getAbsolutePath().contains("Terasology")) {
            f = new File(System.getProperty("java.io.tmpdir"), f.getPath());
            // Terasology.getInstance().getLogger().log(Level.INFO, "Going to use absolute TEMP save path instead: " + f.getAbsolutePath());
            return f;
        }
        return f;
    }
}
