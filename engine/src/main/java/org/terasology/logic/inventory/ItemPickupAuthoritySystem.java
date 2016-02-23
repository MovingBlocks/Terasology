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

package org.terasology.logic.inventory;

import com.bulletphysics.collision.shapes.BoxShape;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.events.DropItemEvent;
import org.terasology.logic.inventory.events.GiveItemEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.VecMath;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.events.CollideEvent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.registry.In;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;

/**
 * This system uses the pickup prefab on an item component to add on extra components needed for the item to display in the world.
 * The default set of components will add a location, lifetime, and rigid body.  This will allow the item to be seen (pending it has a mesh),
 * to interact with gravity, and to disappear after a while.
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class ItemPickupAuthoritySystem extends BaseComponentSystem {
    @In
    private EntitySystemLibrary library;

    @ReceiveEvent
    public void onDropItemEvent(DropItemEvent event, EntityRef itemEntity, ItemComponent itemComponent) {
        for (Component component : itemComponent.pickupPrefab.iterateComponents()) {
            Component componentCopy = library.getComponentLibrary().copy(component);
            if (componentCopy instanceof LocationComponent) {
                ((LocationComponent) componentCopy).setWorldPosition(event.getPosition());
            }
            itemEntity.addOrSaveComponent(componentCopy);
        }

        if (!itemEntity.hasComponent(LocationComponent.class)) {
            itemEntity.addComponent(new LocationComponent(event.getPosition()));
        }
    }


    @ReceiveEvent
    public void onBumpGiveItemToEntity(CollideEvent event, EntityRef entity, PickupComponent pickupComponent) {
        GiveItemEvent giveItemEvent = new GiveItemEvent(event.getOtherEntity());
        entity.send(giveItemEvent);

        if (giveItemEvent.isHandled()) {
            // remove all the components added from the pickup prefab
            ItemComponent itemComponent = entity.getComponent(ItemComponent.class);
            if (itemComponent != null) {
                for (Component component : itemComponent.pickupPrefab.iterateComponents()) {
                    entity.removeComponent(component.getClass());
                }
            }
        }
    }

    @ReceiveEvent
    public void updateExtentsOnBlockItemBoxShape(OnAddedComponent event, EntityRef itemEntity,
                                                 BlockItemComponent blockItemComponent,
                                                 BoxShapeComponent boxShapeComponent) {
        BlockFamily blockFamily = blockItemComponent.blockFamily;
        if (blockFamily.getArchetypeBlock().getCollisionShape() instanceof BoxShape) {
            javax.vecmath.Vector3f extents = ((BoxShape) blockFamily.getArchetypeBlock().getCollisionShape()).getHalfExtentsWithoutMargin(new javax.vecmath.Vector3f());
            extents.scale(2.0f);
            extents.x = Math.max(extents.x, 0.5f);
            extents.y = Math.max(extents.y, 0.5f);
            extents.z = Math.max(extents.z, 0.5f);
            boxShapeComponent.extents.set(VecMath.from(extents));
            itemEntity.saveComponent(boxShapeComponent);
        }
    }

    @ReceiveEvent
    public void updateMassOnBlockItemRigidBody(OnAddedComponent event, EntityRef itemEntity,
                                               BlockItemComponent blockItemComponent,
                                               RigidBodyComponent rigidBodyComponent) {
        rigidBodyComponent.mass = blockItemComponent.blockFamily.getArchetypeBlock().getMass();
        itemEntity.saveComponent(rigidBodyComponent);
    }
}
