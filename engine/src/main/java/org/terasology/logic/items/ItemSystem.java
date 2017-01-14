/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.logic.items;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.logic.items.components.ItemComponent;
import org.terasology.logic.items.events.ItemDropEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;


public class ItemSystem {

    @In
    private EntitySystemLibrary library;

    @ReceiveEvent
    public void onItemDrop(ItemDropEvent event, EntityRef item) {
        dropItem(
                event.getItem() == EntityRef.NULL ? item : event.getItem(),
                event.getPosition());
    }

    private void dropItem(EntityRef item, Vector3f position) {
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);

        for (Component component : itemComponent.onDroppedPrefab.iterateComponents()) {
            Component componentCopy = library.getComponentLibrary().copy(component);
            if (componentCopy instanceof LocationComponent) {
                ((LocationComponent) componentCopy).setWorldPosition(position);
            }
            item.addOrSaveComponent(componentCopy);
        }

        if (!item.hasComponent(LocationComponent.class)) {
            item.addComponent(new LocationComponent(position));
        }
    }
}
