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
package org.terasology.rendering.gui.components;

import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.MeshCollection;
import org.terasology.rendering.primitives.Tessellator;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Transparent fullscreen overlay.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UITransparentOverlay extends UIDisplayElement {
    private static Mesh _mesh;

    public UITransparentOverlay() {
        if (_mesh == null) {
            MeshCollection.addGUIQuadMesh(new Vector4f(0.05f, 0.05f, 0.05f, 0.8f), 1.0f, 1.0f);
            _mesh = Tessellator.getInstance().generateMesh();
            Tessellator.getInstance().resetAll();
        }
    }

    @Override
    public void render() {
        if (_mesh != null) {
            glPushMatrix();
            glScalef(getSize().x, getSize().y, 0.0f);
            _mesh.render();
            glPopMatrix();
        }
    }

    @Override
    public void update() {
        setSize(new Vector2f((float) Display.getWidth(), (float) Display.getHeight()));
    }
}
