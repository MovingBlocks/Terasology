/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.world.block;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.componentSystem.items.ItemSystem;
import org.terasology.components.BlockParticleEffectComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.BlockDroppedEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.FullHealthEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.math.Vector3i;
import org.terasology.physics.ImpulseEvent;
import org.terasology.utilities.FastRandom;
import org.terasology.world.BlockChangedEvent;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.ConnectToAdjacentBlockFamily;
import org.terasology.world.block.management.BlockManager;

/**
 * Event handler for events affecting block entities
 *
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class BlockEntitySystem implements EventHandlerSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private EntityManager entityManager;

    @In
    private AudioManager audioManager;

    private BlockItemFactory blockItemFactory;
    private DroppedBlockFactory droppedBlockFactory;
    private FastRandom random;

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
        droppedBlockFactory = new DroppedBlockFactory(entityManager);
        random = new FastRandom();
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {BlockComponent.class})
    public void onDestroyed(NoHealthEvent event, EntityRef entity) {
        if (worldProvider == null) return;

        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        Block oldBlock = worldProvider.getBlock(blockComp.getPosition());
        worldProvider.setBlock(blockComp.getPosition(), BlockManager.getInstance().getAir(), oldBlock);

        // TODO: This should be driven by block attachment info, and not be billboard specific
        // Remove the upper block if it's a billboard
        Block upperBlock = worldProvider.getBlock(blockComp.getPosition().x, blockComp.getPosition().y + 1, blockComp.getPosition().z);
        if (upperBlock.isSupportRequired()) {
            worldProvider.setBlock(blockComp.getPosition().x, blockComp.getPosition().y + 1, blockComp.getPosition().z, BlockManager.getInstance().getAir(), upperBlock);
        }

        entity.send( new BlockChangedEvent( blockComp.getPosition(), BlockManager.getInstance().getAir(), oldBlock) );

        // TODO: Configurable via block definition
        audioManager.playSound(Assets.getSound("engine:RemoveBlock"), 0.6f);

        if (oldBlock.getEntityMode() == BlockEntityMode.PERSISTENT) {
            entity.removeComponent(HealthComponent.class);
            entity.removeComponent(BlockComponent.class);
        }

        if ((oldBlock.isDirectPickup()) && event.getInstigator().exists()) {
            EntityRef item;
            if (oldBlock.getEntityMode() == BlockEntityMode.PERSISTENT) {
                item = blockItemFactory.newInstance(oldBlock.getBlockFamily(), entity);
            } else {
                item = blockItemFactory.newInstance(oldBlock.getBlockFamily());
            }
            event.getInstigator().send(new ReceiveItemEvent(item));
            ItemComponent itemComp = item.getComponent(ItemComponent.class);
            if (itemComp != null && !itemComp.container.exists()) {
                // TODO: Fix this - entity needs to be added to lootable block or destroyed
                item.destroy();
                EntityRef block = droppedBlockFactory.newInstance(blockComp.getPosition().toVector3f(), oldBlock.getBlockFamily(), 20);
                block.send(new ImpulseEvent(random.randomVector3f(30)));
            }
        } else {
            /* PHYSICS */
            EntityRef block;
            if (oldBlock.getEntityMode() == BlockEntityMode.PERSISTENT) {
                block = droppedBlockFactory.newInstance(blockComp.getPosition().toVector3f(), oldBlock.getBlockFamily(), 20, entity);
            } else {
                block = droppedBlockFactory.newInstance(blockComp.getPosition().toVector3f(), oldBlock.getBlockFamily(), 20);
            }
            block.send(new ImpulseEvent(random.randomVector3f(30)));
            // added as a hook to catch minions breaking blocks
            event.getInstigator().send(new BlockDroppedEvent(oldBlock, block));
        }

        if (oldBlock.getEntityMode() != BlockEntityMode.PERSISTENT) {
            entity.destroy();
        }
    }

    // TODO: Need a occasionally scan for and remove temporary block entities that were never damaged?
    @ReceiveEvent(components = {BlockComponent.class})
    public void onRepaired(FullHealthEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        if (blockComp.temporary) {
            entity.destroy();
        }
    }

    @ReceiveEvent(components = {BlockComponent.class}, priority = EventPriority.PRIORITY_HIGH)
    public void onDamaged(DamageEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);

        EntityRef particlesEntity = entityManager.create();
        particlesEntity.addComponent(new LocationComponent(blockComp.getPosition().toVector3f()));

        BlockParticleEffectComponent particleEffect = new BlockParticleEffectComponent();
        particleEffect.spawnCount = 64;
        particleEffect.blockType = worldProvider.getBlock(blockComp.getPosition()).getBlockFamily();
        particleEffect.initialVelocityRange.set(4, 4, 4);
        particleEffect.spawnRange.set(0.3f, 0.3f, 0.3f);
        particleEffect.destroyEntityOnCompletion = true;
        particleEffect.minSize = 0.05f;
        particleEffect.maxSize = 0.1f;
        particleEffect.minLifespan = 1f;
        particleEffect.maxLifespan = 1.5f;
        particleEffect.targetVelocity.set(0, -5, 0);
        particleEffect.acceleration.set(2f, 2f, 2f);
        particleEffect.collideWithBlocks = true;
        particlesEntity.addComponent(particleEffect);

        // TODO: Don't play this if destroyed?
        // TODO: Configurable via block definition
        audioManager.playSound(Assets.getSound("engine:Dig"), 1.0f);
    }


    @ReceiveEvent(components = {BlockComponent.class})
    public void onReplaceAroundBlocks(BlockChangedEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);

        Vector3i placementPos = event.getBlockPosition();

        for (int i=0; i< ConnectToAdjacentBlockFamily.viewFullMap.length; i++){

            int x = ConnectToAdjacentBlockFamily.viewFullMap[i][0];
            int y = ConnectToAdjacentBlockFamily.viewFullMap[i][1];
            int z = ConnectToAdjacentBlockFamily.viewFullMap[i][2];

            Vector3i currentPos = new Vector3i(placementPos.x + x, placementPos.y + y, placementPos.z + z);

            Block aroundBlock = worldProvider.getBlock(currentPos);

            if ( aroundBlock.getBlockFamily() instanceof ConnectToAdjacentBlockFamily ){
                Block replaceBlock = ( (ConnectToAdjacentBlockFamily) aroundBlock.getBlockFamily() ).getBlockFor(currentPos, worldProvider);
                worldProvider.setBlock( currentPos, replaceBlock, aroundBlock );
            }

        }
    }

}
