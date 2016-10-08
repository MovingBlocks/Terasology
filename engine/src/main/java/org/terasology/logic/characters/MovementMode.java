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

package org.terasology.logic.characters;

import java.util.ArrayList;
import java.util.List;

/**
 */
public enum MovementMode {
    WALKING(1f, 8f, true, true, true, 3f, false),
    CLIMBING(0f, 8f, true, true, true, 3f, false),
    SWIMMING(0f, 3f, true, false, true, 1.5f, true),
    NEW_SWIMMING(1f, 3f, true, true, true, 1.3f, true),
    SURFACE_SWIMMING(0f, 3f, true, true, true, 2f, true),
    DIVING(0f, 2f, true, false, true, 2f, true),
    NEW_DIVING(1f, 2f, true, false, true, 2f, true),
    GHOSTING(0f, 4f, false, false, false, 5f, true),
    FLYING(0f, 4f, true, false, false, 3f, true),
    NONE(0f, 0f, false, false, false, 0f, true);

    public final float scaleGravity;
    public float volatileScaleGravity;
    public float scaleInertia;
    public boolean useCollision;
    public boolean canBeGrounded;
    public boolean respondToEnvironment;
    public float maxSpeed;
    public boolean applyInertiaToVertical;

    MovementMode(float scaleGravity, float scaleInertia, boolean useCollision, boolean canBeGrounded,
                 boolean respondToEnvironment, float maxSpeed, boolean applyInertiaToVertical) {
        this.scaleGravity = scaleGravity;
        this.volatileScaleGravity = scaleGravity;
        this.scaleInertia = scaleInertia;
        this.useCollision = useCollision;
        this.canBeGrounded = canBeGrounded;
        this.respondToEnvironment = respondToEnvironment;
        this.maxSpeed = maxSpeed;
        this.applyInertiaToVertical = applyInertiaToVertical;
    }

    public void refreshVolatile() {
        this.volatileScaleGravity = scaleGravity;
    }

    static List<MovementMode> getSwimmingModes() {
        List<MovementMode> swimmingModes = new ArrayList<>();
        swimmingModes.add(NEW_SWIMMING);
        swimmingModes.add(SURFACE_SWIMMING);
        swimmingModes.add(SWIMMING);
        return swimmingModes;
    }

    static List<MovementMode> getDivingModes() {
        List<MovementMode> divingModes = new ArrayList<>();
        divingModes.add(NEW_DIVING);
        divingModes.add(DIVING);
        return divingModes;
    }
}
