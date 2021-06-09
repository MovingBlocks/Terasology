// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.actions;

import org.terasology.gestalt.entitysystem.component.Component;

public class SpawnPrefabActionComponent implements Component<SpawnPrefabActionComponent> {
    public String prefab;
    public ActionTarget spawnLocationRelativeTo = ActionTarget.Target;

    @Override
    public void copy(SpawnPrefabActionComponent other) {
        this.prefab = other.prefab;
        this.spawnLocationRelativeTo = other.spawnLocationRelativeTo;
    }
}
