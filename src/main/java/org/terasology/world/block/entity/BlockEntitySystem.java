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
package org.terasology.world.block.entity;

import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.health.FullHealthEvent;
import org.terasology.logic.health.NoHealthEvent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemPickupFactory;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.physics.ImpulseEvent;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;
import org.terasology.world.block.items.OnBlockToItem;
import org.terasology.world.block.management.BlockManager;

/**
 * Event handler for events affecting block entities
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem()
public class BlockEntitySystem implements ComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private AudioManager audioManager;

    @In
    private InventoryManager inventoryManager;

    private BlockItemFactory blockItemFactory;
    private ItemPickupFactory itemPickupFactory;
    private FastRandom random;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
        itemPickupFactory = new ItemPickupFactory(entityManager);
        random = new FastRandom();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onDestroyed(NoHealthEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        Block oldBlock = worldProvider.getBlock(blockComp.getPosition());

        // TODO: Better handling of "support required" blocks
        Block upperBlock = worldProvider.getBlock(blockComp.getPosition().x, blockComp.getPosition().y + 1, blockComp.getPosition().z);
        if (upperBlock.isSupportRequired()) {
            worldProvider.setBlock(blockComp.getPosition().x, blockComp.getPosition().y + 1, blockComp.getPosition().z, BlockManager.getAir(), upperBlock);
        }

        // TODO: Configurable via block definition
        entity.send(new PlaySoundEvent(Assets.getSound("engine:RemoveBlock"), 0.6f));

        EntityRef item = blockItemFactory.newInstance(oldBlock.getBlockFamily());
        entity.send(new OnBlockToItem(item));

        if ((oldBlock.isDirectPickup())) {
            if (!inventoryManager.giveItem(event.getInstigator(), item)) {
                EntityRef pickup = itemPickupFactory.newInstance(blockComp.getPosition().toVector3f(), 20, item);
                pickup.send(new ImpulseEvent(random.randomVector3f(30)));
            }
        } else {
            /* PHYSICS */
            EntityRef pickup = itemPickupFactory.newInstance(blockComp.getPosition().toVector3f(), 20, item);
            pickup.send(new ImpulseEvent(random.randomVector3f(30)));
        }

        worldProvider.setBlock(blockComp.getPosition(), BlockManager.getAir(), oldBlock);
    }

    @ReceiveEvent(components = {BlockComponent.class, BlockDamagedComponent.class})
    public void onRepaired(FullHealthEvent event, EntityRef entity) {
        entity.removeComponent(BlockDamagedComponent.class);
    }

    @ReceiveEvent(components = {BlockComponent.class}, priority = EventPriority.PRIORITY_HIGH)
    public void onDamaged(OnDamagedEvent event, EntityRef entity) {
        entity.send(new PlayBlockDamagedEvent(event.getInstigator()));
        if (!entity.hasComponent(BlockDamagedComponent.class)) {
            entity.addComponent(new BlockDamagedComponent());
        }
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onPlayBlockDamage(PlayBlockDamagedEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        BlockFamily family = worldProvider.getBlock(blockComp.getPosition()).getBlockFamily();
        if (family.getArchetypeBlock().isDestructible()) {
            EntityRef particles = entityManager.create("engine:blockParticles", blockComp.getPosition().toVector3f());
            BlockParticleEffectComponent comp = particles.getComponent(BlockParticleEffectComponent.class);
            comp.blockType = family;
            particles.saveComponent(comp);

            // TODO: Configurable via block definition
            audioManager.playSound(Assets.getSound("engine:Dig"), blockComp.getPosition().toVector3f());
        }
    }

}
