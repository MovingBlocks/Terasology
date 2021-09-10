// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.backdrop;

import org.joml.Vector3f;
import org.terasology.engine.context.Context;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.math.TeraMath;
import org.terasology.nui.properties.Range;

/**
 * Skysphere based on the Perez all weather luminance model.
 */
public class Skysphere implements BackdropProvider {

    @Range(min = 0.01f, max = 100.0f)
    private float colorExp = 0.01f;
    @Range(min = 2.0f, max = 32.0f)
    private float turbidity = 9.0f;

    private final CelestialSystem celSystem;

    public Skysphere(Context context) {
        celSystem = context.get(CelestialSystem.class);
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
