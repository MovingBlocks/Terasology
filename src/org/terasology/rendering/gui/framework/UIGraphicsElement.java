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
package org.terasology.rendering.gui.framework;


import org.lwjgl.opengl.GL11;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.manager.TextureManager;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

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

    private Mesh _mesh;

    public UIGraphicsElement(String textureName) {
        _textureName = textureName;

        if (_mesh == null) {
            Tessellator tessellator = new Tessellator();
            TessellatorHelper.addGUIQuadMesh(tessellator, new Vector4f(1f, 1f, 1f, 1f), 1.0f, 1.0f);
            _mesh = tessellator.generateMesh();
        }
    }

    @Override
    public void render() {
        if (_mesh == null)
            return;

        ShaderManager.getInstance().enableDefaultTextured();
        TextureManager.getInstance().bindTexture(_textureName);

        glMatrixMode(GL_TEXTURE);
        glPushMatrix();
        glTranslatef(_textureOrigin.x, _textureOrigin.y, 0.0f);
        glScalef(_textureSize.x, _textureSize.y, 1.0f);
        glMatrixMode(GL11.GL_MODELVIEW);

        glPushMatrix();
        glScalef(getSize().x, getSize().y, 1.0f);
        _mesh.render();
        glPopMatrix();

        glMatrixMode(GL_TEXTURE);
        glPopMatrix();
        glMatrixMode(GL11.GL_MODELVIEW);
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
