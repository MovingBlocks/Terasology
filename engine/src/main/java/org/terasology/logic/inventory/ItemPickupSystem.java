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
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.events.ItemDroppedEvent;
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

import javax.vecmath.Vector3f;


@RegisterSystem(RegisterMode.AUTHORITY)
public class ItemPickupSystem extends BaseComponentSystem {

    @In
    private InventoryManager inventoryManager;

    private Random rand = new FastRandom();

    @ReceiveEvent(components = PickupComponent.class)
    public void onBump(CollideEvent event, EntityRef entity) {
        PickupComponent pickupComponent = entity.getComponent(PickupComponent.class);

        if (inventoryManager.giveItem(event.getOtherEntity(), entity, pickupComponent.itemEntity)) {
            event.getOtherEntity().send(new PlaySoundForOwnerEvent(Assets.getSound("engine:Loot"), 1.0f));
            pickupComponent.itemEntity = EntityRef.NULL;
            entity.destroy();
        }
    }

    @ReceiveEvent
    public void onBlockItemDropped(ItemDroppedEvent event, EntityRef itemEntity, BlockItemComponent blockItemComponent) {
        EntityBuilder builder = event.getPickup();
        BlockFamily blockFamily = blockItemComponent.blockFamily;
        if (builder.hasComponent(MeshComponent.class)) {
            MeshComponent mesh = builder.getComponent(MeshComponent.class);
            mesh.mesh = blockFamily.getArchetypeBlock().getMesh();
            mesh.material = Assets.getMaterial("engine:terrain");
        }
        if (blockFamily.getArchetypeBlock().getCollisionShape() instanceof BoxShape && builder.hasComponent(BoxShapeComponent.class)) {
            Vector3f extents = ((BoxShape) blockFamily.getArchetypeBlock().getCollisionShape()).getHalfExtentsWithoutMargin(new Vector3f());
            extents.scale(2.0f);
            extents.clampMin(0.5f);
            builder.getComponent(BoxShapeComponent.class).extents.set(extents);
        }
        if (blockFamily.getArchetypeBlock().getLuminance() > 0 && !builder.hasComponent(LightComponent.class)) {
            LightComponent lightComponent = builder.addComponent(new LightComponent());

            Vector3f randColor = new Vector3f(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            lightComponent.lightColorDiffuse.set(randColor);
            lightComponent.lightColorAmbient.set(randColor);
        }

        if (builder.hasComponent(RigidBodyComponent.class)) {
            builder.getComponent(RigidBodyComponent.class).mass = blockItemComponent.blockFamily.getArchetypeBlock().getMass();
        }
    }

    @ReceiveEvent
    public void onItemDropped(ItemDroppedEvent event, EntityRef itemEntity, ItemComponent itemComponent) {
        EntityBuilder builder = event.getPickup();
        if (builder.hasComponent(MeshComponent.class)) {
            MeshComponent mesh = builder.getComponent(MeshComponent.class);
            if (mesh.mesh == null && itemComponent.icon != null) {
                builder.getComponent(MeshComponent.class).mesh = IconMeshFactory.getIconMesh(itemComponent.icon);
            }
        }
    }
}
