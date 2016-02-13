/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.players;

import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.GazeAuthoritySystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.location.Location;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.logic.players.event.ResetCameraEvent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

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
    public void mountCameraOnActivate(OnActivatedComponent event, EntityRef entityRef, AutoMountCameraComponent autoMountCameraComponent, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(entityRef) && clientComponent.camera.exists()) {
            mountCamera();
        }
    }

    @ReceiveEvent
    public void mountCameraOnChange(OnChangedComponent event, EntityRef entityRef, AutoMountCameraComponent autoMountCameraComponent, ClientComponent clientComponent) {
        if (localPlayer.getClientEntity().equals(entityRef) && clientComponent.camera.exists()) {
            mountCamera();
        }
    }

    private void mountCamera() {
        ClientComponent clientComponent = localPlayer.getClientEntity().getComponent(ClientComponent.class);
        EntityRef targetEntityForCamera = GazeAuthoritySystem.getGazeEntityForCharacter(clientComponent.character);
        LocationComponent cameraLocation = clientComponent.camera.getComponent(LocationComponent.class);
        // if the camera already has a location,  use that as the relative position of the camera
        if (cameraLocation != null) {
            Location.attachChild(targetEntityForCamera, clientComponent.camera, cameraLocation.getLocalPosition(), new Quat4f(Quat4f.IDENTITY));
        } else {
            Location.attachChild(targetEntityForCamera, clientComponent.camera, Vector3f.zero(), new Quat4f(Quat4f.IDENTITY));
        }
    }
}
