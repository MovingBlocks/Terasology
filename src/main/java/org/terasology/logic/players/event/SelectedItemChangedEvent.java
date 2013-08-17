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
package org.terasology.logic.players.event;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;

/**
 * @author Immortius
 */
public class SelectedItemChangedEvent implements Event {
    private EntityRef oldItem;
    private EntityRef newItem;

    public SelectedItemChangedEvent(EntityRef oldItem, EntityRef newItem) {
        this.oldItem = oldItem;
        this.newItem = newItem;
    }

    public EntityRef getOldItem() {
        return oldItem;
    }

    public EntityRef getNewItem() {
        return newItem;
    }
}
