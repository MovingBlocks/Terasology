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
import org.terasology.asset.AssetLoader;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;

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

    public PrefabLoader() {
    }

    @Override
    public PrefabData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
        EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
        if (prefabData != null) {
            EngineEntityManager entityManager = CoreRegistry.get(EngineEntityManager.class);
            return new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary()).deserialize(prefabData);
        }
        return null;
    }
}
