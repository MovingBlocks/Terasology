/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.world;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.Sphere;
import org.terasology.asset.Assets;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.editor.properties.IPropertyProvider;
import org.terasology.editor.properties.Property;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.shader.ShaderProgram;

import javax.vecmath.Vector3f;

/**
 * Skysphere based on the Perez all weather luminance model.
 *
 * @author Anthony Kireev <adeon.k87@gmail.com>
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Skysphere implements IPropertyProvider {

    private Property colorExp = new Property("colorExp", 14.0f, 0.0f, 100.0f);
    private Property turbidity = new Property("turbidity", 8.0f, 2.0f, 32.0f);
    private float sunPosAngle = 0.1f;

    private static int displayListSphere = -1;

    private final WorldRenderer _parent;

    public Skysphere(WorldRenderer parent) {
        _parent = parent;
    }

    public void render() {
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("sky");
        shader.enable();

        // Draw the skysphere
        drawSphere();

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
    }

    public void update(float delta) {
        sunPosAngle = (float) java.lang.Math.toRadians(360.0 * _parent.getWorldProvider().getTimeInDays() - 90.0);
    }

    private void drawSphere() {
        if (displayListSphere == -1) {
            displayListSphere = glGenLists(1);

            Sphere sphere = new Sphere();
            sphere.setTextureFlag(true);

            glNewList(displayListSphere, GL11.GL_COMPILE);

            sphere.draw(16, 16, 128);

            glEndList();
        }

        glCallList(displayListSphere);
    }

    public float getSunPosAngle() {
        return sunPosAngle;
    }

    public double getDaylight() {
        double angle = java.lang.Math.toDegrees(TeraMath.clamp(java.lang.Math.cos(sunPosAngle)));
        double daylight = 1.0;

        if (angle < 24.0) {
            daylight = 1.0 - (24.0 - angle) / 24.0;
        }

        return daylight;
    }

    public Property getTurbidity() {
        return turbidity;
    }

    public Property getColorExp() {
        return colorExp;
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(turbidity);
        properties.add(colorExp);
    }

    public Vector3f getQuantizedSunDirection(float stepSize) {
        float sunAngle = (float) Math.floor(getSunPosAngle() * stepSize) / stepSize + 0.0001f;
        Vector3f sunDirection = new Vector3f(0.0f, (float) Math.cos(sunAngle), (float) Math.sin(sunAngle));

        // Moonlight flip
        if (sunDirection.y < 0.0f) {
            sunDirection.scale(-1.0f);
        }

        return sunDirection;
    }

    public Vector3f getSunDirection(boolean moonlightFlip) {
        float sunAngle = getSunPosAngle() + 0.0001f;
        Vector3f sunDirection = new Vector3f(0.0f, (float) java.lang.Math.cos(sunAngle), (float) java.lang.Math.sin(sunAngle));

        // Moonlight flip
        if (moonlightFlip && sunDirection.y < 0.0f) {
            sunDirection.scale(-1.0f);
        }

        return sunDirection;
    }
}
