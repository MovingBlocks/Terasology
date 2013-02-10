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

    private Texture skyTexture90 = null;
    private Texture skyTexture180 = null;

    private final WorldRenderer _parent;

    public Skysphere(WorldRenderer parent) {
        _parent = parent;

        initTextures();
    }

    private void initTextures() {
        skyTexture180 = Assets.getTexture("engine:sky180");
        skyTexture90 = Assets.getTexture("engine:sky90");
    }

//    private void loadCubeMap(int textureId, String name, int size) {
//        int internalFormat = GL11.GL_RGBA8, format = GL12.GL_BGRA;
//
//        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, textureId);
//
//        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
//        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
//        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
//        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
//        GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//
//        for (int i = 0; i < 6; i++) {
//
//            ByteBuffer data = Assets.getTexture("engine:" + name + (i + 1)).getImageData(0);
//
//            GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, size, size,
//                    0, format, GL11.GL_UNSIGNED_BYTE, data);
//        }
//    }

    public void render() {
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyTexture90.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, skyTexture180.getId());

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("sky");
        shader.enable();

        shader.setInt("texSky90", 0);
        shader.setInt("texSky180", 1);

        // Draw the skysphere
        drawSphere();

        glEnable(GL_CULL_FACE);
        glEnable(GL_DEPTH_TEST);
    }

    public void update(float delta) {
        sunPosAngle = (float) java.lang.Math.toRadians(360.0 * _parent.getWorldProvider().getTimeInDays() - 90.0);

        // Set the light direction according to the position of the sun
        FloatBuffer buffer = BufferUtils.createFloatBuffer(4);
        buffer.put(0.0f).put((float) java.lang.Math.cos(sunPosAngle)).put((float) java.lang.Math.sin(sunPosAngle)).put(1.0f);
        buffer.flip();

        glLight(GL_LIGHT0, GL11.GL_POSITION, buffer);
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
}
