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

import com.google.common.base.Optional;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldComponent;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Immortius
 */
public class CreateWorldEntity extends SingleStepLoadProcess {
    @Override
    public String getMessage() {
        return "Creating World Entity...";
    }

    @Override
    public boolean step() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);

        Iterator<EntityRef> worldEntityIterator = entityManager.getEntitiesWith(WorldComponent.class).iterator();
        // TODO: Move the world renderer bits elsewhere
        if (worldEntityIterator.hasNext()) {
            worldRenderer.getChunkProvider().setWorldEntity(worldEntityIterator.next());
        } else {
            EntityRef worldEntity = entityManager.create();
            worldEntity.addComponent(new WorldComponent());
            worldRenderer.getChunkProvider().setWorldEntity(worldEntity);

            // transfer all world generation parameters from Config to WorldEntity
            WorldGenerator worldGenerator = CoreRegistry.get(WorldGenerator.class);
            Optional<WorldConfigurator> ocf = worldGenerator.getConfigurator();

            if (ocf.isPresent()) {
                SimpleUri generatorUri = worldGenerator.getUri();
                Config config = CoreRegistry.get(Config.class);
                Map<String, Component> params = ocf.get().getProperties();

                for (Map.Entry<String, Component> entry : params.entrySet()) {
                    Class<? extends Component> clazz = entry.getValue().getClass();
                    Component comp = config.getModuleConfig(generatorUri, entry.getKey(), clazz);
                    if (comp != null) {
                        worldEntity.addComponent(comp);
                    } else {
                        worldEntity.addComponent(entry.getValue());
                    }
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
