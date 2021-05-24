// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.core.modes.loadProcesses.AwaitedLocalCharacterSpawnEvent;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.CreateVisualCharacterEvent;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.assets.management.AssetManager;

/**
 * This system is responsible for sending a {@link CreateVisualCharacterEvent} according to how it is specified in
 * {@link VisualCharacterComponent}. It also provides a default handling for {@link CreateVisualCharacterEvent} that
 * creates a floating cube.
 */
@RegisterSystem(RegisterMode.CLIENT)
public class VisualCharacterSystem extends BaseComponentSystem {
    @In
    LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    private boolean awaitedLocalCharacterSpawn = false;

    private VisualEntityBuildAndAttachStrategy createAndAttachVisualEntityStrategy = this::createAndAttachVisualEntity;

    @ReceiveEvent
    public void onActivatedVisualCharacter(OnActivatedComponent event, EntityRef entity,
                                           VisualCharacterComponent visualCharacterComponent) {
        if (!awaitedLocalCharacterSpawn) {
            /*
             * Before character has spawned localPlayer is not properly initialized
             * and can not be used to test if character is visible.
             */
            return;
        }
        createVisualCharacterIfNotOwnCharacter(entity, visualCharacterComponent);
    }


    @ReceiveEvent(components = VisualCharacterComponent.class)
    public void onBeforeDeactivatedVisualCharacter(BeforeDeactivateComponent event, EntityRef entity,
                                                   VisualCharacterComponent visualCharacterComponent) {
        visualCharacterComponent.visualCharacter.destroy();
    }

    void createVisualCharacterIfNotOwnCharacter(EntityRef characterEntity,
                                                VisualCharacterComponent visualCharacterComponent) {
        boolean isCharacterOfLocalPlayer = characterEntity.getOwner().equals(localPlayer.getClientEntity());
        if (isCharacterOfLocalPlayer) {
            return;
        }
        CreateVisualCharacterEvent event = new CreateVisualCharacterEvent(entityManager.newBuilder());
        characterEntity.send(event);
        EntityBuilder entityBuilder = event.getVisualCharacterBuilder();
        EntityRef visualCharacterEntity = createAndAttachVisualEntityStrategy.createAndAttachVisualEntity(entityBuilder,
                characterEntity);

        visualCharacterComponent.visualCharacter = visualCharacterEntity;
        characterEntity.saveComponent(visualCharacterComponent);
    }

    private EntityRef createAndAttachVisualEntity(EntityBuilder entityBuilder, EntityRef characterEntity) {
        entityBuilder.setPersistent(false);
        entityBuilder.setOwner(characterEntity);
        entityBuilder.addOrSaveComponent(new LocationComponent());
        EntityRef visualCharacterEntity = entityBuilder.build();

        Location.attachChild(characterEntity, visualCharacterEntity, new Vector3f(), new Quaternionf());
        return visualCharacterEntity;
    }

    interface VisualEntityBuildAndAttachStrategy {
        EntityRef createAndAttachVisualEntity(EntityBuilder entityBuilder, EntityRef characterEntity);
    }

    /**
     * Handles the local character spawn  event by doing the work that had to be delayed till then: The
     * CreateVisualCharacterEvent events that could not be sent previously will be sent. (They could not be sent earlier
     * as we need to know if the character belongs to the local player or not which can't be determined if the
     * created/loaded character has not been linked to the player yet)
     */
    @ReceiveEvent
    public void onAwaitedLocalCharacterSpawnEvent(AwaitedLocalCharacterSpawnEvent event, EntityRef entity) {
        awaitedLocalCharacterSpawn = true;
        for (EntityRef character : entityManager.getEntitiesWith(VisualCharacterComponent.class)) {
            createVisualCharacterIfNotOwnCharacter(character, character.getComponent(VisualCharacterComponent.class));
        }
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void onCreateDefaultVisualCharacter(CreateVisualCharacterEvent event, EntityRef characterEntity) {
        Prefab prefab = assetManager.getAsset("engine:defaultVisualCharacter", Prefab.class).get();
        EntityBuilder entityBuilder = event.getVisualCharacterBuilder();
        entityBuilder.addPrefab(prefab);
        event.consume();
    }

    /**
     * For tests only
     */
    void setCreateAndAttachVisualEntityStrategy(VisualEntityBuildAndAttachStrategy createAndAttachVisualEntityStrategy) {
        this.createAndAttachVisualEntityStrategy = createAndAttachVisualEntityStrategy;
    }
}
