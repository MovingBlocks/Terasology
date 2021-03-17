// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input;

import org.terasology.engine.input.events.AxisEvent;

/**
 */
public class BindAxisEvent extends AxisEvent {

    private String id;
    private float value;

    public BindAxisEvent() {
        super(0);
    }

    @Override
    public double getValue() {
        return value;
    }

    public void prepare(String axisId, float axisValue, float delta) {
        reset(delta);
        this.id = axisId;
        this.value = axisValue;
    }

    public String getId() {
        return id;
    }

}
