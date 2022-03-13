// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.BindButtonSubscriber;
import org.terasology.engine.input.BindableButton;
import org.terasology.input.ActivateMode;
import org.terasology.input.ButtonState;
import org.terasology.input.Input;

import java.util.List;
import java.util.Set;

/**
 * A BindableButton is pseudo button that is controlled by one or more actual inputs (whether keys, mouse buttons or the
 * mouse wheel).
 * <br><br>
 * When the BindableButton changes state it sends out events like an actual key or button does. It also allows direct
 * subscription via the {@link BindButtonSubscriber} interface.
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

    /**
     *
     * @param id The id of the binding
     * @param displayName Readable name of the binding
     * @param event The event to send when the binding is updated
     */
    public BindableButtonImpl(SimpleUri id, String displayName, BindButtonEvent event) {
        this.id = id;
        this.displayName = displayName;
        this.buttonEvent = event;
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

    @Override
    public boolean updateBindState(Input input,
                                   boolean pressed,
                                   float delta,
                                   EntityRef[] inputEntities,
                                   EntityRef target,
                                   Vector3ic targetBlockPos,
                                   Vector3fc hitPosition,
                                   Vector3fc hitNormal,
                                   boolean initialKeyConsumed,
                                   long gameTimeInMs) {
        boolean keyConsumed = initialKeyConsumed;
        if (pressed) {
            boolean previouslyEmpty = activeInputs.isEmpty();
            activeInputs.add(input);
            if (previouslyEmpty && mode.isActivatedOnPress()) {
                lastActivateTime = gameTimeInMs;
                consumedActivation = keyConsumed;
                if (!keyConsumed) {
                    keyConsumed = triggerOnPress(delta, target);
                }
                if (!keyConsumed) {
                    buttonEvent.prepare(id, ButtonState.DOWN, delta);
                    buttonEvent.setTargetInfo(target,
                        targetBlockPos,
                        hitPosition,
                        hitNormal);
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
                    buttonEvent.setTargetInfo(target,
                        targetBlockPos,
                        hitPosition,
                        hitNormal);
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

    @Override
    public void update(EntityRef[] inputEntities,
                       float delta,
                       EntityRef target,
                       Vector3ic targetBlockPos,
                       Vector3fc hitPosition,
                       Vector3fc hitNormal,
                       long gameTimeInMs) {
        long activateTime = gameTimeInMs;
        if (repeating && getState() == ButtonState.DOWN && mode.isActivatedOnPress() && activateTime - lastActivateTime > repeatTime) {
            lastActivateTime = activateTime;
            if (!consumedActivation) {
                boolean consumed = triggerOnRepeat(delta, target);
                if (!consumed) {
                    buttonEvent.prepare(id, ButtonState.REPEAT, delta);
                    buttonEvent.setTargetInfo(target,
                        targetBlockPos,
                        hitPosition,
                        hitNormal);
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
