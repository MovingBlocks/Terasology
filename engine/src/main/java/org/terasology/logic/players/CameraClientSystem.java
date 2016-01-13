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
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.event.ResetCameraEvent;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;

@RegisterSystem(RegisterMode.CLIENT)
public class CameraClientSystem extends BaseComponentSystem {
    @In
    LocalPlayer localPlayer;
    @In
    EntityManager entityManager;

    @ReceiveEvent
    public void onAutoMountCamera(OnChangedComponent event, EntityRef client, AutoMountCameraComponent autoMountCameraComponent, ClientComponent clientComponent) {
        client.send(new ResetCameraEvent());
    }

    @ReceiveEvent
    public void ensureCameraEntityCreated(OnActivatedComponent event, EntityRef entityRef, ClientComponent clientComponent) {
        if (!clientComponent.camera.exists()) {
            clientComponent.camera = entityManager.create("engine:camera");
            entityRef.saveComponent(clientComponent);
        }
    }

    @Command(shortDescription = "Reset the camera position", requiredPermission = PermissionManager.NO_PERMISSION)
    public void resetCamera() {
        localPlayer.getClientEntity().send(new ResetCameraEvent());
    }

    @ReceiveEvent
    public void resetCamera(ResetCameraEvent resetCameraEvent, EntityRef client, AutoMountCameraComponent autoMountCameraComponent, ClientComponent clientComponent) {
        EntityRef targetEntityForCamera = GazeAuthoritySystem.getGazeEntityForCharacter(clientComponent.character);
        Location.attachChild(targetEntityForCamera, clientComponent.camera, Vector3f.zero(), new Quat4f(Quat4f.IDENTITY));
    }
}
