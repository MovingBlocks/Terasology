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

package org.terasology.engine.core.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldGenerator;

import java.util.Iterator;
import java.util.Map;

/**
 */
public class CreateWorldEntity extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(CreateWorldEntity.class);

    private final Context context;
    private final GameManifest gameManifest;

    public CreateWorldEntity(Context context, GameManifest gameManifest) {
        this.context = context;
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Creating World Entity...";
    }

    @Override
    public boolean step() {
        if (worldEntityExists()) {
            useConfigurationOfCurrentWorld();
        } else {
            createWorldFromConfig();
        }

        return true;
    }

    private boolean worldEntityExists() {
        EntityManager entityManager = context.get(EntityManager.class);
        Iterable<EntityRef> worldEntityIterator = entityManager.getEntitiesWith(WorldComponent.class);
        return worldEntityIterator.iterator().hasNext();
    }

    private void useConfigurationOfCurrentWorld() {
        EntityManager entityManager = context.get(EntityManager.class);
        ChunkProvider chunkProvider = context.get(ChunkProvider.class);

        Iterator<EntityRef> worldEntityIterator = entityManager.getEntitiesWith(WorldComponent.class).iterator();
        EntityRef worldEntity = worldEntityIterator.next();
        chunkProvider.setWorldEntity(worldEntity);

        // get the world generator config from the world entity
        // replace the world generator values from the components in the world entity
        WorldGenerator worldGenerator = context.get(WorldGenerator.class);
        WorldConfigurator worldConfigurator = worldGenerator.getConfigurator();
        Map<String, Component> params = worldConfigurator.getProperties();
        for (Map.Entry<String, Component> entry : params.entrySet()) {
            Class<? extends Component> clazz = entry.getValue().getClass();
            Component comp = worldEntity.getComponent(clazz);
            if (comp != null) {
                worldConfigurator.setProperty(entry.getKey(), comp);
            }
        }

        worldEntityIterator.forEachRemaining(w -> logger.warn("Ignored extra world {}", w));
    }

    private void createWorldFromConfig() {
        EntityRef worldEntity = createWorldPoolsAndEntityAndGiveItToTheChunkProvider();
        configureWorldEntityFromConfig(worldEntity);
    }

    /** transfer all world generation parameters from Config to WorldEntity */
    private void configureWorldEntityFromConfig(EntityRef worldEntity) {
        // get the map of properties from the world generator.
        WorldConfigurator worldConfigurator = context.get(WorldGenerator.class).getConfigurator();
        Map<String, Component> configuratorProperties = worldConfigurator.getProperties();

        // Replace its values with values from the config set by the UI.
        // Also set all the components to the world entity.
        configuratorProperties.forEach((key, currentComponent) -> {
            Component configuredComponent = getComponentOfWorldEntityFromConfig(key, currentComponent.getClass());
            if (configuredComponent != null) {
                worldEntity.addComponent(configuredComponent);
                worldConfigurator.setProperty(key, configuredComponent);
            } else {
                worldEntity.addComponent(currentComponent);
            }
        });
    }

    private EntityRef createWorldPoolsAndEntityAndGiveItToTheChunkProvider() {
        EntityManager entityManager = context.get(EntityManager.class);

        entityManager.createWorldPools(gameManifest);

        EntityRef worldEntity = entityManager.create();
        worldEntity.addComponent(new WorldComponent());
        NetworkComponent networkComponent = new NetworkComponent();
        networkComponent.replicateMode = NetworkComponent.ReplicateMode.ALWAYS;
        worldEntity.addComponent(networkComponent);

        ChunkProvider chunkProvider = context.get(ChunkProvider.class);
        chunkProvider.setWorldEntity(worldEntity);
        return worldEntity;
    }

    private <T extends Component> T getComponentOfWorldEntityFromConfig(String key, Class<T> clazz) {
        SimpleUri generatorUri = context.get(WorldGenerator.class).getUri();
        return context.get(Config.class).getModuleConfig(generatorUri, key, clazz);
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
