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

package org.terasology.entitySystem.metadata.extension;

import org.terasology.asset.Assets;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius
 */
public class PrefabTypeHandler extends AbstractTypeHandler<Prefab> {

    public PrefabTypeHandler() {
    }

    @Override
    public EntityData.Value serialize(Prefab value) {
        return EntityData.Value.newBuilder().addString(value.getName()).build();
    }

    @Override
    public Prefab deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return Assets.getPrefab(value.getString(0));
        }
        return null;
    }

    @Override
    public Prefab copy(Prefab value) {
        return value;
    }
}
