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
package org.terasology.logic.players;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.components.InventoryComponent;
import org.terasology.components.LightComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.network.ClientComponent;
import org.terasology.math.TeraMath;
import org.terasology.logic.characters.CharacterMovementComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayer {

    private EntityRef clientEntity = EntityRef.NULL;

    public LocalPlayer() {
    }

    public void setClientEntity(EntityRef entity) {
        this.clientEntity = entity;
        ClientComponent clientComp = entity.getComponent(ClientComponent.class);
        if (clientComp != null) {
            clientComp.local = true;
            entity.saveComponent(clientComp);
        }
    }

    public boolean isValid() {
        return getCharacterEntity().exists() && getCharacterEntity().hasComponent(LocationComponent.class) && getCharacterEntity().hasComponent(LocalPlayerComponent.class) && getCharacterEntity().hasComponent(CharacterComponent.class);
    }

    public Vector3f getPosition() {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location == null) {
            return new Vector3f();
        }
        return location.getWorldPosition();
    }

    public Quat4f getRotation() {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location == null) {
            return new Quat4f(0, 0, 0, 1);
        }
        return location.getWorldRotation();
    }

    public boolean isCarryingTorch() {

        InventoryComponent inventory = getCharacterEntity().getComponent(InventoryComponent.class);
        LocalPlayerComponent localPlayer = getCharacterEntity().getComponent(LocalPlayerComponent.class);
        if (inventory == null || localPlayer == null)
            return false;

        return inventory.itemSlots.get(localPlayer.selectedTool).hasComponent(LightComponent.class);
    }

    public EntityRef getCharacterEntity() {
        ClientComponent client = clientEntity.getComponent(ClientComponent.class);
        if (client != null) {
            return client.character;
        }
        return EntityRef.NULL;
    }

    public Vector3f getVelocity() {
        CharacterMovementComponent movement = getCharacterEntity().getComponent(CharacterMovementComponent.class);
        if (movement != null) {
            return new Vector3f(movement.getVelocity());
        }
        return new Vector3f();
    }

    public Vector3f getViewDirection() {
        LocalPlayerComponent localPlayer = getCharacterEntity().getComponent(LocalPlayerComponent.class);
        if (localPlayer == null) {
            return new Vector3f(0, 0, -1);
        }
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, TeraMath.DEG_TO_RAD * localPlayer.viewYaw, TeraMath.DEG_TO_RAD * localPlayer.viewPitch, 0);
        /* This code does not work, as it grabs the rotation of the mouse instead
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, localPlayer.viewYaw, localPlayer.viewPitch, 0);
        */
        // TODO: Put a generator for direction vectors in a util class somewhere
        // And just put quaternion -> vector somewhere too
        Vector3f dir = new Vector3f(0, 0, 1);
        return QuaternionUtil.quatRotate(rot, dir, dir);
    }

    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f)", getPosition().x, getPosition().y, getPosition().z, getViewDirection().x, getViewDirection().y, getViewDirection().z);
    }

    public EntityRef getClientEntity() {
        return clientEntity;
    }
}
