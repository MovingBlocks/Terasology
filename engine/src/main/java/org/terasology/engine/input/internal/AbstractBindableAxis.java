// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.internal;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.input.BindAxisEvent;
import org.terasology.engine.input.BindAxisSubscriber;
import org.terasology.engine.input.BindableAxis;
import org.terasology.engine.input.SendEventMode;

import java.util.List;

/**
 * Implements common functionality of {@link BindableAxis}.
 */
public abstract class AbstractBindableAxis implements BindableAxis {
    private String id;
    private BindAxisEvent event;
    private float value;

    private List<BindAxisSubscriber> subscribers = Lists.newArrayList();

    private SendEventMode sendEventMode = SendEventMode.WHEN_NON_ZERO;

    public AbstractBindableAxis(String id, BindAxisEvent event) {
        this.id = id;
        this.event = event;
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

    public void update(EntityRef[] inputEntities, float delta, EntityRef target, Vector3i targetBlockPos, Vector3f hitPosition, Vector3f hitNormal) {

        // TODO: Interpolate, based on some settings (immediate, linear, lerp?)

        float newValue = getTargetValue();

        if (sendEventMode.shouldSendEvent(value, newValue)) {
            event.prepare(id, newValue, delta);
            event.setTargetInfo(target, targetBlockPos, hitPosition, hitNormal);
            for (EntityRef entity : inputEntities) {
                entity.send(event);
                if (event.isConsumed()) {
                    break;
                }
            }
            sendEventToSubscribers(delta, target);
        }
        value = newValue;
    }

    protected abstract float getTargetValue();

    private void sendEventToSubscribers(float delta, EntityRef target) {
        for (BindAxisSubscriber subscriber : subscribers) {
            subscriber.update(value, delta, target);
        }
    }
}
