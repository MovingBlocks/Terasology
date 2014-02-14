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
package org.terasology.rendering.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.texture.TextureData;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexParameterf;

/**
 * @author Immortius
 */
public class OpenGLTexture extends BaseOpenGLTexture {
    private static final Logger logger = LoggerFactory.getLogger(OpenGLTexture.class);

    // TODO: Make the retention of this dependent on a keep-in-memory setting
    private TextureData textureData;

    /**
     * Note: Generally should not be called directly. Instead use Assets.generateAsset().
     *
     * @param uri
     * @param data
     */
    // TODO: Create lwjgl renderer subsystem, and make this package private
    public OpenGLTexture(AssetUri uri, TextureData data) {
        super(uri);

        reload(data);
    }

    @Override
    public void reload(TextureData data) {
        setWidth(data.getWidth());
        setHeight(data.getHeight());
        setDepth(data.getDepth());
        setWrapMode(data.getWrapMode());
        setFilterMode(data.getFilterMode());
        setTextureType(data.getType());
        this.textureData = data;

        if (isDisposed()) {
            setId(glGenTextures());
        }

        switch (getTextureType()) {
            case TEXTURE2D:
                logger.debug("Bound texture '{}' - {}", getURI(), getId());
                glBindTexture(GL11.GL_TEXTURE_2D, getId());

                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, getGLMode(getWrapMode()));
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, getGLMode(getWrapMode()));
                GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, getGlMinFilter(getFilterMode()));
                GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, getGlMagFilter(getFilterMode()));
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, data.getBuffers().length - 1);

                for (int i = 0; i < data.getBuffers().length; i++) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, getWidth() >> i, getHeight() >> i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffers()[i]);
                }
                break;
            case TEXTURE3D:
                logger.debug("Bound texture '{}' - {}", getURI(), getId());
                glBindTexture(GL12.GL_TEXTURE_3D, getId());

                glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, getGLMode(getWrapMode()));
                glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, getGLMode(getWrapMode()));
                glTexParameterf(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, getGLMode(getWrapMode()));

                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, getGlMinFilter(getFilterMode()));
                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, getGlMagFilter(getFilterMode()));

                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_MAX_LEVEL, data.getBuffers().length - 1);

                for (int i = 0; i < data.getBuffers().length; i++) {
                    GL12.glTexImage3D(GL12.GL_TEXTURE_3D, i, GL11.GL_RGBA, getWidth(), getHeight(), getDepth(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffers()[i]);
                }

                break;
        }
    }


    @Override
    public void dispose() {
        if (getId() != 0) {
            glDeleteTextures(getId());
            setId(0);
        }
    }

    @Override
    public boolean isDisposed() {
        return getId() == 0;
    }

    @Override
    public TextureData getData() {
        return textureData;
    }
}
