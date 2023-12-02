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
    public void invoke(int error, long description) {
        logger.atError().addArgument(() -> error).addArgument(() -> MemoryUtil.memASCII(description)).
                log("Received error. Code: {}, Description: {}");
    }
}
