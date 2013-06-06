/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.game.modes.loadProcesses;

import java.util.Iterator;

import org.terasology.components.world.WorldComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Immortius
 */
public class CreateWorldEntity implements LoadProcess {
    @Override
    public String getMessage() {
        return "Creating World Entity...";
    }

    @Override
    public boolean step() {
        EntityManager entityManager = CoreRegistry.get(EntityManager.class);
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);

        Iterator<EntityRef> worldEntityIterator = entityManager.iteratorEntities(WorldComponent.class).iterator();
        // TODO: Move the world renderer bits elsewhere
        if (worldEntityIterator.hasNext()) {
            worldRenderer.getChunkProvider().setWorldEntity(worldEntityIterator.next());
        } else {
            EntityRef worldEntity = entityManager.create();
            worldEntity.addComponent(new WorldComponent());
            worldRenderer.getChunkProvider().setWorldEntity(worldEntity);
        }
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}
