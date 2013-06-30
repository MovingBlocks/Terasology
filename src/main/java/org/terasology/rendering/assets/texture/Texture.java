/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.rendering.assets.texture;

import org.terasology.asset.Asset;

import static org.lwjgl.opengl.GL11.GL_CLAMP;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NEAREST_MIPMAP_NEAREST;
import static org.lwjgl.opengl.GL11.GL_REPEAT;

/**
 * @author Immortius
 */
public interface Texture extends Asset<TextureData> {
    public enum WrapMode {
        Clamp(GL_CLAMP),
        Repeat(GL_REPEAT);

        private int glWrapEnum;

        private WrapMode(int glEnum) {
            this.glWrapEnum = glEnum;
        }

        public int getGLMode() {
            return glWrapEnum;
        }
    }

    public enum FilterMode {
        Nearest(GL_NEAREST_MIPMAP_NEAREST, GL_NEAREST),
        Linear(GL_LINEAR_MIPMAP_LINEAR, GL_LINEAR);

        private int glMinFilter;
        private int glMagFilter;

        private FilterMode(int glMinFilter, int glMagFilter) {
            this.glMinFilter = glMinFilter;
            this.glMagFilter = glMagFilter;
        }

        public int getGlMinFilter() {
            return glMinFilter;
        }

        public int getGlMagFilter() {
            return glMagFilter;
        }
    }

    int getWidth();

    int getHeight();

    WrapMode getWrapMode();

    FilterMode getFilterMode();

    // TODO: Remove when no longer needed
    TextureData getData();

    // TODO: This shouldn't be on texture
    int getId();
}
