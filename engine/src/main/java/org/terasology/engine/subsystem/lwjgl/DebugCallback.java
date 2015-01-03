/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.engine.subsystem.lwjgl;

import org.lwjgl.opengl.KHRDebugCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
class DebugCallback implements KHRDebugCallback.Handler {

    private static final Logger logger = LoggerFactory.getLogger("OpenGL");

    @Override
    public void handleMessage(int source, int type, int id, int severity, String message) {
        String logFormat = "[{}] [{}] {}";
        Object[] args = new Object[]{getSourceString(source), getTypeString(type), message};

        switch (severity) {
            case GL_DEBUG_SEVERITY_HIGH:
                logger.error(logFormat, args);
                break;
            case GL_DEBUG_SEVERITY_MEDIUM:
                logger.warn(logFormat, args);
                break;
            case GL_DEBUG_SEVERITY_LOW:
                logger.debug(logFormat, args);
                break;
            default:
            case GL_DEBUG_SEVERITY_NOTIFICATION:
                logger.info(logFormat, args);
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
