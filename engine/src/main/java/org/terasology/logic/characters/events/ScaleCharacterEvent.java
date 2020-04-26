/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.logic.characters.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.logic.characters.CharacterMovementComponent;

import java.util.Optional;

/**
 * Indicate that a character entity should be scaled by some factor.
 */
public class ScaleCharacterEvent implements Event {
    /**
     * The factor by which the target entity should be scaled.
     */
    public float factor;

    /**
     * Create a new scale event with the given factor.
     * <p>
     * The factor cannot be zero or negative. To scale a character down, choose a value in (0, 1), to scale it up use
     * a factor greater 1.
     *
     * @param factor the scaling factor
     */
    public ScaleCharacterEvent(final float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("zero or negative factor");
        }
        this.factor = factor;
    }

    /**
     * Create a scale event with given factor based on the given entity.
     * <p>
     * The factor cannot be zero or negative. To scale a character down, choose a value in (0, 1), to scale it up use
     * a factor greater 1.
     *
     * @param entity the entity supposed to be scaled (should have a {@link CharacterMovementComponent}
     * @param factor the factor to scale the character entity by
     * @return an event that can be sent to scale the entity
     */
    public static ScaleCharacterEvent scaleByFactor(final EntityRef entity, final float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("zero or negative factor");
        }

        return new ScaleCharacterEvent(factor);
    }

    /**
     * Create a scale event to an absolute value based on the given entity.
     * <p>
     * The target value cannot be zero or negative. If the given entity does not have {@link CharacterMovementComponent}
     * the default character height will be used to determine the scaling factor.
     *
     * @param entity       the entity supposed to be scaled (should have a {@link CharacterMovementComponent}
     * @param targetHeight the target height to scale the character entity to
     * @return an event that can be sent to scale the entity to the desired value
     */
    public static ScaleCharacterEvent scaleToValue(final EntityRef entity, final float targetHeight) {
        if (targetHeight <= 0) {
            throw new IllegalArgumentException("zero or negative target height");
        }

        CharacterMovementComponent movementComponent =
                Optional.ofNullable(entity.getComponent(CharacterMovementComponent.class))
                        .orElse(new CharacterMovementComponent());
        return new ScaleCharacterEvent(targetHeight / movementComponent.height);
    }
}
