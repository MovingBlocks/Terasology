package org.terasology.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

/**
 * @author Immortius <immortius@gmail.com>
 * @author Florian <florian@fkoeberle.de>.

 */
public class OpenGLUtil {

    private OpenGLUtil() {
        // Utility class, no instance required
    }

    /**
     * Removes the rotation and scale part of the current OpenGL matrix.
     * Can be used to render billboards like particles.
     */
    public static void applyBillboardOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    model.put(i * 4 + j, 1.0f);
                } else {
                    model.put(i * 4 + j, 0.0f);
                }
            }
        }

        GL11.glLoadMatrix(model);
    }
}
