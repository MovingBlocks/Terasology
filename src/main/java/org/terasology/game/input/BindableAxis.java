/*
 * Copyright 2012
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

package org.terasology.game.input;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.manager.GUIManager;

import java.util.List;

/**
 * A Bind Axis is an simulated analog input axis, maintaining a value between -1 and 1.  It is linked to
 * a positive BindableButton (that pushes the axis towards 1) and a negative BindableButton (that pushes it towards -1)
 *
 * @author Immortius
 */
public class BindableAxis
{
    private String id;
    private BindAxisEvent event;
    private BindableButton positiveInput;
    private BindableButton negativeInput;
    private float value = 0;

    private List<BindAxisSubscriber> subscribers = Lists.newArrayList();

    private SendEventMode sendEventMode = SendEventMode.WHEN_NON_ZERO;

    public enum SendEventMode {
        ALWAYS {
            @Override
            public boolean shouldSendEvent(float oldValue, float newValue) {
                return true;
            }
        },
        WHEN_NON_ZERO {
            @Override
            public boolean shouldSendEvent(float oldValue, float newValue) {
                return newValue != 0;
            }
        },
        WHEN_CHANGED {
            @Override
            public boolean shouldSendEvent(float oldValue, float newValue) {
                return oldValue != newValue;
            }
        };

        public abstract boolean shouldSendEvent(float oldValue, float newValue);
    }

    public BindableAxis(String id, BindAxisEvent event, BindableButton positiveButton, BindableButton negativeButton) {
        this.id = id;
        this.event = event;
        this.positiveInput = positiveButton;
        this.negativeInput = negativeButton;
    }

    public void setSendEventMode(SendEventMode mode) {
        sendEventMode = mode;
    }

    public SendEventMode getSendEventMode() {
        return sendEventMode;
    }

    public void subscribe(BindAxisSubscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    public void unsubscribe(BindAxisSubscriber subscriber) {
        this.subscribers.remove(subscriber);
    }

    public float getValue() {
        return value;
    }

    void update(EntityRef localPlayer, float delta, EntityRef target) {
        boolean posInput = positiveInput.getState() == ButtonState.DOWN;
        boolean negInput = negativeInput.getState() == ButtonState.DOWN;

        float targetValue = 0;
        if (!GUIManager.getInstance().isConsumingInput()) {
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
            event.prepare(id, newValue, delta, target);
            localPlayer.send(event);
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
