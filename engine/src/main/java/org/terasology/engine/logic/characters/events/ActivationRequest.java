// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.characters.events;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.NetworkEvent;
import org.terasology.engine.network.ServerEvent;

@ServerEvent(lagCompensate = true)
public class ActivationRequest extends NetworkEvent {
    /**
     * The field is used to preserve the fact that an item got used, even when the item is no more at the target server.
     */
    private boolean ownedEntityUsage;
    private EntityRef usedOwnedEntity;
    /**
     * The field is used to preserve the fact if a target got hit on the client, even when the hit target entity does
     * not exist at the target server.
     */
    private boolean eventWithTarget;
    private EntityRef target;
    private Vector3f origin;
    private Vector3f direction;
    private Vector3f hitPosition;
    private Vector3f hitNormal;
    private int activationId;

    public ActivationRequest() {
    }

    public ActivationRequest(EntityRef instigator, boolean ownedEntityUsage, EntityRef usedOwnedEntity,
                             boolean eventWithTarget, EntityRef target, Vector3f origin, Vector3f direction,
                             Vector3f hitPosition, Vector3f hitNormal, int activationId) {
        super(instigator);
        this.ownedEntityUsage = ownedEntityUsage;
        this.usedOwnedEntity = usedOwnedEntity;
        this.eventWithTarget = eventWithTarget;
        this.target = target;
        this.direction = direction;
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
        this.origin = origin;
        this.activationId = activationId;
    }

    public boolean isOwnedEntityUsage() {
        return ownedEntityUsage;
    }

    public EntityRef getUsedOwnedEntity() {
        return usedOwnedEntity;
    }

    public boolean isEventWithTarget() {
        return eventWithTarget;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getHitPosition() {
        return hitPosition;
    }

    public Vector3f getHitNormal() {
        return hitNormal;
    }

    /**
     *
     * @return a number that can be used to distinguish multiple activations from the same player.
     */
    public int getActivationId() {
        return activationId;
    }
}
