/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.items.components;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.module.sandbox.API;
import org.terasology.network.FieldReplicateType;
import org.terasology.network.Replicate;
import org.terasology.rendering.assets.texture.TextureRegionAsset;

@API
public class ItemComponent implements Component {

    /**
     * Name of the icon this item should be rendered with
     */
    public TextureRegionAsset<?> icon;

    public int cooldownTime = 200;
    public Prefab onDroppedPrefab;
    public boolean exclusiveStack = false;

    //TODO: Remove to health system
    public int baseDamage = 1;
    public Prefab damageType;
}
