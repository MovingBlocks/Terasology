/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.selection;

import org.terasology.module.sandbox.API;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.Region3i;

/**
 * This event is fired once a player finished a selection using an item with a BlockSelectionComponent. The item used
 * is included in the event.
 *
 */
@API
public class ApplyBlockSelectionEvent implements Event {
    private final Region3i selection;
    private final EntityRef selectedItemEntity;

    public ApplyBlockSelectionEvent(EntityRef selectedItemEntity, Region3i selection) {
        this.selectedItemEntity = selectedItemEntity;
        this.selection = selection;
    }

    public Region3i getSelection() {
        return selection;
    }

    public EntityRef getSelectedItemEntity() {
        return selectedItemEntity;
    }
}
