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
package org.terasology.logic.characters;

import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.OnScaleEvent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;

/**
 * Gaze describes where the character is looking.
 *
 * This direction is accessible to all clients and could be hooked up to part of the rendered character.
 * Also, this can be used to allow the server to correctly perform actions based on where the character is looking.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class GazeAuthoritySystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(GazeAuthoritySystem.class);
    @In
    EntityManager entityManager;
    @In
    private Config config;

    @ReceiveEvent
    public void ensureGazeContainerEntitiesCreated(OnActivatedComponent event, EntityRef entityRef, GazeMountPointComponent gazeMountPointComponent,
            LocationComponent locationComponent) {
        if (!gazeMountPointComponent.gazeEntity.exists()) {
            gazeMountPointComponent.gazeEntity = createGazeEntity();
            entityRef.saveComponent(gazeMountPointComponent);
        }
        gazeMountPointComponent.translate.y = config.getPlayer().getEyeHeight();
        Location.attachChild(entityRef, gazeMountPointComponent.gazeEntity, gazeMountPointComponent.translate, new Quaternionf());
    }

    private EntityRef createGazeEntity() {
        EntityBuilder gazeContainerBuilder = entityManager.newBuilder("engine:gaze");
        EntityRef gazeEntity = gazeContainerBuilder.build();
        return gazeEntity;
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void onScaleCharacter(OnScaleEvent event, EntityRef entity, GazeMountPointComponent gazeMountPoint) {
        // adjust character eye level
        // set eye level based on "average" body decomposition for human-like figures into 7.5 "heads".
        //TODO: this glitches for some values (look through ceiling)
        gazeMountPoint.translate.y = (event.getNewValue() / 7.5f) * 7f - event.getNewValue() * 0.5f;

        Location.removeChild(entity, gazeMountPoint.gazeEntity);
        Location.attachChild(entity, gazeMountPoint.gazeEntity, gazeMountPoint.translate, new Quaternionf());
        entity.saveComponent(gazeMountPoint);
    }

    /**
     * Returns the gaze entity if it exists, otherwise the character entity will be returned.
     *
     * @param character
     * @return
     */
    public static EntityRef getGazeEntityForCharacter(EntityRef character) {
        GazeMountPointComponent gazeMountPointComponent = character.getComponent(GazeMountPointComponent.class);
        if (gazeMountPointComponent != null && gazeMountPointComponent.gazeEntity.exists()) {
            return gazeMountPointComponent.gazeEntity;
        }
        return character;
    }
}
