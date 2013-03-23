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
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;
import java.util.List;

/**
 * A Bind Axis is an simulated analog input axis, maintaining a value between -1 and 1.  It is linked to
 * a positive BindableButton (that pushes the axis towards 1) and a negative BindableButton (that pushes it towards -1)
 *
 * @author Immortius
 */
public class BindableAxisImpl implements BindableAxis {
    private String id;
    private BindAxisEvent event;
    private BindableButton positiveInput;
    private BindableButton negativeInput;
    private float value = 0;

    private List<BindAxisSubscriber> subscribers = Lists.newArrayList();

    private SendEventMode sendEventMode = SendEventMode.WHEN_NON_ZERO;

    public BindableAxisImpl(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        this.id = id;
        this.event = event;
        this.positiveInput = positiveButton;
        this.negativeInput = negativeButton;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setSendEventMode(SendEventMode mode) {
        sendEventMode = mode;
    }

    @Override
    public SendEventMode getSendEventMode() {
        return sendEventMode;
    }

    @Override
    public void subscribe(BindAxisSubscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    @Override
    public void unsubscribe(BindAxisSubscriber subscriber) {
        this.subscribers.remove(subscriber);
    }

    @Override
    public float getValue() {
        return value;
    }

    void update(EntityRef[] inputEntities, float delta, EntityRef target, Vector3i targetBlockPos, Vector3f hitPosition, Vector3f hitNormal) {
        boolean posInput = positiveInput.getState() == ButtonState.DOWN;
        boolean negInput = negativeInput.getState() == ButtonState.DOWN;

        float targetValue = 0;
        if (!CoreRegistry.get(GUIManager.class).isConsumingInput()) {
            if (posInput) {
                targetValue += 1.0f;
            }
            if (negInput) {
                targetValue -= 1.0f;
            }
        }

        // TODO: Interpolate, based on some settings (immediate, linear, lerp?)

        float newValue = targetValue;

        if (sendEventMode.shouldSendEvent(value, newValue)) {
            event.prepare(id, newValue, delta);
            event.setTarget(target, targetBlockPos, hitPosition, hitNormal);
            for (EntityRef entity : inputEntities) {
                entity.send(event);
                if (event.isCancelled()) {
                    break;
                }
            }
            sendEventToSubscribers(delta, target);
        }
        value = newValue;
    }

    private void sendEventToSubscribers(float delta, EntityRef target) {
        for (BindAxisSubscriber subscriber : subscribers) {
            subscriber.update(value, delta, target);
        }
    }

}
