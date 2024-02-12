// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.subsystem.lwjgl;


import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GLFWErrorCallback implements GLFWErrorCallbackI {
    private static final Logger logger = LoggerFactory.getLogger("GLFW");

    @Override
    @SuppressWarnings("PMD.GuardLogStatement")
    public void invoke(int error, long description) {
        logger.error("Received error. Code: {}, Description: {}", error, MemoryUtil.memASCII(description));
    }
}
