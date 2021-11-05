// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence;

import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.persistence.serializers.EntityDataJSONFormat;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializer;
import org.terasology.engine.persistence.serializers.WorldSerializerImpl;
import org.terasology.gestalt.entitysystem.component.Component;
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
