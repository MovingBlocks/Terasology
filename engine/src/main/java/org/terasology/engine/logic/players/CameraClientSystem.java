// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityBuilder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.GazeAuthoritySystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.location.Location;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.engine.logic.players.event.ResetCameraEvent;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

/**
 * This is a system that creates and maintains a client side entity for the camera.
 * <p>
 * Entity Creation Flow:
 * - ClientComponent for the local player added or changed
 * - If Camera does not exist, create "engine:camera" entity
 * - Save the ClientComponent with the new camera attached
 * <p>
 * Reset Camera Event Flow:
 * - ResetCameraEvent
 * - Camera entity on the ClientComponent is destroyed and set to EntityRef.NULL
 * - ClientComponent is saved
 * <p>
 * Auto Mount Camera Flow:
 * - ClientComponent or AutoMountCameraComponent for the local player added or changed
 * - The local player's camera entity (created from above steps) is location linked to the Gaze Entity
 */
@RegisterSystem(RegisterMode.CLIENT)
public class CameraClientSystem extends BaseComponentSystem {
    @In
    LocalPlayer localPlayer;
    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void ensureCameraEntityCreatedOnChangedClientComponent(OnChangedComponent event, EntityRef client, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(client)) {
            ensureCameraEntityCreated();
        }
    }

    @ReceiveEvent
    public void ensureCameraEntityCreatedOnActivateClientComponent(OnActivatedComponent event, EntityRef client, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(client)) {
            ensureCameraEntityCreated();
        }
    }

    private void ensureCameraEntityCreated() {
        if (!localPlayer.getCameraEntity().exists()) {
            ClientComponent clientComponent = localPlayer.getClientEntity().getComponent(ClientComponent.class);
            // create the camera from the prefab
            EntityBuilder builder = entityManager.newBuilder("engine:camera");
            builder.setPersistent(false);
            clientComponent.camera = builder.build();
            clientComponent.camera.setScope(EntityScope.GLOBAL); // Ensure that the camera isn't destroyed by the purgeWorld command.
            localPlayer.getClientEntity().saveComponent(clientComponent);
        }
    }

    @ReceiveEvent
    public void resetCameraOnCharacterSpawn(OnPlayerSpawnedEvent event, EntityRef character) {
        if (localPlayer.getCharacterEntity().equals(character)) {
            resetCamera();
        }
    }

    @Command(shortDescription = "Reset the camera position", requiredPermission = PermissionManager.NO_PERMISSION)
    public void resetCamera() {
        localPlayer.getClientEntity().send(new ResetCameraEvent());
    }

    @ReceiveEvent
    public void resetCameraForClient(ResetCameraEvent resetCameraEvent, EntityRef entityRef, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(entityRef)) {
            clientComponent.camera.destroy();
            clientComponent.camera = EntityRef.NULL;
            // this will trigger a ClientComponent change which will in turn trigger recreation of the camera entity
            localPlayer.getClientEntity().saveComponent(clientComponent);
        }
    }

    @ReceiveEvent
    public void mountCameraOnActivate(OnActivatedComponent event, EntityRef entityRef,
                                      AutoMountCameraComponent autoMountCameraComponent, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(entityRef) && clientComponent.camera.exists()) {
            mountCamera();
        }
    }

    @ReceiveEvent
    public void mountCameraOnChange(OnChangedComponent event, EntityRef entityRef,
                                    AutoMountCameraComponent autoMountCameraComponent, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(entityRef) && clientComponent.camera.exists()) {
            mountCamera();
        }
    }

    private void mountCamera() {
        ClientComponent clientComponent = localPlayer.getClientEntity().getComponent(ClientComponent.class);
        EntityRef targetEntityForCamera = GazeAuthoritySystem.getGazeEntityForCharacter(clientComponent.character);

        //TODO: why is the camera setup differently
        LocationComponent cameraLocation = clientComponent.camera.getComponent(LocationComponent.class);
        //if the camera already has a location,  use that as the relative position of the camera
        if (cameraLocation != null) {
            Location.attachChild(targetEntityForCamera, clientComponent.camera, cameraLocation.getLocalPosition(), new Quaternionf());
        } else {
            Location.attachChild(targetEntityForCamera, clientComponent.camera, new Vector3f(0, 0, 0), new Quaternionf());
        }
    }
}
