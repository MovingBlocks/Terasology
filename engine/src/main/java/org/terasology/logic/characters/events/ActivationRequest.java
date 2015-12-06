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

package org.terasology.logic.characters.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.NetworkEvent;
import org.terasology.network.ServerEvent;

/**
 */
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
