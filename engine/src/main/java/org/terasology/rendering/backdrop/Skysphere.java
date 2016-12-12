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

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import org.terasology.utilities.Assets;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.properties.Range;
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

    public Skysphere() {
        celSystem = CoreRegistry.get(CelestialSystem.class);
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

    /**
     * Get the strength of the light for the current day part, based on sun/moon visibility.
     * @return the strength of the current light in [0;1] range.
     */
    @Override
    public float getDaylight() {
        float lightBasedOnTimeOfDay = 1.0f;
        final float ambientLight = 0.2f; // not totally black when the sun goes down, but the moon is not yet up
        double radSunAngle = getSunPositionAngle(); // expected [-PI;PI] for the day cycle to work correctly!

        // if the degree of the sun/moon is less than "lessLightUnderAngle", then we gradually increase the light, instead of full intensity
        final double lessLightUnderAngle = Math.sin(Math.toRadians(15.0));
        double absSinSunAngle = Math.abs(Math.sin(radSunAngle));
        if (absSinSunAngle < lessLightUnderAngle) {
            lightBasedOnTimeOfDay = (float)(absSinSunAngle / lessLightUnderAngle);
        }
        // otherwise the light remains full intensity

        // if the moon is out, we want to reduce the brightness
        if(radSunAngle < 0.0) {
            lightBasedOnTimeOfDay *= 0.2f;
        }

        return ambientLight + (lightBasedOnTimeOfDay*(1.0f-ambientLight));
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
        Vector3f sunDirection = new Vector3f(0.0f, (float) Math.sin(sunAngle), (float) Math.cos(sunAngle));

        // Moonlight flip
        if (moonlightFlip && sunDirection.y < 0.0f) {
            sunDirection.scale(-1.0f);
        }

        return sunDirection;
    }
}
