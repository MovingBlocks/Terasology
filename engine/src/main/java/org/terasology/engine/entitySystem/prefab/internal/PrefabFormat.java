// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.prefab.internal;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.engine.persistence.serializers.EntityDataJSONFormat;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.engine.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PrefabFormat extends AbstractAssetFileFormat<PrefabData> {
    private static final Logger logger = LoggerFactory.getLogger(PrefabFormat.class);

    private final ComponentLibrary componentLibrary;
    private final TypeHandlerLibrary typeHandlerLibrary;

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
                logger.info("Attempting to deserialize prefab {} with inputs {}", resourceUrn, inputs);
                PrefabSerializer serializer = new PrefabSerializer(componentLibrary, typeHandlerLibrary);
                return serializer.deserialize(prefabData);
            } else {
                throw new IOException("Failed to read prefab for '" + resourceUrn + "'");
            }
        }
    }

}
