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
package org.terasology.rendering.world;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import org.terasology.asset.Assets;
import org.terasology.editor.EditorRange;
import org.terasology.engine.Time;
import org.terasology.math.TeraMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.world.sun.CelestialSystem;

import javax.vecmath.Vector3f;

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
 *
 * @author Anthony Kireev <adeon.k87@gmail.com>
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Skysphere {

    private static int displayListSphere = -1;

    @EditorRange(min = 0.01f, max = 100.0f)
    private float colorExp = 0.01f;
    @EditorRange(min = 2.0f, max = 32.0f)
    private float turbidity = 9.0f;

    private final Time time;
    private final CelestialSystem celSystem;

    public Skysphere() {
        celSystem = CoreRegistry.get(CelestialSystem.class);
        time = CoreRegistry.get(Time.class);
    }

    public void render(Camera camera) {
        glDepthMask(false);

        if (camera.isReflected()) {
            glCullFace(GL_BACK);
        } else {
            glCullFace(GL_FRONT);
        }

        Material shader = Assets.getMaterial("engine:prog.sky");
        shader.enable();

        // Draw the skysphere
        drawSkysphere();

        if (camera.isReflected()) {
            glCullFace(GL_FRONT);
        } else {
            glCullFace(GL_BACK);
        }

        glDepthMask(true);
    }

    private void drawSkysphere() {
        if (displayListSphere == -1) {
            displayListSphere = glGenLists(1);

            Sphere sphere = new Sphere();
            sphere.setTextureFlag(true);

            glNewList(displayListSphere, GL11.GL_COMPILE);

            sphere.draw(1024, 16, 128);

            glEndList();
        }

        glCallList(displayListSphere);
    }

    public float getSunPosAngle() {
        return celSystem.getWorldCelestialModel().getSunPosAngle(time.getGameTimeInMs());
    }

    public float getDaylight() {
        float angle = (float) Math.toDegrees(TeraMath.clamp(Math.cos(getSunPosAngle())));
        float daylight = 1.0f;

        if (angle < 24.0f) {
            daylight = 1.0f - (24.0f - angle) / 24.0f;
        }

        return daylight;
    }

    public float getTurbidity() {
        return turbidity;
    }

    public float getColorExp() {
        return colorExp;
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
        Vector3f sunDirection = new Vector3f(0.0f, (float) Math.cos(sunAngle), (float) Math.sin(sunAngle));

        // Moonlight flip
        if (moonlightFlip && sunDirection.y < 0.0f) {
            sunDirection.scale(-1.0f);
        }

        return sunDirection;
    }
}
