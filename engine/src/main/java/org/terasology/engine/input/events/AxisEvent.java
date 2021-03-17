// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.events;


/**
 */
public abstract class AxisEvent extends InputEvent {

    public AxisEvent(float delta) {
        super(delta);
    }

    public abstract double getValue();
}
