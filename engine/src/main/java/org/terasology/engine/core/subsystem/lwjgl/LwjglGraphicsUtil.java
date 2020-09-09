// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.terasology.engine.rendering.assets.texture.Texture;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

/**
 *
 */
public final class LwjglGraphicsUtil {
    private LwjglGraphicsUtil() {
    }

    public static int getGLMode(Texture.WrapMode mode) {
        switch (mode) {
            case CLAMP:
                return GL_CLAMP;
            case REPEAT:
                return GL_REPEAT;
            default:
                throw new RuntimeException("Unsupported WrapMode '" + mode + "'");
        }
    }

    public static int getGlMinFilter(Texture.FilterMode mode) {
        switch (mode) {
            case LINEAR:
                return GL_LINEAR_MIPMAP_LINEAR;
            case NEAREST:
                return GL_NEAREST_MIPMAP_NEAREST;
            default:
                throw new RuntimeException("Unsupported FilterMode '" + mode + "'");
        }
    }

    public static int getGlMagFilter(Texture.FilterMode filterMode2) {
        switch (filterMode2) {
            case LINEAR:
                return GL_LINEAR;
            case NEAREST:
                return GL_NEAREST;
            default:
                throw new RuntimeException("Unsupported FilterMode '" + filterMode2 + "'");
        }
    }
}
