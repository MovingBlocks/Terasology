/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.gui.widgets;


import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.internal.ColorUtil;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * Provides support for rendering graphical elements.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         <p/>
 *         TODO rotation screws up the intersection check
 */
public class UIImage extends UIDisplayContainer {

    private static final Logger logger = LoggerFactory.getLogger(UIImage.class);

    private Color color;
    private Texture texture;

    private Vector2f textureOrigin = new Vector2f(0.0f, 0.0f);
    private Vector2f textureSize = new Vector2f(1.0f, 1.0f);

    private float rotate;
    private Mesh mesh;

    public UIImage() {
    }

    public UIImage(Color color) {
        setColor(color);
    }

    public UIImage(String color) {
        setColor(color);
    }

    public UIImage(Texture texture) {
        setTexture(texture);
    }

    private float rbgToColor(int v) {
        return (float) v / 255.0f;
    }

    @Override
    public void render() {
        if (mesh == null) {
            return;
        }

        if (mesh.isDisposed()) {
            logger.error("Disposed mesh encountered!");
            return;
        }

        if (texture != null) {
            CoreRegistry.get(ShaderManager.class).enableDefaultTextured();
            glBindTexture(GL11.GL_TEXTURE_2D, texture != null ? texture.getId() : 0);
            glMatrixMode(GL_TEXTURE);
            glPushMatrix();
            glTranslatef(textureOrigin.x, textureOrigin.y, 0.0f);
            glScalef(textureSize.x, textureSize.y, 1.0f);
            glMatrixMode(GL11.GL_MODELVIEW);

            glPushMatrix();
            if (rotate > 0f) {
                glRotatef(rotate, 0f, 0f, 1f);
            }
            glScalef(getSize().x, getSize().y, 1.0f);
            mesh.render();
            glPopMatrix();

            glMatrixMode(GL_TEXTURE);
            glPopMatrix();
            glMatrixMode(GL11.GL_MODELVIEW);
        } else {
            glPushMatrix();
            if (rotate > 0f) {
                glRotatef(rotate, 0f, 0f, 1f);
            }
            glScalef(getSize().x, getSize().y, 0.0f);
            mesh.render();
            glPopMatrix();
        }

        super.render();
    }

    /**
     * Get the texture origin.
     *
     * @return Returns the texture origin.
     */
    public Vector2f getTextureOrigin() {
        return new Vector2f(textureOrigin);
    }

    /**
     * Set the texture origin. You don't need to divide by the texture width/height, this will be done within this method.
     *
     * @param origin The origin of the texture.
     */
    public void setTextureOrigin(Vector2f origin) {
        if (texture != null) {
            textureOrigin.set(origin.x / (float) texture.getWidth(), origin.y / (float) texture.getHeight());
        }
    }

    /**
     * Get the texture size.
     *
     * @return Returns the texture size.
     */
    public Vector2f getTextureSize() {
        return new Vector2f(textureSize);
    }

    /**
     * Set the texture size. You don't need to divide by the texture width/height, this will be done within this method.
     *
     * @param size The size of the texture.
     */
    public void setTextureSize(Vector2f size) {
        if (texture != null) {
            textureSize.set(size.x / (float) texture.getWidth(), size.y / (float) texture.getHeight());
        }
    }

    /**
     * Set the texture.
     *
     * @param texture The texture.
     */
    public void setTexture(Texture texture) {
        this.texture = texture;

        if (texture != null) {
            setColor(new Color(1f, 1f, 1f, 1f));
        }
    }

    public Texture getTexture() {
        return texture;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color value) {
        this.color = value;
        generateMesh();
    }

    public void setColor(String value) {
        setColor(ColorUtil.getColorForColorHexString(value));
    }

    private void generateMesh() {
        if (mesh != null) {
            mesh.dispose();
        }

        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addGUIQuadMesh(tessellator, new Vector4f(color.r, color.g, color.b, color.a), 1.0f, 1.0f);
        mesh = tessellator.generateMesh();
    }

    /*
     * Rotate graphics element
     */
    public void setRotateAngle(float angle) {
        rotate = angle;
    }

    public float getRotateAngle() {
        return rotate;
    }
}
