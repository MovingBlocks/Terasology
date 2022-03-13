// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.terasology.engine.core.subsystem.DisplayDeviceInfo;
import org.terasology.engine.rendering.assets.texture.Texture;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glEnable;


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

    /**
     * Converting BufferedImage to GLFWImage.
     *
     * @param image image to convert
     * @return convertedImage
     */
    public static GLFWImage convertToGLFWFormat(BufferedImage image) {
        BufferedImage convertedImage;
        if (image.getType() != BufferedImage.TYPE_INT_ARGB_PRE) {
            convertedImage = new BufferedImage(image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_INT_ARGB_PRE);
            final Graphics2D graphics = convertedImage.createGraphics();
            final int targetWidth = image.getWidth();
            final int targetHeight = image.getHeight();
            graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
            graphics.dispose();
        }
        final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                int colorSpace = image.getRGB(j, i);
                buffer.put((byte) ((colorSpace << 8) >> 24));
                buffer.put((byte) ((colorSpace << 16) >> 24));
                buffer.put((byte) ((colorSpace << 24) >> 24));
                buffer.put((byte) (colorSpace >> 24));
            }
        }
        buffer.flip();
        final GLFWImage result = GLFWImage.create();
        result.set(image.getWidth(), image.getHeight(), buffer);
        return result;
    }

    public static void initOpenGLParams() {
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
    }

    public static void checkOpenGL() {
        GLCapabilities capabilities = GL.createCapabilities();
        boolean[] requiredCapabilities = {
                capabilities.OpenGL12,
                capabilities.OpenGL14,
                capabilities.OpenGL15,
                capabilities.OpenGL20,
                capabilities.OpenGL21,
                capabilities.OpenGL30,
                capabilities.OpenGL32,
                capabilities.OpenGL33,
        };

        String[] capabilityNames = {
                "OpenGL12",
                "OpenGL14",
                "OpenGL15",
                "OpenGL20",
                "OpenGL21",
                "OpenGL30",
                "OpenGL32",
                "OpenGL33",
        };

        boolean canRunTheGame = true;
        StringBuilder missingCapabilitiesMessage = new StringBuilder();

        for (int index = 0; index < requiredCapabilities.length; index++) {
            if (!requiredCapabilities[index]) {
                missingCapabilitiesMessage.append("    - ").append(capabilityNames[index]).append("\n");
                canRunTheGame = false;
            }
        }

        if (!canRunTheGame) {
            String completeErrorMessage = completeErrorMessage(missingCapabilitiesMessage.toString());
            throw new IllegalStateException(completeErrorMessage);
        }
    }

    private static String completeErrorMessage(String errorMessage) {
        return "\n" +
                "\nThe following OpenGL versions/extensions are required but are not supported by your GPU driver:\n" +
                "\n" +
                errorMessage +
                "\n" +
                "GPU Information:\n" +
                "\n" +
                "    Vendor:  " + GL11.glGetString(GL11.GL_VENDOR) + "\n" +
                "    Model:   " + GL11.glGetString(GL11.GL_RENDERER) + "\n" +
                "    Driver:  " + GL11.glGetString(GL11.GL_VERSION) + "\n" +
                "\n" +
                "Try updating the driver to the latest version available.\n" +
                "If that fails you might need to use a different GPU (graphics card). Sorry!\n";
    }

    public static void updateDisplayDeviceInfo(DisplayDeviceInfo deviceInfo) {
        deviceInfo.setOpenGlVendor(GL11.glGetString(GL11.GL_VENDOR));
        deviceInfo.setOpenGlVersion(GL11.glGetString(GL11.GL_VERSION));
        deviceInfo.setOpenGlRenderer(GL11.glGetString(GL11.GL_RENDERER));
    }
}
