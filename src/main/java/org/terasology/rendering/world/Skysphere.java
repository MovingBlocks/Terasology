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
package org.terasology.rendering.world;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.Sphere;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Timer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.utilities.PerlinNoise;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Skysphere based on the Perez all weather luminance model.
 *
 * @author Anthony Kireev <adeon.k87@gmail.com>
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Skysphere implements IGameObject {

    private static int _displayListSphere = -1;
    private static int _displayListClouds = -1;
    private static final float PI = 3.1415926f;

    /* SKY */
    private double _turbidity = 4.0f, _sunPosAngle = 0.1f;

    /* CLOUDS */
    private static final Vector2f CLOUD_RESOLUTION = Config.getInstance().getCloudResolution();
    private static final long CLOUD_UPDATE_INTERVAL = Config.getInstance().getCloudUpdateInterval();
    private static IntBuffer _textureIds;

    private final PerlinNoise _noiseGenerator;
    private Timer _timer = CoreRegistry.get(Timer.class);
    private long _lastCloudUpdate = _timer.getTimeInMs() - CLOUD_UPDATE_INTERVAL;
    ByteBuffer _cloudByteBuffer = null;

    private final WorldRenderer _parent;

    public Skysphere(WorldRenderer parent) {
        _parent = parent;
        _noiseGenerator = new PerlinNoise(_parent.getWorldProvider().getSeed().hashCode());

        initTextures();
        loadStarTextures();
    }

    private void initTextures() {
        if (_textureIds == null) {
            _textureIds = BufferUtils.createIntBuffer(2);
            GL11.glGenTextures(_textureIds);
        }
    }

    private void loadStarTextures() {
        int internalFormat = GL11.GL_RGBA8, format = GL12.GL_BGRA;

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, _textureIds.get(0));

        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

        for (int i = 0; i < 6; i++) {

            ByteBuffer data = AssetManager.loadTexture("engine:stars" + (i + 1)).getImageData(0);

            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, 256, 256,
                    0, format, GL11.GL_UNSIGNED_BYTE, data);
        }
    }

    public void render() {
        updateClouds();

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL13.GL_TEXTURE_CUBE_MAP);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, _textureIds.get(0));
        _sunPosAngle = (float) java.lang.Math.toRadians(360.0 * _parent.getWorldProvider().getTime() - 90.0);
        Vector4d sunNormalise = new Vector4d(0.0f, java.lang.Math.cos(_sunPosAngle), java.lang.Math.sin(_sunPosAngle), 1.0);
        sunNormalise.normalize();

        Vector3d zenithColor = new Vector3d();

        if (sunNormalise.y >= -0.35)
            zenithColor = getAllWeatherZenith((float) sunNormalise.y);

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("sky");
        shader.enable();

        shader.setFloat4("sunPos", 0.0f, (float) java.lang.Math.cos(_sunPosAngle), (float) java.lang.Math.sin(_sunPosAngle), 1.0f);
        shader.setFloat("time", (float) _parent.getWorldProvider().getTime());
        shader.setFloat("sunAngle", (float) _sunPosAngle);
        shader.setFloat("turbidity", (float) _turbidity);
        shader.setFloat3("zenith", (float) zenithColor.x, (float) zenithColor.y, (float) zenithColor.z);

        // Draw the skysphere
        drawSphere();

        glDisable(GL13.GL_TEXTURE_CUBE_MAP);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        ShaderManager.getInstance().enableShader("clouds");
        // Apply daylight
        int lightClouds = GL20.glGetUniformLocation(ShaderManager.getInstance().getShaderProgram("clouds").getShaderId(), "light");
        GL20.glUniform1f(lightClouds, (float) getDaylight());

        // Finally draw the clouds
        drawClouds();

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

        double t2 = (float) java.lang.Math.pow(_turbidity, 2);
        double chi = (4.0f / 9.0f - _turbidity / 120.0f) * (PI - 2.0f * thetaSun);

        Vector4f theta = new Vector4f(1, thetaSun, (float) java.lang.Math.pow(thetaSun, 2), (float) java.lang.Math.pow(thetaSun, 3));

        double Y = (4.0453f * _turbidity - 4.9710f) * (float) java.lang.Math.tan(chi) - 0.2155f * _turbidity + 2.4192f;
        double x = t2 * cx1.dot(theta) + _turbidity * cx2.dot(theta) + cx3.dot(theta);
        double y = t2 * cy1.dot(theta) + _turbidity * cy2.dot(theta) + cy3.dot(theta);

        return new Vector3d(Y, x, y);
    }

    public void update(float delta) {
        if (_cloudByteBuffer == null && _timer.getTimeInMs() - _lastCloudUpdate >= CLOUD_UPDATE_INTERVAL) {
            _lastCloudUpdate = _timer.getTimeInMs();

            CoreRegistry.get(GameEngine.class).submitTask("Generate Clouds", new Runnable() {
                public void run() {
                    generateNewClouds();
                }
            });
        }

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

    private void drawClouds() {
        glEnable(GL11.GL_BLEND);
        glBindTexture(GL_TEXTURE_2D, _textureIds.get(1));

        glPushMatrix();
        glTranslatef(0, 8.0f, 0);
        glScalef(128f, 1.0f, 128f);

        if (_displayListClouds == -1) {
            _displayListClouds = glGenLists(1);

            glNewList(_displayListClouds, GL11.GL_COMPILE);

            glBegin(GL_QUADS);

            glTexCoord2d(1, 1);
            glVertex3f(0.5f, 0.0f, 0.5f);
            glTexCoord2d(0, 1);
            glVertex3f(-0.5f, 0.0f, 0.5f);
            glTexCoord2d(0, 0);
            glVertex3f(-0.5f, 0.0f, -0.5f);
            glTexCoord2d(1, 0);
            glVertex3f(0.5f, 0.0f, -0.5f);

            glEnd();
            glEndList();
        }

        glCallList(_displayListClouds);

        glPopMatrix();

        glDisable(GL_BLEND);

    }

    private void generateNewClouds() {
        // Generate some new clouds according to the current time
        ByteBuffer clouds = ByteBuffer.allocateDirect((int) CLOUD_RESOLUTION.x * (int) CLOUD_RESOLUTION.y * 3);

        for (int i = 0; i < (int) CLOUD_RESOLUTION.x; i++) {
            for (int j = 0; j < (int) CLOUD_RESOLUTION.y; j++) {
                double noise = _noiseGenerator.fBm(i * 0.008, j * 0.008, _parent.getWorldProvider().getTime());

                byte value = (byte) (TeraMath.clamp(noise * 1.25 + 0.25) * 255);

                clouds.put(value);
                clouds.put(value);
                clouds.put(value);
            }
        }

        _cloudByteBuffer = clouds;
        _cloudByteBuffer.flip();
    }

    private void updateClouds() {
        if (_cloudByteBuffer != null) {
            glBindTexture(GL_TEXTURE_2D, _textureIds.get(1));

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, (int) CLOUD_RESOLUTION.x, (int) CLOUD_RESOLUTION.y, 0, GL_RGB, GL_UNSIGNED_BYTE, _cloudByteBuffer);

            _cloudByteBuffer = null;
        }
    }

    public double getSunPosAngle() {
        return _sunPosAngle;
    }

    public double getTurbidity() {
        return _turbidity;
    }

    public double getDaylight() {
        double angle = java.lang.Math.toDegrees(TeraMath.clamp(java.lang.Math.cos(_sunPosAngle)));
        double daylight = 1.0;

        if (angle < 24.0) {
            daylight = 1.0 - (24.0 - angle) / 24.0;
        }

        return daylight;
    }
}
