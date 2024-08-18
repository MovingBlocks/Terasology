// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.lwjgl;

import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_HIGH;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_LOW;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_MEDIUM;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SEVERITY_NOTIFICATION;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_API;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_APPLICATION;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_OTHER;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_SHADER_COMPILER;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_THIRD_PARTY;
import static org.lwjgl.opengl.GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_ERROR;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_MARKER;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_OTHER;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_PERFORMANCE;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_POP_GROUP;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_PORTABILITY;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_PUSH_GROUP;
import static org.lwjgl.opengl.GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR;

/**
 * Callback used by the OpenGL driver to output additional debug information about our use of the API.
 */
public class DebugCallback implements org.lwjgl.opengl.GLDebugMessageCallbackI {

    private static final Logger logger = LoggerFactory.getLogger("OpenGL");

    @Override
    public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
        String logFormat = "[{}] [{}] [{}] {}";
        String idString = "0x" + Integer.toHexString(id).toUpperCase(Locale.ROOT);
        String sourceString = getSourceString(source);
        String typeString = getTypeString(type);
        String messageString = MemoryUtil.memASCII(message).trim();

        switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH:
                logger.error(logFormat, idString, sourceString, typeString, messageString);
                break;
            case GL_DEBUG_SEVERITY_MEDIUM:
                logger.warn(logFormat, idString, sourceString, typeString, messageString);
                break;
            case GL_DEBUG_SEVERITY_LOW:
                logger.debug(logFormat, idString, sourceString, typeString, messageString);
                break;
            default:
            case GL_DEBUG_SEVERITY_NOTIFICATION:
                logger.trace(logFormat, idString, sourceString, typeString, messageString);
                break;
        }
    }

    private static String getSourceString(int source) {
        switch (source) {
            case GL_DEBUG_SOURCE_API:
                return "api";
            case GL_DEBUG_SOURCE_WINDOW_SYSTEM:
                return "window system";
            case GL_DEBUG_SOURCE_SHADER_COMPILER:
                return "shader compiler";
            case GL_DEBUG_SOURCE_THIRD_PARTY:
                return "third party";
            case GL_DEBUG_SOURCE_APPLICATION:
                return "app";
            default:
            case GL_DEBUG_SOURCE_OTHER:
                return "other";
        }
    }

    private static String getTypeString(int type) {
        switch (type) {
            case GL_DEBUG_TYPE_ERROR:
                return "error";
            case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
                return "deprecated";
            case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
                return "undefined behaviour";
            case GL_DEBUG_TYPE_PORTABILITY:
                return "portability";
            case GL_DEBUG_TYPE_PERFORMANCE:
                return "performance";
            case GL_DEBUG_TYPE_MARKER:
                return "marker";
            case GL_DEBUG_TYPE_PUSH_GROUP:
                return "pushGroup";
            case GL_DEBUG_TYPE_POP_GROUP:
                return "popGroup";
            default:
            case GL_DEBUG_TYPE_OTHER:
                return "other";
        }
    }

}
