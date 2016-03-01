/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.health;

import org.terasology.utilities.Assets;
import org.terasology.entitySystem.prefab.Prefab;

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

    private EngineDamageTypes(String prefabId) {
        this.prefabId = prefabId;
    }

    public Prefab get() {
        return Assets.getPrefab(prefabId).get();
    }

}
