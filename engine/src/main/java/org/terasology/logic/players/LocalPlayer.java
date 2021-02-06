// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.players;

import com.google.common.collect.Sets;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.logic.characters.CharacterSystem;
import org.terasology.logic.characters.events.ActivationPredicted;
import org.terasology.logic.characters.events.ActivationRequest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Direction;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Quat4f;
import org.terasology.network.ClientComponent;
import org.terasology.physics.HitResult;
import org.terasology.physics.Physics;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.registry.CoreRegistry;

public class LocalPlayer {

    private EntityRef clientEntity = EntityRef.NULL;
    private int nextActivationId;

    //Record and Replay classes
    private DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

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

    public void setRecordAndReplayClasses(DirectionAndOriginPosRecorderList list, RecordAndReplayCurrentStatus status) {
        this.directionAndOriginPosRecorderList = list;
        this.recordAndReplayCurrentStatus = status;
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

    public EntityRef getCameraEntity() {
        ClientComponent client = clientEntity.getComponent(ClientComponent.class);
        if (client != null) {
            return client.camera;
        }
        return EntityRef.NULL;
    }

    public EntityRef getClientInfoEntity() {
        ClientComponent client = clientEntity.getComponent(ClientComponent.class);
        if (client != null) {
            return client.clientInfo;
        }
        return EntityRef.NULL;
    }

    public boolean isValid() {
        EntityRef characterEntity = getCharacterEntity();
        return characterEntity.exists() && characterEntity.hasComponent(LocationComponent.class) && characterEntity.hasComponent(CharacterComponent.class)
                && characterEntity.hasComponent(CharacterMovementComponent.class);
    }

    /**
     * the position of the local player
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3f getPosition(Vector3f dest) {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location != null) {
            Vector3f result = location.getWorldPosition(new Vector3f());
            if (result.isFinite()) { //TODO: MP finite check seems to hide a larger underlying problem
                dest.set(result);
            }
        }
        return dest;
    }

    /**
     * the rotation of the local player
     *
     * @param dest will hold the result
     * @return dest
     */
    public Quaternionf getRotation(Quaternionf dest) {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location != null) {
            Quaternionf result = location.getWorldRotation(new Quaternionf());
            if (result.isFinite()) { //TODO: MP finite check seems to hide a larger underlying problem
                dest.set(result);
            }
        }
        return dest;
    }

    /**
     * position of camera if one is present else use {@link #getPosition(Vector3f)}
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3f getViewPosition(Vector3f dest) {
        ClientComponent clientComponent = getClientEntity().getComponent(ClientComponent.class);
        if (clientComponent == null) {
            return dest;
        }
        LocationComponent location = clientComponent.camera.getComponent(LocationComponent.class);
        if (location != null) {
            Vector3f result = location.getWorldPosition(new Vector3f());
            if (result.isFinite()) { //TODO: MP finite check seems to hide a larger underlying problem
                dest.set(result);
                return dest;
            }
        }
        return getPosition(dest);
    }

    /**
     * @return
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *         {@link #getViewRotation(Quaternionf)}.
     */
    @Deprecated
    public Quat4f getViewRotation() {
        return JomlUtil.from(getViewRotation(new Quaternionf()));
    }

    /**
     * orientation of camera if one is present else use {@link #getPosition(Vector3f)}
     *
     * @param dest will hold the result
     * @return dest
     */
    public Quaternionf getViewRotation(Quaternionf dest) {
        ClientComponent clientComponent = getClientEntity().getComponent(ClientComponent.class);
        if (clientComponent == null) {
            return new Quaternionf();
        }
        LocationComponent location = clientComponent.camera.getComponent(LocationComponent.class);
        if (location != null) {
            Quaternionf result = location.getWorldRotation(new Quaternionf());
            if (result.isFinite()) { //TODO: MP finite check seems to hide a larger underlying problem
                dest.set(result);
                return dest;
            }
        }
        return getRotation(dest);
    }

