// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.actions;

import org.terasology.engine.entitySystem.Component;

public class SpawnPrefabActionComponent implements Component {
    public String prefab;
    public ActionTarget spawnLocationRelativeTo = ActionTarget.Target;
}
