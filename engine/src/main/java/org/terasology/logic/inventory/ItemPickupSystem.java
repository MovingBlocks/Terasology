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
import org.terasology.asset.Assets;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.entitySystem.MutableComponentContainer;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.events.ItemDroppedEvent;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.components.RigidBodyComponent;
import org.terasology.physics.events.CollideEvent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.registry.In;
import org.terasology.rendering.iconmesh.IconMeshFactory;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.rendering.logic.MeshComponent;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;


@RegisterSystem(RegisterMode.AUTHORITY)
public class ItemPickupSystem extends BaseComponentSystem {

    @In
    private InventoryManager inventoryManager;

    private static Random rand = new FastRandom();

    @ReceiveEvent(components = PickupComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        PickupComponent pickupComponent = entity.getComponent(PickupComponent.class);

        if (inventoryManager.giveItem(event.getOtherEntity(), entity, pickupComponent.itemEntity)) {
            event.getOtherEntity().send(new PlaySoundForOwnerEvent(Assets.getSound("engine:Loot").get(), 1.0f));
            pickupComponent.itemEntity = EntityRef.NULL;
            entity.destroy();
        }
    }

    @ReceiveEvent
    public void onBlockItemDropped(ItemDroppedEvent event, EntityRef itemEntity, BlockItemComponent blockItemComponent) {
        EntityBuilder builder = event.getPickup();
        if( builder.hasComponent(MeshComponent.class)) {
            addOrUpdateBlockRendering(blockItemComponent, builder);
        }

        BlockFamily blockFamily = blockItemComponent.blockFamily;
        if (blockFamily.getArchetypeBlock().getCollisionShape() instanceof BoxShape && builder.hasComponent(BoxShapeComponent.class)) {
            javax.vecmath.Vector3f extents = ((BoxShape) blockFamily.getArchetypeBlock().getCollisionShape()).getHalfExtentsWithoutMargin(new javax.vecmath.Vector3f());
            extents.scale(2.0f);
            extents.x = Math.max(extents.x, 0.5f);
            extents.y = Math.max(extents.y, 0.5f);
            extents.z = Math.max(extents.z, 0.5f);
            builder.getComponent(BoxShapeComponent.class).extents.set(VecMath.from(extents));
        }
        if (builder.hasComponent(RigidBodyComponent.class)) {
            builder.getComponent(RigidBodyComponent.class).mass = blockItemComponent.blockFamily.getArchetypeBlock().getMass();
        }
    }

    @ReceiveEvent
    public void onItemDropped(ItemDroppedEvent event, EntityRef itemEntity, ItemComponent itemComponent) {
        EntityBuilder builder = event.getPickup();
        addOrUpdateItemRendering(itemComponent, builder);
    }

    public static void addOrUpdateItemRendering(ItemComponent itemComponent, MutableComponentContainer entity) {
        if (itemComponent != null) {
            MeshComponent meshComponent = null;
            if( entity.hasComponent(MeshComponent.class)) {
                meshComponent = entity.getComponent(MeshComponent.class);
            } else {
                meshComponent = new MeshComponent();
            }
            meshComponent.material = Assets.getMaterial("engine:droppedItem").get();
            if (itemComponent.icon != null) {
                meshComponent.mesh = IconMeshFactory.getIconMesh(itemComponent.icon);
            }
            entity.addOrSaveComponent(meshComponent);
        }
    }

    public static void addOrUpdateBlockRendering(BlockItemComponent blockItemComponent, MutableComponentContainer entity) {
        if( blockItemComponent != null) {
            MeshComponent meshComponent = null;
            if( entity.hasComponent(MeshComponent.class)) {
                meshComponent = entity.getComponent(MeshComponent.class);
            } else {
                meshComponent = new MeshComponent();
            }
            BlockFamily blockFamily = blockItemComponent.blockFamily;

            if (blockFamily == null) {
                return;
            }

            meshComponent.mesh = blockFamily.getArchetypeBlock().getMesh();
            meshComponent.material = Assets.getMaterial("engine:terrain").get();

            if (blockFamily.getArchetypeBlock().getLuminance() > 0 && !entity.hasComponent(LightComponent.class)) {
                LightComponent lightComponent = entity.addComponent(new LightComponent());

                Vector3f randColor = new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                lightComponent.lightColorDiffuse.set(randColor);
                lightComponent.lightColorAmbient.set(randColor);
            }

            entity.addOrSaveComponent(meshComponent);
        }
    }

}
