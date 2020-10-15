// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;

import java.util.Iterator;
import java.util.Map;

/**
 *
 */
@ExpectedCost(1)
public class CreateWorldEntity extends SingleStepLoadProcess {

    @In
    private GameManifest gameManifest;

    @In
    private EntityManager entityManager;
    @In
    private ChunkProvider chunkProvider;
    @In
    private WorldGenerator worldGenerator;
    @In
    private Config config;

    @Override
    public String getMessage() {
        return "Creating World Entity...";
    }

    @Override
    public boolean step() {
        Iterator<EntityRef> worldEntityIterator = entityManager.getEntitiesWith(WorldComponent.class).iterator();
        if (worldEntityIterator.hasNext()) {
            EntityRef worldEntity = worldEntityIterator.next();
            chunkProvider.setWorldEntity(worldEntity);

            // get the world generator config from the world entity
            // replace the world generator values from the components in the world entity
            WorldConfigurator worldConfigurator = worldGenerator.getConfigurator();
            Map<String, Component> params = worldConfigurator.getProperties();
            for (Map.Entry<String, Component> entry : params.entrySet()) {
                Class<? extends Component> clazz = entry.getValue().getClass();
                Component comp = worldEntity.getComponent(clazz);
                if (comp != null) {
                    worldConfigurator.setProperty(entry.getKey(), comp);
                }
            }
        } else {
            EntityRef worldEntity;
            entityManager.createWorldPools(gameManifest);
            worldEntity = entityManager.create();
            worldEntity.addComponent(new WorldComponent());
            NetworkComponent networkComponent = new NetworkComponent();
            networkComponent.replicateMode = NetworkComponent.ReplicateMode.ALWAYS;
            worldEntity.addComponent(networkComponent);
            chunkProvider.setWorldEntity(worldEntity);

            // transfer all world generation parameters from Config to WorldEntity
            SimpleUri generatorUri = worldGenerator.getUri();

            // get the map of properties from the world generator.
            // Replace its values with values from the config set by the UI.
            // Also set all the components to the world entity.
            WorldConfigurator worldConfigurator = worldGenerator.getConfigurator();
            Map<String, Component> params = worldConfigurator.getProperties();
            for (Map.Entry<String, Component> entry : params.entrySet()) {
                Class<? extends Component> clazz = entry.getValue().getClass();
                Component comp = config.getModuleConfig(generatorUri, entry.getKey(), clazz);
                if (comp != null) {
                    worldEntity.addComponent(comp);
                    worldConfigurator.setProperty(entry.getKey(), comp);
                } else {
                    worldEntity.addComponent(entry.getValue());
                }
            }
        }

        return true;
    }
}