    /**
     * forward view direction of camera view
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3f getViewDirection(Vector3f dest) {
        Quaternionf rot = getViewRotation(new Quaternionf());
        return rot.transform(Direction.FORWARD.asVector3f(), dest);
    }

    public Vector3f getVelocity(Vector3f dest) {
        CharacterMovementComponent movement = getCharacterEntity().getComponent(CharacterMovementComponent.class);
        if (movement != null) {
            return dest.set(movement.getVelocity());
        }
        return dest;
    }


    /**
     * Can be used by modules to trigger the activation of a player owned entity like an item.
     * <p>
     * The method has been made for the usage on the client. It triggers a {@link ActivationPredicted} event on the
     * client and a {@link ActivationRequest} event on the server which will lead to a {@link
     * org.terasology.logic.common.ActivateEvent} on the server.
     *
     * @param usedOwnedEntity an entity owned by the player like an item.
     */
    public void activateOwnedEntityAsClient(EntityRef usedOwnedEntity) {
        if (!usedOwnedEntity.exists()) {
            return;
        }
        activateTargetOrOwnedEntity(usedOwnedEntity);
    }

    /**
     * Tries to activate the target the player is pointing at.
     * <p>
     * This method is indented to be used on the client.
     *
     * @return true if a target got activated.
     */
    boolean activateTargetAsClient() {
        return activateTargetOrOwnedEntity(EntityRef.NULL);
    }

    /**
     * @param usedOwnedEntity if it does not exist it is not an item usage.
     * @return true if an activation request got sent. Returns always true if usedItem exists.
     */
    private boolean activateTargetOrOwnedEntity(EntityRef usedOwnedEntity) {
        EntityRef character = getCharacterEntity();
        CharacterComponent characterComponent = character.getComponent(CharacterComponent.class);
        Vector3f direction = getViewDirection(new Vector3f());
        Vector3f originPos = getViewPosition(new Vector3f());
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.RECORDING) {
            this.directionAndOriginPosRecorderList.getTargetOrOwnedEntityDirectionAndOriginPosRecorder().add(direction, originPos);
        } else if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.REPLAYING) {
            Vector3f[] data =
                    this.directionAndOriginPosRecorderList.getTargetOrOwnedEntityDirectionAndOriginPosRecorder().poll();
            direction = data[0];
            originPos = data[1];
        }
        boolean ownedEntityUsage = usedOwnedEntity.exists();
        int activationId = nextActivationId++;
        Physics physics = CoreRegistry.get(Physics.class);
        HitResult result = physics.rayTrace(originPos, direction, characterComponent.interactionRange,
                Sets.newHashSet(character), CharacterSystem.DEFAULTPHYSICSFILTER);
        boolean eventWithTarget = result.isHit();
        if (eventWithTarget) {
            EntityRef activatedObject = usedOwnedEntity.exists() ? usedOwnedEntity : result.getEntity();
            activatedObject.send(new ActivationPredicted(character, result.getEntity(), originPos, direction,
                    result.getHitPoint(), result.getHitNormal(), activationId));
            character.send(new ActivationRequest(character, ownedEntityUsage, usedOwnedEntity, eventWithTarget,
                    result.getEntity(),
                    originPos, direction, result.getHitPoint(), result.getHitNormal(), activationId));
            return true;
        } else if (ownedEntityUsage) {
            usedOwnedEntity.send(new ActivationPredicted(character, EntityRef.NULL, originPos, direction,
                    originPos, new Vector3f(), activationId));
            character.send(new ActivationRequest(character, ownedEntityUsage, usedOwnedEntity, eventWithTarget,
                    EntityRef.NULL,
                    originPos, direction, originPos, new Vector3f(), activationId));
            return true;
        }
        return false;
    }


    @Override
    public String toString() {
        Vector3f pos = getPosition(new Vector3f());
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f)",
                pos.x, pos.y, pos.z, getViewDirection(new Vector3f()).x,
                getViewDirection(new Vector3f()).y, getViewDirection(new Vector3f()).z);
    }
}
