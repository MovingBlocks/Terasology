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

import com.github.begla.blockmania.rendering.RenderableObject;
import com.github.begla.blockmania.rendering.ShaderManager;
import com.github.begla.blockmania.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Anthony Kireev <adeon.k87@gmail.com>
 */
public class Skysphere implements RenderableObject {
    private static int _displayListSphere = -1;
    private static final float PI = 3.1415926f, PI2 = 2 * PI, PIHALF = PI / 2;

    /* SKY */
    private float _turbidity = 10.0f, _sunPosAngle = 0.1f;
    private Vector3f _zenithColor = new Vector3f();
    private World _parent;

    public Skysphere(World parent) {
        _parent = parent;
    }

    private void drawSphere() {
        if (_displayListSphere == -1) {
            _displayListSphere = glGenLists(1);

            Sphere sphere = new Sphere();
            glNewList(_displayListSphere, GL11.GL_COMPILE);

            sphere.draw(16, 8, 8);

            glEndList();
        }

        glCallList(_displayListSphere);
    }

    public void render() {
        if (_parent.getPlayer().isHeadUnderWater())
            return;

        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        _sunPosAngle = (float) Math.toRadians(360.0 * _parent.getWorldProvider().getTime() - 90.0);
        Vector4f sunNormalise = new Vector4f(0.0f, (float) Math.cos(_sunPosAngle), (float) Math.sin(_sunPosAngle), 1.0f);
        sunNormalise = sunNormalise.normalise(null);

        _zenithColor = getAllWeatherZenith(sunNormalise.y);

        ShaderManager.getInstance().enableShader("sky");

        int sunPos = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "sunPos");
        GL20.glUniform4f(sunPos, 0.0f, (float) Math.cos(_sunPosAngle), (float) Math.sin(_sunPosAngle), 1.0f);

        int turbidity = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "turbidity");
        GL20.glUniform1f(turbidity, _turbidity);

        int zenith = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("sky"), "zenith");
        GL20.glUniform3f(zenith, _zenithColor.x, _zenithColor.y, _zenithColor.z);

        drawSphere();

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
        float x = t2 * Vector4f.dot(cx1, theta) + _turbidity * Vector4f.dot(cx2, theta) + Vector4f.dot(cx3, theta);
        float y = t2 * Vector4f.dot(cy1, theta) + _turbidity * Vector4f.dot(cy2, theta) + Vector4f.dot(cy3, theta);

        return new Vector3f(Y, x, y);
    }

    public void update() {
        _turbidity = 6.0f + (float) _parent.getActiveHumidity() * (float) _parent.getActiveTemperature();
    }

    public float getSunPosAngle() {
        return _sunPosAngle;
    }

    public float getTurbidity() {
        return _turbidity;
    }
}