// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

@ForceBlockActive(retainUnalteredOnBlockChange = true)
public class RetainedOnBlockChangeComponent implements Component<RetainedOnBlockChangeComponent> {
    public int value;

    public RetainedOnBlockChangeComponent() {
    }

    public RetainedOnBlockChangeComponent(int value) {
        this.value = value;
    }

    @Override
    public void copyFrom(RetainedOnBlockChangeComponent other) {
        this.value = other.value;
    }
}
