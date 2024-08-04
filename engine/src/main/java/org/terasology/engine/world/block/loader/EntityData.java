// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.loader;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.context.annotation.API;

@API
public class EntityData {
    private Prefab prefab;
    private boolean keepActive;

    public EntityData() {
    }

    public EntityData(EntityData other) {
        this.prefab = other.prefab;
        this.keepActive = other.keepActive;
    }

    public Prefab getPrefab() {
        return prefab;
    }

    public void setPrefab(Prefab prefab) {
        this.prefab = prefab;
    }

    public void setKeepActive(boolean keepActive) {
        this.keepActive = keepActive;
    }

    public boolean isKeepActive() {
        return keepActive;
    }
}
