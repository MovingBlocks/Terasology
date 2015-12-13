/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.world.block.loader;

import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.module.sandbox.API;

/**
 */
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
