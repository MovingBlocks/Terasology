// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.nui.input.ActivateMode;
import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.Input;

/**
 *
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
     * @return The circumstance under which this button sends events
     */
    ActivateMode getMode();

    /**
     * Set the circumstance under which this button sends events
     *
     * @param mode
     */
    void setMode(ActivateMode mode);

    /**
     * @return Whether this button sends repeat events while pressed
     */
    boolean isRepeating();

    /**
     * Sets whether this button sends repeat events while pressed
     *
     * @param repeating
     */
    void setRepeating(boolean repeating);

    /**
     * @return The time (in milliseconds) between repeat events being sent
     */
    int getRepeatTime();

    /**
     * @param repeatTimeMs The time (in milliseconds) between repeat events being sent
     */
    void setRepeatTime(int repeatTimeMs);

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
                            Vector3i targetBlockPos,
                            Vector3f hitPosition,
                            Vector3f hitNormal,
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
                Vector3i targetBlockPos,
                Vector3f hitPosition,
                Vector3f hitNormal,
                long gameTimeInMs);

}
