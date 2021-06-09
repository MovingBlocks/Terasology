// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Owns;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

public class OwnerComponent implements Component<OwnerComponent> {
    @Owns
    public EntityRef child = EntityRef.NULL;

    @Override
    public void copy(OwnerComponent other) {
        this.child = other.child;
    }
}
