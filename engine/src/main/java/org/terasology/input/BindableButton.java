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

package org.terasology.input;

import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;

/**
 */
public interface BindableButton {

    /**
     * @return The identifier for this button
     */
    SimpleUri getId();

    /**
     * @return The display name for this button
     */
    String getDisplayName();

    /**
     * Set the circumstance under which this button sends events
     *
     * @param mode
     */
    void setMode(ActivateMode mode);

    /**
     * @return The circumstance under which this button sends events
     */
    ActivateMode getMode();

    /**
     * Sets whether this button sends repeat events while pressed
     *
     * @param repeating
     */
    void setRepeating(boolean repeating);

    /**
     * @return Whether this button sends repeat events while pressed
     */
    boolean isRepeating();

    /**
     * @param repeatTimeMs The time (in milliseconds) between repeat events being sent
     */
    void setRepeatTime(int repeatTimeMs);

    /**
     * @return The time (in milliseconds) between repeat events being sent
     */
    int getRepeatTime();

    /**
     * @return The current state of this button (either up or down)
     */
    ButtonState getState();

    /**
     * Used to directly subscribe to the button's events
     *
     * @param subscriber
     */
    void subscribe(BindButtonSubscriber subscriber);

    /**
     * Used to unsubscribe from the button's event
     *
     * @param subscriber
     */
    void unsubscribe(BindButtonSubscriber subscriber);

    /**
     * Updates this bind with the new state of a bound button. This should be done whenever a bound button changes
     * state, so that the overall state of the bind can be tracked.
     *
     * @param pressed Is the changing
     * @param delta The length of the current frame
     * @param inputEntities The entities which receive the input events
     * @param target The current camera target
     * @param targetBlockPos The current targeted block position
     * @param hitPosition The current hit position
     * @param hitNormal The current hit normal
     * @param initialKeyConsumed Has the changing button's event already been consumed
     * @param gameTimeInMs The game time in milliseconds.
     * @return Whether the button's event has been consumed
     */
    boolean updateBindState(Input input,
                            boolean pressed,
                            float delta,
                            EntityRef[] inputEntities,
                            EntityRef target,
                            Vector3ic targetBlockPos,
                            Vector3fc hitPosition,
                            Vector3fc hitNormal,
                            boolean initialKeyConsumed,
                            long gameTimeInMs);

    /**
     * Updates this bind. If the binding is repeating, this will trigger the binding event with the {@link
     * ButtonState#REPEAT} state.
     *
     * @param inputEntities The entities which receive the input events
     * @param delta The length of the current frame
     * @param target The current camera target
     * @param targetBlockPos The current targeted block position
     * @param hitPosition The current hit position
     * @param hitNormal The current hit normal
     * @param gameTimeInMs The game time in milliseconds.
     */
    void update(EntityRef[] inputEntities,
                float delta,
                EntityRef target,
                Vector3ic targetBlockPos,
                Vector3fc hitPosition,
                Vector3fc hitNormal,
                long gameTimeInMs);

}
