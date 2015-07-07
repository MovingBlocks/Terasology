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
package org.terasology.input.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.ActivateMode;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.BindButtonSubscriber;
import org.terasology.input.BindableButton;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.List;
import java.util.Set;

/**
 * A BindableButton is pseudo button that is controlled by one or more actual inputs (whether keys, mouse buttons or the
 * mouse wheel).
 * <br><br>
 * When the BindableButton changes state it sends out events like an actual key or button does. It also allows direct
 * subscription via the {@link org.terasology.input.BindButtonSubscriber} interface.
 */
public class BindableButtonImpl implements BindableButton {

    private SimpleUri id;
    private String displayName;
    private BindButtonEvent buttonEvent;
    private Set<Input> activeInputs = Sets.newHashSet();

    private List<BindButtonSubscriber> subscribers = Lists.newArrayList();
    private ActivateMode mode = ActivateMode.BOTH;
    private boolean repeating;
    private int repeatTime;
    private long lastActivateTime;

    private boolean consumedActivation;
    private Time time;

    /**
     * Creates the button. Package-private, as should be created through the InputSystem
     *
     * @param id
     * @param event
     */
    public BindableButtonImpl(SimpleUri id, String displayName, BindButtonEvent event, Time time) {
        this.id = id;
        this.displayName = displayName;
        this.buttonEvent = event;
        this.time = time;
    }

    @Override
    public SimpleUri getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setMode(ActivateMode mode) {
        this.mode = mode;
    }

    @Override
    public ActivateMode getMode() {
        return mode;
    }

    @Override
    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    @Override
    public boolean isRepeating() {
        return repeating;
    }

    /**
     * Sets the repeat time
     *
     * @param repeatTimeMs The time between repeat events, in ms
     */
    @Override
    public void setRepeatTime(int repeatTimeMs) {
        this.repeatTime = repeatTimeMs;
    }

    @Override
    public int getRepeatTime() {
        return repeatTime;
    }

    @Override
    public ButtonState getState() {
        return (activeInputs.isEmpty() || consumedActivation) ? ButtonState.UP : ButtonState.DOWN;
    }

    /**
     * Register a subscriber to this bind
     *
     * @param subscriber
     */
    @Override
    public void subscribe(BindButtonSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * Removes a subscriber from this bind
     *
     * @param subscriber
     */
    @Override
    public void unsubscribe(BindButtonSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Updates this bind with the new state of a bound button. This should be done whenever a bound button changes
     * state, so that the overall state of the bind can be tracked.
     *
     * @param pressed            Is the changing
     * @param delta              The length of the current frame
     * @param target             The current camera target
     * @param initialKeyConsumed Has the changing button's event already been consumed
     * @return Whether the button's event has been consumed
     */
    public boolean updateBindState(Input input,
                                   boolean pressed,
                                   float delta,
                                   EntityRef[] inputEntities,
                                   EntityRef target,
                                   Vector3i targetBlockPos,
                                   Vector3f hitPosition,
                                   Vector3f hitNormal,
                                   boolean initialKeyConsumed) {
        boolean keyConsumed = initialKeyConsumed;
        if (pressed) {
            boolean previouslyEmpty = activeInputs.isEmpty();
            activeInputs.add(input);
            if (previouslyEmpty && mode.isActivatedOnPress()) {
                lastActivateTime = time.getGameTimeInMs();
                consumedActivation = keyConsumed;
                if (!keyConsumed) {
                    keyConsumed = triggerOnPress(delta, target);
                }
                if (!keyConsumed) {
                    buttonEvent.prepare(id, ButtonState.DOWN, delta);
                    buttonEvent.setTargetInfo(target, targetBlockPos, hitPosition, hitNormal);
                    for (EntityRef entity : inputEntities) {
                        entity.send(buttonEvent);
                        if (buttonEvent.isConsumed()) {
                            break;
                        }
                    }
                    keyConsumed = buttonEvent.isConsumed();
                }
            }
        } else if (!activeInputs.isEmpty()) {
            activeInputs.remove(input);
            if (activeInputs.isEmpty() && mode.isActivatedOnRelease()) {
                if (!keyConsumed) {
                    keyConsumed = triggerOnRelease(delta, target);
                }
                if (!keyConsumed) {
                    buttonEvent.prepare(id, ButtonState.UP, delta);
                    buttonEvent.setTargetInfo(target, targetBlockPos, hitPosition, hitNormal);
                    for (EntityRef entity : inputEntities) {
                        entity.send(buttonEvent);
                        if (buttonEvent.isConsumed()) {
                            break;
                        }
                    }
                    keyConsumed = buttonEvent.isConsumed();
                }
            }
        }
        return keyConsumed;
    }

    public void update(EntityRef[] inputEntities, float delta, EntityRef target, Vector3i targetBlockPos, Vector3f hitPosition, Vector3f hitNormal) {
        long activateTime = this.time.getGameTimeInMs();
        if (repeating && getState() == ButtonState.DOWN && mode.isActivatedOnPress() && activateTime - lastActivateTime > repeatTime) {
            lastActivateTime = activateTime;
            if (!consumedActivation) {
                boolean consumed = triggerOnRepeat(delta, target);
                if (!consumed) {
                    buttonEvent.prepare(id, ButtonState.REPEAT, delta);
                    buttonEvent.setTargetInfo(target, targetBlockPos, hitPosition, hitNormal);
                    for (EntityRef entity : inputEntities) {
                        entity.send(buttonEvent);
                        if (buttonEvent.isConsumed()) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean triggerOnPress(float delta, EntityRef target) {
        for (BindButtonSubscriber subscriber : subscribers) {
            if (subscriber.onPress(delta, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean triggerOnRepeat(float delta, EntityRef target) {
        for (BindButtonSubscriber subscriber : subscribers) {
            if (subscriber.onRepeat(delta, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean triggerOnRelease(float delta, EntityRef target) {
        for (BindButtonSubscriber subscriber : subscribers) {
            if (subscriber.onRelease(delta, target)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BindableButtonEventImpl [" + id + ", \"" + displayName + "\", " + buttonEvent + "]";
    }
}
