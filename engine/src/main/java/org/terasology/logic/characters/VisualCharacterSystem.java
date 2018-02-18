/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.assets.management.AssetManager;
import org.terasology.engine.modes.loadProcesses.AwaitedLocalCharacterSpawnEvent;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.CreateVisualCharacterEvent;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.network.ColorComponent;
import org.terasology.registry.In;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.rendering.nui.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    private Map<EntityRef, EntityRef> characterToVisualMap = new HashMap<>();

    @ReceiveEvent(components = VisualCharacterComponent.class)
    public void onActivatedVisualCharacter(OnActivatedComponent event, EntityRef entity) {
        if (!awaitedLocalCharacterSpawn) {
            /*
             * Before character has spawned localPlayer is not properly initialized
             * and can not be used to test if character is visible.
             */
            return;
        }
        createVisualCharacterIfNotOwnCharacter(entity);
    }


    @ReceiveEvent(components = {VisualCharacterComponent.class})
    public void onBeforeDeactivatedVisualCharacter(BeforeDeactivateComponent event, EntityRef entity) {
        EntityRef visualCharacter = characterToVisualMap.remove(entity);
        if (visualCharacter != null) {
            visualCharacter.destroy();
        }
    }

    void createVisualCharacterIfNotOwnCharacter(EntityRef characterEntity) {
        boolean isCharacterOfLocalPlayer = characterEntity.getOwner().equals(localPlayer.getClientEntity());
        if (isCharacterOfLocalPlayer) {
            return;
        }
        CreateVisualCharacterEvent event = new CreateVisualCharacterEvent(entityManager.newBuilder());
        characterEntity.send(event);
        EntityBuilder entityBuilder = event.getVisualCharacterBuilder();
        entityBuilder.setPersistent(false);
        entityBuilder.setOwner(characterEntity);
        entityBuilder.addOrSaveComponent(new LocationComponent());
        EntityRef visualCharacterEntity = entityBuilder.build();


        Location.attachChild(characterEntity, visualCharacterEntity, new Vector3f(), new Quat4f(0, 0, 0, 1));
        characterToVisualMap.put(characterEntity, visualCharacterEntity);
    }

    /**
     * Handles the local character spawn  event by doing the work that had to be delayed till then:
     * The CreateVisualCharacterEvent events that could not be sent previously will be sent.
     * (They could not be sent earlier as we need to know if the cahracter belongs to the local player or not
     * which can't be determined if the created/loaded character has not been linked to the player yet)
     */
    @ReceiveEvent
    public void onAwaitedLocalCharacterSpawnEvent(AwaitedLocalCharacterSpawnEvent event, EntityRef entity) {
        awaitedLocalCharacterSpawn = true;
        for (EntityRef character: entityManager.getEntitiesWith(VisualCharacterComponent.class)) {
            createVisualCharacterIfNotOwnCharacter(character);
        }
    }


    @ReceiveEvent(priority = EventPriority.PRIORITY_TRIVIAL)
    public void onCreateDefaultVisualCharacter(CreateVisualCharacterEvent event, EntityRef characterEntity) {
        Prefab prefab = assetManager.getAsset("engine:defaultVisualCharacter", Prefab.class).get();
        EntityBuilder entityBuilder = event.getVisualCharacterBuilder();
        entityBuilder.addPrefab(prefab);
        SkeletalMeshComponent skeletalMeshComponent = entityBuilder.getComponent(SkeletalMeshComponent.class);
        if (skeletalMeshComponent != null) {
            skeletalMeshComponent.color = colorOfOwningPlayer(characterEntity).orElse(Color.WHITE);
            entityBuilder.saveComponent(skeletalMeshComponent);
        }


        event.consume();
    }

    private Optional<Color> colorOfOwningPlayer(EntityRef entityRef) {
        EntityRef owner = entityRef.getOwner();
        if (owner == null) {
            owner = entityRef;
        }
        ClientComponent clientComp = owner.getComponent(ClientComponent.class);
        if (clientComp != null) {
            ColorComponent colorComp = clientComp.clientInfo.getComponent(ColorComponent.class);
            if (colorComp != null) {
                return Optional.of(colorComp.color);
            }
        }
        return Optional.empty();
    }
}
