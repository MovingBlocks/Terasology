/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem.prefab;

import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.metadata.ComponentLibrary;
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
    public PrefabData load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
        if (prefabData != null) {
            return new PrefabSerializer(CoreRegistry.get(ComponentLibrary.class)).deserialize(prefabData);
        }
        return null;
    }
}
