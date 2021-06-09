// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;

public class EntityRefComponent implements Component<EntityRefComponent> {

    public EntityRef entityRef = EntityRef.NULL;

    public EntityRefComponent() {

    }

    public EntityRefComponent(EntityRef ref) {
        this.entityRef = ref;
    }

    @Override
    public void copy(EntityRefComponent other) {
        this.entityRef = other.entityRef;
    }
}
