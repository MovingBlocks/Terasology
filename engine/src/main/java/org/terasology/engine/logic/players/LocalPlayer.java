// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import com.google.common.collect.Sets;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.characters.CharacterComponent;
import org.terasology.engine.logic.characters.CharacterMovementComponent;
import org.terasology.engine.logic.characters.CharacterSystem;
import org.terasology.engine.logic.characters.events.ActivationPredicted;
import org.terasology.engine.logic.characters.events.ActivationRequest;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.Direction;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.physics.HitResult;
import org.terasology.engine.physics.Physics;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

public class LocalPlayer {

    private EntityRef clientEntity = EntityRef.NULL;
    private int nextActivationId;

    //Record and Replay classes
    private DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    public LocalPlayer() {

    }

    public void setRecordAndReplayClasses(DirectionAndOriginPosRecorderList list, RecordAndReplayCurrentStatus status) {
        this.directionAndOriginPosRecorderList = list;
        this.recordAndReplayCurrentStatus = status;
    }

    public EntityRef getClientEntity() {
        return clientEntity;
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

    public Vector3f getPosition() {
        return getPosition(new Vector3f());
    }

    public Vector3f getPosition(Vector3f out) {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location == null || Float.isNaN(location.getWorldPosition().x)) {
            return out;
        }
        return location.getWorldPosition(out);
    }

    public Quat4f getRotation() {
        LocationComponent location = getCharacterEntity().getComponent(LocationComponent.class);
        if (location == null || Float.isNaN(location.getWorldPosition().x)) {
            return new Quat4f(Quat4f.IDENTITY);
        }
        return location.getWorldRotation();
    }

    public Vector3f getViewPosition() {
        return getViewPosition(new Vector3f());
    }

    public Vector3f getViewPosition(Vector3f out) {
        ClientComponent clientComponent = getClientEntity().getComponent(ClientComponent.class);
        if (clientComponent == null) {
            return out;
        }
        LocationComponent location = clientComponent.camera.getComponent(LocationComponent.class);
        if (location == null || Float.isNaN(location.getWorldPosition().x)) {
            return getPosition();
        }

        return location.getWorldPosition(out);
    }

    public Quat4f getViewRotation() {
        ClientComponent clientComponent = getClientEntity().getComponent(ClientComponent.class);
        if (clientComponent == null) {
            return new Quat4f(Quat4f.IDENTITY);
        }
        LocationComponent location = clientComponent.camera.getComponent(LocationComponent.class);
        if (location == null || Float.isNaN(location.getWorldPosition().x)) {
            return getRotation();
        }

        return location.getWorldRotation();
    }

    public Vector3f getViewDirection() {
        Quat4f rot = getViewRotation();
        // TODO: Put a generator for direction vectors in a util class somewhere
        // And just put quaternion -> vector somewhere too
        Vector3f dir = Direction.FORWARD.getVector3f();
        return rot.rotate(dir, dir);
    }

    public Vector3f getVelocity() {
        CharacterMovementComponent movement = getCharacterEntity().getComponent(CharacterMovementComponent.class);
        if (movement != null) {
            return new Vector3f(movement.getVelocity());
        }
        return new Vector3f();
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
        Vector3f direction = getViewDirection();
        Vector3f originPos = getViewPosition();
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
        return String.format("player (x: %.2f, y: %.2f, z: %.2f | x: %.2f, y: %.2f, z: %.2f)",
                getPosition().x, getPosition().y, getPosition().z, getViewDirection().x, getViewDirection().y,
                getViewDirection().z);
    }


}
