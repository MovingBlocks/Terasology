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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.game.GameManifest;
import org.terasology.network.NetworkComponent;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;

import java.util.Iterator;
import java.util.Map;

/**
 */
public class CreateWorldEntity extends SingleStepLoadProcess {

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
        EntityManager entityManager = context.get(EntityManager.class);
        ChunkProvider chunkProvider = context.get(ChunkProvider.class);

        Iterator<EntityRef> worldEntityIterator = entityManager.getEntitiesWith(WorldComponent.class).iterator();
        if (worldEntityIterator.hasNext()) {
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
            WorldGenerator worldGenerator = context.get(WorldGenerator.class);
            SimpleUri generatorUri = worldGenerator.getUri();
            Config config = context.get(Config.class);

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

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
