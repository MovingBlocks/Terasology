/*
 * Copyright 2018 MovingBlocks
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

import org.lwjgl.glfw.GLFWVidMode;
import org.terasology.engine.subsystem.Resolution;

public final class LwjglResolution implements Resolution {

    private final GLFWVidMode vidMode;

    public LwjglResolution(GLFWVidMode vidMode) {
        this.vidMode = vidMode;
    }

    public GLFWVidMode getVidMode() {
        return vidMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LwjglResolution that = (LwjglResolution) o;

        return vidMode.equals(that.vidMode);
    }

    @Override
    public int hashCode() {
        return vidMode.hashCode();
    }

    @Override
    public String toString() {
        return vidMode.toString();
    }
}
