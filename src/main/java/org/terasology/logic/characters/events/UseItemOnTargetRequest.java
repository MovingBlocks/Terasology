/*
 * Copyright 2013 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.logic.characters.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

import javax.vecmath.Vector3f;

/**
 * A request for a player to use an item on a target
 * @author Immortius
 */
@ServerEvent(lagCompensate = true)
public class UseItemOnTargetRequest extends UseItemRequest {

    @Replicate
    private EntityRef target = EntityRef.NULL;
    @Replicate
    private Vector3f targetPosition = new Vector3f();

    protected UseItemOnTargetRequest() {
    }

    public UseItemOnTargetRequest(EntityRef usedItem, EntityRef target, Vector3f targetPosition) {
        super(usedItem);
        this.target = target;
        this.targetPosition.set(targetPosition);
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getTargetPosition() {
        return targetPosition;
    }
}
