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
package org.terasology.rendering.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AbstractAsset;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.texture.Texture;
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
public class OpenGLTexture extends AbstractAsset<TextureData> implements Texture {

    private static final Logger logger = LoggerFactory.getLogger(OpenGLTexture.class);

    private int id = 0;
    private int width;
    private int height;
    private int depth;
    private WrapMode wrapMode = WrapMode.Clamp;
    private FilterMode filterMode = FilterMode.Nearest;
    private Type textureType = Type.TEXTURE2D;

    // TODO: Remove this when Icons can access TextureData directly.
    private TextureData textureData;

    public OpenGLTexture(AssetUri uri, TextureData data) {
        super(uri);

        reload(data);
    }

    @Override
    public void reload(TextureData data) {
        dispose();
        this.width = data.getWidth();
        this.height = data.getHeight();
        this.depth = data.getDepth();
        this.wrapMode = data.getWrapMode();
        this.filterMode = data.getFilterMode();
        this.textureType = data.getType();
        this.textureData = data;

        switch (textureType) {
            case TEXTURE2D:
                id = glGenTextures();
                logger.debug("Bound texture '{}' - {}", getURI(), id);
                glBindTexture(GL11.GL_TEXTURE_2D, id);

                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode.getGLMode());
                glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode.getGLMode());
                GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filterMode.getGlMinFilter());
                GL11.glTexParameteri(GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filterMode.getGlMagFilter());
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, data.getBuffers().length - 1);

                for (int i = 0; i < data.getBuffers().length; i++) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, i, GL11.GL_RGBA, width >> i, height >> i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffers()[i]);
                }
                break;
            case TEXTURE3D:
                id = glGenTextures();
                logger.debug("Bound texture '{}' - {}", getURI(), id);
                glBindTexture(GL12.GL_TEXTURE_3D, id);

                glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_S, wrapMode.getGLMode());
                glTexParameterf(GL12.GL_TEXTURE_3D, GL_TEXTURE_WRAP_T, wrapMode.getGLMode());
                glTexParameterf(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_WRAP_R, wrapMode.getGLMode());

                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MIN_FILTER, filterMode.getGlMinFilter());
                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL11.GL_TEXTURE_MAG_FILTER, filterMode.getGlMagFilter());

                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                GL11.glTexParameteri(GL12.GL_TEXTURE_3D, GL12.GL_TEXTURE_MAX_LEVEL, data.getBuffers().length - 1);

                for (int i = 0; i < data.getBuffers().length; i++) {
                    GL12.glTexImage3D(GL12.GL_TEXTURE_3D, i, GL11.GL_RGBA, width, height, depth, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data.getBuffers()[i]);
                }

                Util.checkGLError();
                break;
        }


    }

    @Override
    public void dispose() {
        if (id != 0) {
            Util.checkGLError();
            glDeleteTextures(id);
            id = 0;
            Util.checkGLError();
        }
    }

    @Override
    public boolean isDisposed() {
        return id == 0;
    }

    public int getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Texture.WrapMode getWrapMode() {
        return wrapMode;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    @Override
    public TextureData getData() {
        return textureData;
    }
}
