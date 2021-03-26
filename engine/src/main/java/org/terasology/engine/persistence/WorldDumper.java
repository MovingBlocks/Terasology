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
package org.terasology.engine.persistence;

import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.persistence.serializers.EntityDataJSONFormat;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializerImpl;
import org.terasology.protobuf.EntityData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Used to create a dump of the current state of the world (specifically, entities)
 *
 */
public class WorldDumper {

    private WorldSerializer persisterHelper;

    public WorldDumper(EngineEntityManager entityManager, PrefabSerializer prefabSerializer) {
        this.persisterHelper = new WorldSerializerImpl(entityManager, prefabSerializer);
    }

    /**
     * Save all world entities to file
     * @param file  path to file in which entities will be saved
     * @return number of saved entities and prefabs
     * @throws IOException thrown when error occurs while saving world to file
     */
    public int save(Path file) throws IOException {
        final EntityData.GlobalStore world = persisterHelper.serializeWorld(true);

        Path parentFile = file.toAbsolutePath().getParent();
        if (!Files.isDirectory(parentFile)) {
            Files.createDirectories(parentFile);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file, TerasologyConstants.CHARSET)) {
            EntityDataJSONFormat.write(world, writer);
        }
        return world.getEntityCount() + world.getPrefabCount();
    }

    /***
     * Save World entities, which only contain some of Components
     * @param file  path to file in which entities will be saved
     * @param filterComponents list of component classes to filter by World entities
     * @return number of saved entities and prefabs
     * @throws IOException thrown when error occurs while saving world to file
     */
    public int save(Path file, List<Class<? extends Component>> filterComponents) throws IOException {
        final EntityData.GlobalStore world = persisterHelper.serializeWorld(true, filterComponents);

        Path parentFile = file.toAbsolutePath().getParent();
        if (!Files.isDirectory(parentFile)) {
            Files.createDirectories(parentFile);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(file, TerasologyConstants.CHARSET)) {
            EntityDataJSONFormat.write(world, writer);
        }
        return world.getEntityCount() + world.getPrefabCount();
    }
}
