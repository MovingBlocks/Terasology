// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.health;

import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.utilities.Assets;

/**
 * Helper enum for getting engine damage type prefabs.
 * TODO: Should this really be an engine mechanism?
 */
public enum EngineDamageTypes {
    /**
     * Raw damage in the most absolute form.
     */
    DIRECT("engine:directDamage"),
    PHYSICAL("engine:physicalDamage"),
    EXPLOSIVE("engine:explosiveDamage"),
    DROWNING("engine:drowningDamage"),
    /**
     * This is used if healing is flipped to damage (like the old healing hurts undead gimmick)
     */
    HEALING("engine:healingDamage");

    private String prefabId;

    EngineDamageTypes(String prefabId) {
        this.prefabId = prefabId;
    }

    public Prefab get() {
        return Assets.getPrefab(prefabId).get();
    }

}
