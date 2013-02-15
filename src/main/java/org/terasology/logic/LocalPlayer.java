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
package org.terasology.logic;

import com.bulletphysics.linearmath.QuaternionUtil;
import org.terasology.components.InventoryComponent;
import org.terasology.components.LightComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.physics.character.CharacterMovementComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayer {

    private EntityRef entity = EntityRef.NULL;

    public LocalPlayer(EntityRef playerEntity) {
        this.entity = playerEntity;
    }

    public void setEntity(EntityRef newEntity) {
        this.entity = (newEntity == null) ? EntityRef.NULL : newEntity;
    }

    public boolean isValid() {
        return entity.exists() && entity.hasComponent(LocationComponent.class) && entity.hasComponent(LocalPlayerComponent.class) && entity.hasComponent(PlayerComponent.class);
    }

    public Vector3f getPosition() {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location == null) {
            return new Vector3f();
        }
        return location.getWorldPosition();
    }

    public Quat4f getViewRotation() {
        LocalPlayerComponent character = getEntity().getComponent(LocalPlayerComponent.class);
        if (character == null) {
            return new Quat4f(0, 0, 0, 1);
        }
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, TeraMath.DEG_TO_RAD * character.viewYaw, TeraMath.DEG_TO_RAD * character.viewPitch, 0);
        return rot;
    }

    public Quat4f getRotation() {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location == null) {
            return new Quat4f(0, 0, 0, 1);
        }
        return location.getWorldRotation();
    }

    public boolean isCarryingTorch() {

        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);
        if (inventory == null || localPlayer == null)
            return false;

        return inventory.itemSlots.get(localPlayer.selectedTool).hasComponent(LightComponent.class);
    }

    public EntityRef getEntity() {
        return entity;
    }

    public Vector3f getVelocity() {
        CharacterMovementComponent movement = entity.getComponent(CharacterMovementComponent.class);
        if (movement != null) {
            return new Vector3f(movement.getVelocity());
        }
        return new Vector3f();
    }

    public Vector3f getViewDirection() {
        LocalPlayerComponent localPlayer = entity.getComponent(LocalPlayerComponent.class);
        if (localPlayer == null) {
            return Direction.FORWARD.getVector3f();
        }
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, TeraMath.DEG_TO_RAD * localPlayer.viewYaw, TeraMath.DEG_TO_RAD * localPlayer.viewPitch, 0);
        // TODO: Put a generator for direction vectors in a util class somewhere
        // And just put quaternion -> vector somewhere too
        Vector3f dir = Direction.FORWARD.getVector3f();
        return QuaternionUtil.quatRotate(rot, dir, dir);
    }

    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f)", getPosition().x, getPosition().y, getPosition().z, getViewDirection().x, getViewDirection().y, getViewDirection().z);
    }
}
