// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.delay;

import org.terasology.gestalt.entitysystem.component.Component;

public class ArbritaryDelayActionComponent implements Component<ArbritaryDelayActionComponent> {
    public int value = 1;

    @Override
    public void copyFrom(ArbritaryDelayActionComponent other) {
        this.value = other.value;
    }
}
