// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

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

public class CreateWorldEntity extends SingleStepLoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(CreateWorldEntity.class);

    //TODO: figure out dependencies at some point ....
    protected EntityManager entityManager;
    protected WorldGenerator worldGenerator;
    protected WorldConfigurator worldConfigurator;
    protected Config config;
    protected ChunkProvider chunkProvider;

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
        this.entityManager = context.get(EntityManager.class);
        this.worldGenerator = context.get(WorldGenerator.class);
        this.config = context.get(Config.class);
        this.chunkProvider = context.get(ChunkProvider.class);
        this.worldConfigurator = worldGenerator.getConfigurator();

        Iterator<EntityRef> worldEntityIterator = entityManager.getEntitiesWith(WorldComponent.class).iterator();
        if (worldEntityIterator.hasNext()) {

            EntityRef worldEntity = worldEntityIterator.next();
            worldEntityIterator.forEachRemaining(w -> logger.warn("Ignored extra world {}", w));
            chunkProvider.setWorldEntity(worldEntity);

            // replace the world generator values from the components in the world entity
            worldConfigurator.getProperties().forEach((key, currentComponent) -> {
                Component component = worldEntity.getComponent(currentComponent.getClass());
                if (component != null) {
                    worldConfigurator.setProperty(key, component);
                }
            });

        } else {
            // create world entity if one does not exist.
            EntityRef worldEntity = createWorldPoolsAndEntity();
            chunkProvider.setWorldEntity(worldEntity);

            // transfer all world generation parameters from Config to WorldEntity
            SimpleUri generatorUri = worldGenerator.getUri();
            worldConfigurator.getProperties().forEach((key, currentComponent) -> {
                Class<? extends Component> clazz = currentComponent.getClass();
                Component moduleComponent = gameManifest.getModuleConfig(generatorUri, key, clazz);
                if (moduleComponent != null) {
                    // configure entity from component
                    worldEntity.addComponent(moduleComponent);
                    worldConfigurator.setProperty(key, moduleComponent);
                } else {
                    worldEntity.addComponent(currentComponent);
                }
            });
        }

        return true;
    }

    private EntityRef createWorldPoolsAndEntity() {
        entityManager.createWorldPools(gameManifest);

        EntityRef worldEntity = entityManager.create();
        worldEntity.addComponent(new WorldComponent());
        NetworkComponent networkComponent = new NetworkComponent();
        networkComponent.replicateMode = NetworkComponent.ReplicateMode.ALWAYS;
        worldEntity.addComponent(networkComponent);
        return worldEntity;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
