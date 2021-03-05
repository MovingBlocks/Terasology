// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.entitySystem.prefab.internal;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PrefabFormat extends AbstractAssetFileFormat<PrefabData> {
    private static final Logger logger = LoggerFactory.getLogger(PrefabFormat.class);

    private ComponentLibrary componentLibrary;
    private TypeHandlerLibrary typeHandlerLibrary;

    public PrefabFormat(ComponentLibrary componentLibrary, TypeHandlerLibrary typeHandlerLibrary) {
        super("prefab");
        this.componentLibrary = componentLibrary;
        this.typeHandlerLibrary = typeHandlerLibrary;
    }

    @Override
    public PrefabData load(ResourceUrn resourceUrn, List<AssetDataFile> inputs) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8))) {
            EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
            if (prefabData != null) {
                if (!prefabData.hasName()) {
                    prefabData = prefabData.toBuilder().setName(resourceUrn.toString()).build();
                }
                logger.debug("Deserializing prefab {} with inputs {}", resourceUrn, inputs);
                PrefabSerializer serializer = new PrefabSerializer(componentLibrary, typeHandlerLibrary);
                return serializer.deserialize(prefabData);
            } else {
                throw new IOException("Failed to read prefab for '" + resourceUrn + "'");
            }
        }
    }

}
