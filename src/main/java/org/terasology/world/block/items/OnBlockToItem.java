/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.world.block.items;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * This event is sent when a block in the world is turned into an item, to allow modification of the item entity.
 * @author Immortius
 */
public class OnBlockToItem implements Event {

    private EntityRef item;

    public OnBlockToItem(EntityRef item) {
        this.item = item;
    }

    public EntityRef getItem() {
        return item;
    }
}
