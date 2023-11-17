// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.PlayerConfig;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.OnScaleEvent;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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
    private PlayerConfig playerConfig;

    @ReceiveEvent
    public void ensureGazeContainerEntitiesCreated(OnActivatedComponent event, EntityRef entityRef, GazeMountPointComponent gazeMountPointComponent,
            LocationComponent locationComponent) {
        if (!gazeMountPointComponent.gazeEntity.exists()) {
            gazeMountPointComponent.gazeEntity = createGazeEntity();
            entityRef.saveComponent(gazeMountPointComponent);
        }
        gazeMountPointComponent.translate.y = playerConfig.eyeHeight.get();
        Location.attachChild(entityRef, gazeMountPointComponent.gazeEntity, gazeMountPointComponent.translate, new Quaternionf());
    }

    private EntityRef createGazeEntity() {
        EntityBuilder gazeContainerBuilder = entityManager.newBuilder("engine:gaze");
        return gazeContainerBuilder.build();
    }

    @Priority(EventPriority.PRIORITY_LOW)
    @ReceiveEvent
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
