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

package org.terasology.logic.characters;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.NetworkEvent;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

import javax.vecmath.Vector3f;

/**
 * A request for a player to use an item
 * @author Immortius
 */
@ServerEvent
public class UseItemRequest extends NetworkEvent {

    @Replicate
    private EntityRef item = EntityRef.NULL;

    protected UseItemRequest() {
    }

    public UseItemRequest(EntityRef usedItem) {
        this.item = usedItem;
    }

    public EntityRef getItem() {
        return item;
    }
}
