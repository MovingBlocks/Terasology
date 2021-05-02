// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.ForceBlockActive;

/**
 */
@ForceBlockActive(retainUnalteredOnBlockChange = true)
public class RetainedOnBlockChangeComponent implements Component {
    public int value;

    public RetainedOnBlockChangeComponent() {
    }

    public RetainedOnBlockChangeComponent(int value) {
        this.value = value;
    }
}
