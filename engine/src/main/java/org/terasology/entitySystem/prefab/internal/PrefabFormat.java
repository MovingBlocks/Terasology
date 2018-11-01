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
package org.terasology.entitySystem.prefab.internal;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.ModuleDependenciesComponent;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.module.ModuleEnvironment;
import org.terasology.naming.Name;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Defines how prefab data should be loaded from asset data
 */
public class PrefabFormat extends AbstractAssetFileFormat<PrefabData> {
    private static final Logger logger = LoggerFactory.getLogger(PrefabFormat.class);

    private ComponentLibrary componentLibrary;
    private TypeSerializationLibrary typeSerializationLibrary;
    private ModuleEnvironment environment;

    public PrefabFormat(ComponentLibrary componentLibrary, TypeSerializationLibrary typeSerializationLibrary, ModuleEnvironment environment) {
        super("prefab");
        this.componentLibrary = componentLibrary;
        this.environment = environment;
        this.typeSerializationLibrary = typeSerializationLibrary;
    }

    /**
     * @inheritDoc
     */
    @Override
    public PrefabData load(ResourceUrn resourceUrn, List<AssetDataFile> inputs) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8))) {
            EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
            if (prefabData != null) {
                logger.info("Attempting to deserialize prefab {} with inputs {}", resourceUrn, inputs);
                PrefabSerializer serializer = new PrefabSerializer(componentLibrary, typeSerializationLibrary);
                return checkOptionalDependencies(serializer.deserialize(prefabData));
            } else {
                throw new IOException("Failed to read prefab for '" + resourceUrn + "'");
            }
        }
    }

    /**
     * Checks if the prefab has any optional dependencies.
     * These should be specified in a {@link ModuleDependenciesComponent} on the prefab
     *
     * @param data The deserialised prefab data
     * @return The data if it should be loaded, null otherwise.
     */
    private PrefabData checkOptionalDependencies(PrefabData data) {
        if (environment != null && data.hasComponent(ModuleDependenciesComponent.class)) {
            ModuleDependenciesComponent component = data.getComponent(ModuleDependenciesComponent.class);
            for (String name : component.modules) {
                // TODO: Add add an `isPresent` method to `ModuleEnvironment` in  gestalt.
                if (environment.get(new Name(name)) == null) {
                    return null;
                }
            }
        }
        return data;
    }

}
