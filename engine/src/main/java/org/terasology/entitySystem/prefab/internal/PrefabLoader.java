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
package org.terasology.entitySystem.prefab.internal;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.terasology.asset.AssetLoader;
import org.terasology.engine.module.Module;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * @author Immortius
 */
public class PrefabLoader implements AssetLoader<PrefabData> {

    @Override
    public PrefabData load(Module module, InputStream stream, List<URL> urls, List<URL> deltas) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
        EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
        if (prefabData != null) {
            EngineEntityManager entityManager = CoreRegistry.get(EngineEntityManager.class);
            List<EntityData.Prefab> deltaData = Lists.newArrayListWithCapacity(deltas.size());
            for (URL deltaUrl : deltas) {
                try (BufferedReader deltaReader = new BufferedReader(new InputStreamReader(deltaUrl.openStream(), Charsets.UTF_8))) {
                    EntityData.Prefab delta = EntityDataJSONFormat.readPrefab(deltaReader);
                    deltaData.add(delta);
                }
            }
            PrefabSerializer serializer = new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary());
            return serializer.deserialize(prefabData, deltaData);
        }
        return null;
    }
}
