// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.prefab.internal;

import com.google.common.base.Charsets;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.persistence.serializers.EntityDataJSONFormat;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.gestalt.assets.format.AbstractAssetAlterationFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 */
public class PrefabDeltaFormat extends AbstractAssetAlterationFileFormat<PrefabData> {

    private final ComponentLibrary componentLibrary;
    private final TypeHandlerLibrary typeHandlerLibrary;

    public PrefabDeltaFormat(ComponentLibrary componentLibrary, TypeHandlerLibrary typeHandlerLibrary) {
        super("prefab");
        this.componentLibrary = componentLibrary;
        this.typeHandlerLibrary = typeHandlerLibrary;
    }

    @Override
    public void apply(AssetDataFile assetDataFile, PrefabData assetData) throws IOException {

        try (BufferedReader deltaReader = new BufferedReader(new InputStreamReader(assetDataFile.openStream(), Charsets.UTF_8))) {
            EntityData.Prefab delta = EntityDataJSONFormat.readPrefab(deltaReader);
            PrefabSerializer serializer = new PrefabSerializer(componentLibrary, typeHandlerLibrary);
            serializer.deserializeDeltaOnto(delta, assetData);
        }
    }
}
