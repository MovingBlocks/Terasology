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
package com.github.begla.blockmania.world.horizon;

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.noise.PerlinNoise;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import com.github.begla.blockmania.rendering.manager.ShaderManager;
import com.github.begla.blockmania.rendering.manager.TextureManager;
import com.github.begla.blockmania.utilities.MathHelper;
import com.github.begla.blockmania.world.main.WorldRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.Sphere;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * Skysphere based on the Perez all weather luminance model.
 *
 * @author Anthony Kireev <adeon.k87@gmail.com>
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Skysphere implements RenderableObject {

    private static int _displayListSphere = -1;
    private static final float PI = 3.1415926f;

    /* SKY */
    private float _turbidity = 12.0f, _sunPosAngle = 0.1f;

    /* CLOUDS */
    private static final Vector2f CLOUD_RESOLUTION = (Vector2f) ConfigurationManager.getInstance().getConfig().get("System.cloudResolution");
    private static final long CLOUD_UPDATE_INTERVAL = (Integer) ConfigurationManager.getInstance().getConfig().get("System.cloudUpdateInterval");
    private static IntBuffer _textureIds;

    private final PerlinNoise _noiseGenerator;
    private long _lastCloudUpdate = Blockmania.getInstance().getTime();
    ByteBuffer _cloudByteBuffer = null;

    private final WorldRenderer _parent;

    public Skysphere(WorldRenderer parent) {
        _parent = parent;
        _noiseGenerator = new PerlinNoise(_parent.getWorldProvider().getSeed().hashCode());

        initTextures();
        loadStarTextures();
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

    private void generateNewClouds() {

        // Generate some new clouds according to the current time
        ByteBuffer clouds = ByteBuffer.allocateDirect((int) CLOUD_RESOLUTION.x * (int) CLOUD_RESOLUTION.y * 3);

        for (int i = 0; i < (int) CLOUD_RESOLUTION.x; i++) {
            for (int j = 0; j < (int) CLOUD_RESOLUTION.y; j++) {
                double noise = _noiseGenerator.fBm(i * 0.05, j * 0.05, _parent.getWorldProvider().getTime() * 5f);
                byte value = (byte) ((MathHelper.clamp(noise)) * 255);

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

    private void initTextures() {
        if (_textureIds == null) {
            _textureIds = BufferUtils.createIntBuffer(2);
            GL11.glGenTextures(_textureIds);
        }
    }

    private void drawClouds() {
        glEnable(GL11.GL_BLEND);

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, _textureIds.get(1));

        glPushMatrix();
        glTranslatef(0, 8.0f, 0);
        glScalef(128f, 1.0f, 128f);

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

        glPopMatrix();

        glDisable(GL_BLEND);
        glDisable(GL11.GL_TEXTURE_2D);
    }

    public void render() {
        updateClouds();

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL13.GL_TEXTURE_CUBE_MAP);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, _textureIds.get(0));
        _sunPosAngle = (float) Math.toRadians(360.0 * _parent.getWorldProvider().getTime() - 90.0);
        Vector4f sunNormalise = new Vector4f(0.0f, (float) Math.cos(_sunPosAngle), (float) Math.sin(_sunPosAngle), 1.0f);
        sunNormalise.normalize();

        Vector3f _zenithColor = getAllWeatherZenith(sunNormalise.y);

        ShaderManager.getInstance().enableShader("sky");

        int sunPos = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "sunPos");
        GL20.glUniform4f(sunPos, 0.0f, (float) Math.cos(_sunPosAngle), (float) Math.sin(_sunPosAngle), 1.0f);

        int time = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "time");
        GL20.glUniform1f(time, (float) _parent.getWorldProvider().getTime());

        int sunAngle = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "sunAngle");
        GL20.glUniform1f(sunAngle, _sunPosAngle);

        int turbidity = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "turbidity");
        GL20.glUniform1f(turbidity, _turbidity);

        int zenith = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "zenith");
        GL20.glUniform3f(zenith, _zenithColor.x, _zenithColor.y, _zenithColor.z);

        // DRAW THE SKYSPHERE
        drawSphere();

        glDisable(GL13.GL_TEXTURE_CUBE_MAP);

        ShaderManager.getInstance().enableShader("clouds");
        // Apply daylight
        int lightClouds = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("clouds"), "light");
        GL20.glUniform1f(lightClouds, getDaylight());

        // DRAW THE CLOUDS
        drawClouds();
        ShaderManager.getInstance().enableShader(null);

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
    }

    public void addToSunAngle(float addVolume) {
        _sunPosAngle += addVolume;
    }

    private Vector3f getAllWeatherZenith(float thetaSun) {
        thetaSun = (float) Math.acos(thetaSun);
        Vector4f cx1 = new Vector4f(0.0f, 0.00209f, -0.00375f, 0.00165f);
        Vector4f cx2 = new Vector4f(0.00394f, -0.03202f, 0.06377f, -0.02903f);
        Vector4f cx3 = new Vector4f(0.25886f, 0.06052f, -0.21196f, 0.11693f);
        Vector4f cy1 = new Vector4f(0.0f, 0.00317f, -0.00610f, 0.00275f);
        Vector4f cy2 = new Vector4f(0.00516f, -0.04153f, 0.08970f, -0.04214f);
        Vector4f cy3 = new Vector4f(0.26688f, 0.06670f, -0.26756f, 0.15346f);

        float t2 = (float) Math.pow(_turbidity, 2);
        float chi = (4.0f / 9.0f - _turbidity / 120.0f) * (PI - 2.0f * thetaSun);

        Vector4f theta = new Vector4f(1, thetaSun, (float) Math.pow(thetaSun, 2), (float) Math.pow(thetaSun, 3));

        float Y = (4.0453f * _turbidity - 4.9710f) * (float) Math.tan(chi) - 0.2155f * _turbidity + 2.4192f;
        float x = t2 * cx1.dot(theta) + _turbidity * cx2.dot(theta) + cx3.dot(theta);
        float y = t2 * cy1.dot(theta) + _turbidity * cy2.dot(theta) + cy3.dot(theta);

        return new Vector3f(Y, x, y);
    }

    public void update() {
        if (_cloudByteBuffer == null && Blockmania.getInstance().getTime() - _lastCloudUpdate >= CLOUD_UPDATE_INTERVAL) {
            _lastCloudUpdate = Blockmania.getInstance().getTime();

            Blockmania.getInstance().getThreadPool().execute(new Runnable() {
                public void run() {
                    generateNewClouds();
                }
            });
        }

        _turbidity = 6.0f + ((float) _parent.getActiveHumidity() * (float) _parent.getActiveTemperature()) * 6.0f;
    }

    public float getSunPosAngle() {
        return _sunPosAngle;
    }

    public float getTurbidity() {
        return _turbidity;
    }

    public float getDaylight() {
        return (float) Math.min(Math.max(Math.cos(_sunPosAngle), 0.0f) + 0.3f, 1.0f);
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

            byte[] data = TextureManager.getInstance().getTexture("stars" + (i + 1)).getTextureData();
            ByteBuffer byteBuffer = BufferUtils.createByteBuffer(data.length);
            byteBuffer.put(data);
            byteBuffer.flip();

            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, 256, 256,
                    0, format, GL11.GL_UNSIGNED_BYTE, byteBuffer);
        }
    }
}
