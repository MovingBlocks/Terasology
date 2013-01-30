/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.portals;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.world.block.BlockComponent;

/**
 * System that handles interactions with portals
 * TODO: Add a game hook for world creation that at the end adds a portal by the player start location (only runs once per world)
 *
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
@RegisterSystem
public class PortalSystem implements ComponentSystem {
    protected EntityManager entityManager;

    private static final Logger logger = LoggerFactory.getLogger(SpawnerSystem.class);

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        // register a hook for after the world has been created to check for and place an initial portal
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {PortalComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        logger.info("Activating PortalSystem!");
        // Not sure if this will be needed, but handy to remember how it is done
        PortalComponent portal = entity.getComponent(PortalComponent.class);
        BlockComponent block = entity.getComponent(BlockComponent.class);

        // Activating a portal simply toggles spawning - which is as simple as attaching or detaching that component
        if (entity.hasComponent(SpawnerComponent.class)) {
            logger.info("Found a portal with a spawner, so removing it");
            entity.removeComponent(SpawnerComponent.class);
        } else {
            logger.info("Found a portal withOUT a spawner, so adding one");
            SpawnerComponent spawner = new SpawnerComponent();
            entity.addComponent(spawner);
            entity.saveComponent(spawner);
        }
    }
}


