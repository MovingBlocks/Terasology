/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * A BindableButton is pseudo button that is controlled by one or more actual inputs (whether keys, mouse buttons or the
 * mouse wheel).
 * <p/>
 * When the BindableButton changes state it sends out events like an actual key or button does. It also allows direct
 * subscription via the {@link BindButtonSubscriber} interface.
 */
public class BindableButtonImpl implements BindableButton {

    private String id;
    private String displayName;
    private BindButtonEvent buttonEvent;
    private int activeInputs = 0;

    private List<BindButtonSubscriber> subscribers = Lists.newArrayList();
    private ActivateMode mode = ActivateMode.BOTH;
    private boolean repeating = false;
    private int repeatTime = 0;
    private long lastActivateTime;

    private Timer timer;

    /**
     * Creates the button. Package-private, as should be created through the InputSystem
     *
     * @param id
     * @param event
     */
    BindableButtonImpl(String id, String displayName, BindButtonEvent event) {
        this.id = id;
        this.displayName = displayName;
        this.buttonEvent = event;
        timer = CoreRegistry.get(Timer.class);
    }

    @Override
    public String getId() {
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
        return (activeInputs > 0) ? ButtonState.DOWN : ButtonState.UP;
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
     * @param pressed     Is the changing
     * @param delta       The length of the current frame
     * @param target      The current camera target
     * @param keyConsumed Has the changing button's event already been consumed
     * @param guiOnly     Is the gui consuming input
     * @return Whether the button's event has been consumed
     */
    boolean updateBindState(boolean pressed, float delta, EntityRef[] inputEntities, EntityRef target, Vector3i targetBlockPos, Vector3f hitPosition, Vector3f hitNormal, boolean keyConsumed, boolean guiOnly) {
        if (pressed) {
            activeInputs++;
            if (activeInputs == 1 && mode.isActivatedOnPress()) {
                lastActivateTime = timer.getTimeInMs();
                if (!keyConsumed) {
                    keyConsumed = triggerOnPress(delta, target);
                }
                if (!keyConsumed) {
                    buttonEvent.prepare(id, ButtonState.DOWN, delta);
                    buttonEvent.setTarget(target, targetBlockPos, hitPosition, hitNormal);
                    for (EntityRef entity : inputEntities) {
                        entity.send(buttonEvent);
                    }
                    keyConsumed = buttonEvent.isConsumed();
                }
            }
        } else if (activeInputs != 0) {
            activeInputs--;
            if (activeInputs == 0 && mode.isActivatedOnRelease()) {
                if (!keyConsumed) {
                    keyConsumed = triggerOnRelease(delta, target);
                }
                if (!keyConsumed) {
                    buttonEvent.prepare(id, ButtonState.UP, delta);
                    buttonEvent.setTarget(target, targetBlockPos, hitPosition, hitNormal);
                    for (EntityRef entity : inputEntities) {
                        entity.send(buttonEvent);
                    }
                    keyConsumed = buttonEvent.isConsumed();
                }
            }
        }
        return keyConsumed;
    }

    void update(EntityRef[] inputEntities, float delta, EntityRef target, Vector3i targetBlockPos, Vector3f hitPosition, Vector3f hitNormal) {
        long time = timer.getTimeInMs();
        if (repeating && getState() == ButtonState.DOWN && mode.isActivatedOnPress() && time - lastActivateTime > repeatTime) {
            lastActivateTime = time;
            if (!CoreRegistry.get(GUIManager.class).isConsumingInput()) {
                boolean consumed = triggerOnRepeat(delta, target);
                if (!consumed) {
                    buttonEvent.prepare(id, ButtonState.REPEAT, delta);
                    buttonEvent.setTarget(target, targetBlockPos, hitPosition, hitNormal);
                    for (EntityRef entity : inputEntities) {
                        entity.send(buttonEvent);
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
}
