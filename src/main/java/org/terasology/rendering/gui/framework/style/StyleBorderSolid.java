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
package org.terasology.rendering.gui.framework.style;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static org.lwjgl.opengl.GL11.glVertex2f;

/**
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class StyleBorderSolid extends UIDisplayContainer implements Style {

    //Textured borders
    private Vector4f width = new Vector4f(0, 0, 0, 0);
    private Color color = Color.black;

    public StyleBorderSolid(Vector4f width, Color color) {
        this.width = width;
        this.color = color;
        setCrop(false);
    }

    public StyleBorderSolid(Vector4f width, String color) {
        this.width = width;
        setColor(color);
        setCrop(false);
    }

    private float RGBtoColor(int v) {
        return (float) v / 255.0f;
    }

    @Override
    public void render() {
        glPushMatrix();
        glLoadIdentity();
        glTranslatef(getAbsolutePosition().x, getAbsolutePosition().y, 0);

        glColor4f(color.r, color.g, color.b, color.a);

        if (width.x > 0) {
            glLineWidth(width.x);
            glBegin(GL11.GL_LINES);
            glVertex2f(getPosition().x, getPosition().y - width.x / 2f);
            glVertex2f(getPosition().x + getSize().x, getPosition().y - width.x / 2f);
            glEnd();
        }

        if (width.y > 0) {
            glLineWidth(width.y);
            glBegin(GL11.GL_LINES);
            glVertex2f(getPosition().x + getSize().x + width.y / 2f, getPosition().y - width.x + width.y % 2f);
            glVertex2f(getPosition().x + getSize().x + width.y / 2f, getPosition().y + getSize().y + width.z + width.y % 2f - width.z % 2);
            glEnd();
        }

        if (width.z > 0) {
            glLineWidth(width.z);
            glBegin(GL11.GL_LINES);
            glVertex2f(getPosition().x, getPosition().y + getSize().y + width.z / 2f - width.z % 2f);
            glVertex2f(getPosition().x + getSize().x, getPosition().y + getSize().y + width.z / 2f - width.z % 2f);
            glEnd();
        }

        if (width.w > 0) {
            glLineWidth(width.w);
            glBegin(GL11.GL_LINES);
            glVertex2f(getPosition().x - width.w / 2f, getPosition().y - width.x + width.w % 2f);
            glVertex2f(getPosition().x - width.w / 2f, getPosition().y + getSize().y + width.z + width.w % 2f - width.z % 2);
            glEnd();
        }

        glPopMatrix();
    }

    @Override
    public void update() {

    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setColor(String color) {
        color = color.trim().toLowerCase();

        int r = 0;
        int g = 0;
        int b = 0;
        int a = 255;

        if (color.matches("^#[a-f0-9]{1,8}$")) {
            color = color.replace("#", "");

            int sum = Integer.parseInt(color, 16);

            a = (sum & 0xFF000000) >> 24;
            r = (sum & 0x00FF0000) >> 16;
            g = (sum & 0x0000FF00) >> 8;
            b = sum & 0x000000FF;
        }

        this.color = new Color(RGBtoColor(r), RGBtoColor(g), RGBtoColor(b), RGBtoColor(a));
    }

    public Vector4f getWidth() {
        return width;
    }

    public void setWidth(Vector4f width) {
        this.width = width;
    }

    @Override
    public int getLayer() {
        return 2;
    }
}
