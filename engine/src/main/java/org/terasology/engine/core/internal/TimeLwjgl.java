// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.internal;

import org.lwjgl.glfw.GLFW;

public final class TimeLwjgl extends TimeBase {

    public TimeLwjgl() {
        super((long) (GLFW.glfwGetTime() * 1000));
    }

    @Override
    public long getRawTimeInMs() {
        return (long) (GLFW.glfwGetTime() * 1000);
    }
}
