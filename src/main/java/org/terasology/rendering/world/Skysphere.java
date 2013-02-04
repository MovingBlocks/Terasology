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
import static org.lwjgl.opengl.GL11.GL_LIGHT0;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glLight;
import static org.lwjgl.opengl.GL11.glNewList;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.Sphere;
import org.terasology.asset.Assets;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.properties.IPropertyProvider;
import org.terasology.properties.Property;
import org.terasology.rendering.shader.ShaderProgram;

/**
 * Skysphere based on the Perez all weather luminance model.
 *
 * @author Anthony Kireev <adeon.k87@gmail.com>
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Skysphere implements IPropertyProvider {

    private static int _displayListSphere = -1;
    private static final float PI = 3.1415926f;

    /* SKY */
    private Property turbidity = new Property("turbidity", 14.0f, 0.0f, 100.0f);

    private float _sunPosAngle = 0.1f;
    private static IntBuffer _textureIds;

    private final WorldRenderer _parent;

    public Skysphere(WorldRenderer parent) {
        _parent = parent;

        initTextures();
        loadCubeMap(_textureIds.get(0), "stars", 128);
        loadCubeMap(_textureIds.get(1), "sky", 512);
    }

    private void initTextures() {
        if (_textureIds == null) {
            _textureIds = BufferUtils.createIntBuffer(2);
            GL11.glGenTextures(_textureIds);
        }
    }

    private void loadCubeMap(int textureId, String name, int size) {
        int internalFormat = GL11.GL_RGBA8, format = GL12.GL_BGRA;

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureId);

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        for (int i = 0; i < 6; i++) {

            ByteBuffer data = Assets.getTexture("engine:" + name + (i + 1)).getImageData(0);

            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, size, size,
                    0, format, GL11.GL_UNSIGNED_BYTE, data);
        }
    }

    public void render() {
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL13.GL_TEXTURE_CUBE_MAP);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, _textureIds.get(0));
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, _textureIds.get(1));

        _sunPosAngle = (float) java.lang.Math.toRadians(360.0 * _parent.getWorldProvider().getTimeInDays() - 90.0);
        Vector4d sunNormalise = new Vector4d(0.0f, java.lang.Math.cos(_sunPosAngle), java.lang.Math.sin(_sunPosAngle), 1.0);
        sunNormalise.normalize();

        Vector3d zenithColor = new Vector3d();

        if (sunNormalise.y >= -0.05)
            zenithColor = getAllWeatherZenith((float) sunNormalise.y);

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("sky");
        shader.enable();

        shader.setInt("texCubeStars", 0);
        shader.setInt("texCubeSky", 1);
        shader.setFloat4("sunPos", 0.0f, (float) java.lang.Math.cos(_sunPosAngle), (float) java.lang.Math.sin(_sunPosAngle), 1.0f);
        shader.setFloat("sunAngle", _sunPosAngle);
        shader.setFloat("turbidity", (Float) turbidity.getValue());
        shader.setFloat3("zenith", (float) zenithColor.x, (float) zenithColor.y, (float) zenithColor.z);

        // Draw the skysphere
        drawSphere();

        glDisable(GL13.GL_TEXTURE_CUBE_MAP);
        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
    }

    private Vector3d getAllWeatherZenith(float thetaSun) {
        thetaSun = (float) java.lang.Math.acos(thetaSun);
        Vector4f cx1 = new Vector4f(0.0f, 0.00209f, -0.00375f, 0.00165f);
        Vector4f cx2 = new Vector4f(0.00394f, -0.03202f, 0.06377f, -0.02903f);
        Vector4f cx3 = new Vector4f(0.25886f, 0.06052f, -0.21196f, 0.11693f);
        Vector4f cy1 = new Vector4f(0.0f, 0.00317f, -0.00610f, 0.00275f);
        Vector4f cy2 = new Vector4f(0.00516f, -0.04153f, 0.08970f, -0.04214f);
        Vector4f cy3 = new Vector4f(0.26688f, 0.06670f, -0.26756f, 0.15346f);

        double t2 = (float) java.lang.Math.pow((Float) turbidity.getValue(), 2);
        double chi = (4.0f / 9.0f - (Float) turbidity.getValue() / 120.0f) * (PI - 2.0f * thetaSun);

        Vector4f theta = new Vector4f(1, thetaSun, (float) java.lang.Math.pow(thetaSun, 2), (float) java.lang.Math.pow(thetaSun, 3));

        double Y = (4.0453f * (Float) turbidity.getValue() - 4.9710f) * (float) java.lang.Math.tan(chi) - 0.2155f * (Float) turbidity.getValue() + 2.4192f;
        double x = t2 * cx1.dot(theta) + (Float) turbidity.getValue() * cx2.dot(theta) + cx3.dot(theta);
        double y = t2 * cy1.dot(theta) + (Float) turbidity.getValue() * cy2.dot(theta) + cy3.dot(theta);

        return new Vector3d(Y, x, y);
    }

    public void update(float delta) {
        // Set the light direction according to the position of the sun
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        buffer.put(0.0f).put((float) java.lang.Math.cos(_sunPosAngle)).put((float) java.lang.Math.sin(_sunPosAngle)).put(1.0f);
        buffer.flip();

        glLight(GL_LIGHT0, GL11.GL_POSITION, buffer);
    }

    private void drawSphere() {
        if (_displayListSphere == -1) {
            _displayListSphere = glGenLists(1);

            Sphere sphere = new Sphere();
            glNewList(_displayListSphere, GL11.GL_COMPILE);

            sphere.draw(16, 16, 128);

            glEndList();
        }

        glCallList(_displayListSphere);
    }

    public double getSunPosAngle() {
        return _sunPosAngle;
    }

    public double getDaylight() {
        double angle = java.lang.Math.toDegrees(TeraMath.clamp(java.lang.Math.cos(_sunPosAngle)));
        double daylight = 1.0;

        if (angle < 24.0) {
            daylight = 1.0 - (24.0 - angle) / 24.0;
        }

        return daylight;
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(turbidity);
    }
}
