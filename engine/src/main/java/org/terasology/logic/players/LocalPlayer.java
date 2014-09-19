/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Direction;
import org.terasology.math.TeraMath;
import org.terasology.network.ClientComponent;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class LocalPlayer {

    private EntityRef clientEntity = EntityRef.NULL;

    public LocalPlayer() {
    }

    // TODO: As per Immortius answer in Pull Request #1088,
    // TODO: there appears to be situations in which LocalPlayer is instantiated
    // TODO: but the client entity is -not- set, i.e. in the headless server.
    // TODO: However, it's unclear why the headless server needs a LocalPlayer,
    // TODO: instance. If that can be avoided the code in the following method
    // TODO: might be more rightfully placed in the LocalPlayer constructor.
    public void setClientEntity(EntityRef entity) {
        this.clientEntity = entity;
        ClientComponent clientComp = entity.getComponent(ClientComponent.class);
        if (clientComp != null) {
            clientComp.local = true;
            entity.saveComponent(clientComp);
        }
    }

    public EntityRef getClientEntity() {
        return clientEntity;
    }

    public EntityRef getCharacterEntity() {
        ClientComponent client = clientEntity.getComponent(ClientComponent.class);
        if (client != null) {
            return client.character;
        }
        return EntityRef.NULL;
    }

    public boolean isValid() {
        EntityRef characterEntity = getCharacterEntity();
        return characterEntity.exists() && characterEntity.hasComponent(LocationComponent.class) && characterEntity.hasComponent(CharacterComponent.class)
               && characterEntity.hasComponent(CharacterMovementComponent.class);
    }

    public Vector3f getPosition() {
        return getPosition(new Vector3f());
    }

    public Vector3f getPosition(Vector3f out) {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location == null) {
            return out;
        }
        return location.getWorldPosition(out);
    }

    public Quat4f getRotation() {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location == null) {
            return new Quat4f(0, 0, 0, 1);
        }
        return location.getWorldRotation();
    }

    public Quat4f getViewRotation() {
        CharacterComponent character = getCharacterEntity().getComponent(CharacterComponent.class);
        if (character == null) {
            return new Quat4f(0, 0, 0, 1);
        }
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, TeraMath.DEG_TO_RAD * character.yaw, TeraMath.DEG_TO_RAD * character.pitch, 0);
        return rot;
    }

    public Vector3f getViewDirection() {
        CharacterComponent character = getCharacterEntity().getComponent(CharacterComponent.class);
        if (character == null) {
            return Direction.FORWARD.getVector3f();
        }
        Quat4f rot = new Quat4f();
        QuaternionUtil.setEuler(rot, TeraMath.DEG_TO_RAD * character.yaw, TeraMath.DEG_TO_RAD * character.pitch, 0);
        // TODO: Put a generator for direction vectors in a util class somewhere
        // And just put quaternion -> vector somewhere too
        Vector3f dir = Direction.FORWARD.getVector3f();
        return QuaternionUtil.quatRotate(rot, dir, dir);
    }

    public Vector3f getVelocity() {
        CharacterMovementComponent movement = getCharacterEntity().getComponent(CharacterMovementComponent.class);
        if (movement != null) {
            return new Vector3f(movement.getVelocity());
        }
        return new Vector3f();
    }


    public String toString() {
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f)",
                getPosition().x, getPosition().y, getPosition().z, getViewDirection().x, getViewDirection().y, getViewDirection().z);
    }


}
