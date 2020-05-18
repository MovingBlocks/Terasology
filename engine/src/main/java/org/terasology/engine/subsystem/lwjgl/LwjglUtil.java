/*
 * Copyright 2020 MovingBlocks
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

import org.lwjgl.glfw.GLFW;

public class LwjglUtil {

    // TODO: LWJGL 3 - try use it in non static way
    public static WindowSize getWindowSize() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetWindowSize(GLFW.glfwGetCurrentContext(), width, height);
        return new WindowSize(width[0], height[0]);
    }
}
