// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.stubs;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;

/**
 */
public class EntityRefComponent implements Component {

    public EntityRef entityRef = EntityRef.NULL;

    public EntityRefComponent() {

    }

    public EntityRefComponent(EntityRef ref) {
        this.entityRef = ref;
    }
}
