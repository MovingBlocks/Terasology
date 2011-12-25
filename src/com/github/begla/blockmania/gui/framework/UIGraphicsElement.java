/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.gui.framework;


import com.github.begla.blockmania.rendering.manager.TextureManager;
import com.github.begla.blockmania.utilities.FastRandom;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Provides support for rendering graphical elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIGraphicsElement extends UIDisplayElement {
    private final String _textureName;

    private final Vector2f _textureOrigin = new Vector2f(0.0f, 0.0f);
    private final Vector2f _textureSize = new Vector2f(1.0f, 1.0f);

    private static final FastRandom _debugRand = new FastRandom(42);
    private final Vector3f _debugColor = new Vector3f();

    public UIGraphicsElement(String textureName) {
        _textureName = textureName;

        _debugColor.set((float) Math.abs(_debugRand.randomDouble()), (float) Math.abs(_debugRand.randomDouble()), (float) Math.abs(_debugRand.randomDouble()));
    }

    @Override
    public void render() {
        TextureManager.getInstance().bindTexture(_textureName);

        glEnable(GL11.GL_TEXTURE_2D);
        glBegin(GL11.GL_QUADS);

        //glColor4f(_debugColor.x,_debugColor.y, _debugColor.z, 1.0f);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        glTexCoord2f(_textureOrigin.x, _textureOrigin.y);
        glVertex2f(0, 0);
        glTexCoord2f(_textureOrigin.x + getTextureSize().x, _textureOrigin.y);
        glVertex2f(getSize().x, 0);
        glTexCoord2f(_textureOrigin.x + getTextureSize().x, _textureOrigin.y + getTextureSize().y);
        glVertex2f(getSize().x, getSize().y);
        glTexCoord2f(_textureOrigin.x, _textureOrigin.y + getTextureSize().y);
        glVertex2f(0, getSize().y);

        glEnd();
        glDisable(GL11.GL_TEXTURE_2D);
    }

    public Vector2f getTextureOrigin() {
        return _textureOrigin;
    }

    public Vector2f getTextureSize() {
        return _textureSize;
    }

    @Override
    public void update() {
    }
}
