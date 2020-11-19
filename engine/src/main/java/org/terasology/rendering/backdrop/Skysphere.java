/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.backdrop;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.terasology.context.Context;
import org.terasology.nui.properties.Range;
import org.terasology.rendering.primitives.Sphere;
import org.terasology.utilities.Assets;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.world.sun.CelestialSystem;

import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;

/**
 * Skysphere based on the Perez all weather luminance model.
 */
public class Skysphere implements BackdropProvider, BackdropRenderer {

    private static int displayListSphere = -1;

    @Range(min = 0.01f, max = 100.0f)
    private float colorExp = 0.01f;
    @Range(min = 2.0f, max = 32.0f)
    private float turbidity = 9.0f;

    private final CelestialSystem celSystem;

    public Skysphere(Context context) {
        celSystem = context.get(CelestialSystem.class);
    }

    @Override
    public void render(Camera camera) {
        glDepthMask(false);

        if (camera.isReflected()) {
            glCullFace(GL_BACK);
        } else {
            glCullFace(GL_FRONT);
        }

        Material shader = Assets.getMaterial("engine:prog.sky").get();
        shader.enable();

        // Draw the skysphere
        drawSkysphere(camera.getzFar());

        if (camera.isReflected()) {
            glCullFace(GL_FRONT);
        } else {
            glCullFace(GL_BACK);
        }

        glDepthMask(true);
    }

    private void drawSkysphere(float zFar) {
        if (displayListSphere == -1) {
            displayListSphere = glGenLists(1);

            Sphere sphere = new Sphere();
            sphere.setTextureFlag(true);

            glNewList(displayListSphere, GL11.GL_COMPILE);

            float skyBoxDistance = (zFar > 1024 ? 1024.0f : zFar * 0.95f);
            sphere.draw(skyBoxDistance, 16, 128);

            glEndList();
        }

        glCallList(displayListSphere);
    }

    @Override
    public float getSunPositionAngle() {
        return celSystem.getSunPosAngle();
    }

    @Override
    public float getDaylight() {
        float angle = (float) Math.toDegrees(TeraMath.clamp(Math.cos(getSunPositionAngle())));
        float daylight = 1.0f;

        if (angle < 24.0f) {
            daylight = Math.max(1.0f - (24.0f - angle) / 24.0f, 0.15f);
        }

        return daylight;
    }

    @Override
    public float getTurbidity() {
        return turbidity;
    }

    @Override
    public float getColorExp() {
        return colorExp;
    }

    @Override
    public Vector3f getSunDirection(boolean moonlightFlip) {
        float sunAngle = getSunPositionAngle() + 0.0001f;
        Vector3f sunDirection = new Vector3f(0.0f, (float) Math.cos(sunAngle), (float) Math.sin(sunAngle));

        // Moonlight flip
        if (moonlightFlip && sunDirection.y < 0.0f) {
            sunDirection.mul(-1.0f);
        }

        return sunDirection;
    }
}
