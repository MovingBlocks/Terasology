/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.stateChanges;

import static org.lwjgl.opengl.GL11.glEnable;

/**
 * TODO: Add javadocs
 */
abstract class EnableStateParameter extends SetStateParameter {
    /**
     * Construct an instance of this class, provided an OpenGL constant and a boolean to enable or disable
     * the indicated mode.
     *
     * @param glParameter An integer representing one of the many OpenGL constants, i.e. GL_DEPTH_TEST
     */
    EnableStateParameter(int glParameter) {
        super(glParameter);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EnableStateParameter) && (this.glParameter == ((EnableStateParameter) obj).glParameter);
    }

    @Override
    public void process() {
        glEnable(glParameter);
    }
}
