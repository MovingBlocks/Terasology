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
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 */
public class PrefabFormat extends AbstractAssetFileFormat<PrefabData> {

    private ComponentLibrary componentLibrary;
    private TypeSerializationLibrary typeSerializationLibrary;

    public PrefabFormat(ComponentLibrary componentLibrary, TypeSerializationLibrary typeSerializationLibrary) {
        super("prefab");
        this.componentLibrary = componentLibrary;
        this.typeSerializationLibrary = typeSerializationLibrary;
    }

    @Override
    public PrefabData load(ResourceUrn resourceUrn, List<AssetDataFile> inputs) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8))) {
            EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
            if (prefabData != null) {
                PrefabSerializer serializer = new PrefabSerializer(componentLibrary, typeSerializationLibrary);
                return serializer.deserialize(prefabData);
            } else {
                throw new IOException("Failed to read prefab for '" + resourceUrn + "'");
            }
        }
    }

}
