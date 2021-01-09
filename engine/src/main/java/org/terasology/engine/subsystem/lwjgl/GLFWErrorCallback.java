package org.terasology.engine.subsystem.lwjgl;


import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GLFWErrorCallback implements GLFWErrorCallbackI {
    private static final Logger logger = LoggerFactory.getLogger("GLFW");

    @Override
    public void invoke(int error, long description) {
        logger.error("Received error. Code: {}, Description: {}", error, MemoryUtil.memASCII(description));
    }
}
