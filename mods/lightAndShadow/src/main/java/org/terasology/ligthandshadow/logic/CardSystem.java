/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.ligthandshadow.logic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.components.BlockParticleEffectComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockRegionComponent;
import org.terasology.world.block.management.BlockManager;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class CardSystem implements EventHandlerSystem {
    private static final Logger logger = LoggerFactory.getLogger(CardSystem.class);

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private AudioManager audioManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CardComponent.class, ItemComponent.class})
    public void placeCard(ActivateEvent event, EntityRef entity) {
        CardComponent card = entity.getComponent(CardComponent.class);
        BlockComponent targetBlockComponent = event.getTarget().getComponent(BlockComponent.class);
        if (targetBlockComponent == null) {
            event.cancel();
            return;
        }

        Vector3f horizontalDir = new Vector3f(event.getDirection());
        horizontalDir.y = 0;
        Side facingDir = Side.inDirection(horizontalDir);
        if (!facingDir.isHorizontal()) {
            event.cancel();
            return;
        }

        Vector3f offset = new Vector3f(event.getHitPosition());
        offset.sub(targetBlockComponent.getPosition().toVector3f());
        Side offsetDir = Side.inDirection(offset);

        Vector3i primePos = new Vector3i(targetBlockComponent.getPosition());
        primePos.add(offsetDir.getVector3i());

        Block primeBlock = worldProvider.getBlock(primePos);
        if (!primeBlock.isReplacementAllowed()) {
            event.cancel();
            return;
        }
        logger.info("Prime block {} at pos {}", primeBlock, primePos);
        Block belowBlock = worldProvider.getBlock(primePos.x, primePos.y - 1, primePos.z);
        Block aboveBlock = worldProvider.getBlock(primePos.x, primePos.y + 1, primePos.z);

        // Determine top and bottom blocks
        Vector3i bottomBlockPos;
        Block bottomBlock;
        Vector3i topBlockPos;
        Block topBlock;
        if (belowBlock.isReplacementAllowed()) {
            bottomBlockPos = new Vector3i(primePos.x, primePos.y - 1, primePos.z);
            bottomBlock = belowBlock;
            topBlockPos = primePos;
            topBlock = primeBlock;
        } else if (aboveBlock.isReplacementAllowed()) {
            bottomBlockPos = primePos;
            bottomBlock = primeBlock;
            topBlockPos = new Vector3i(primePos.x, primePos.y + 1, primePos.z);
            topBlock = aboveBlock;
        } else {
            event.cancel();
            return;
        }

        worldProvider.setBlock(bottomBlockPos, card.bottomBlockFamily.getBlockForPlacing(worldProvider,
            bottomBlockPos, facingDir, Side.TOP), bottomBlock);
        worldProvider.setBlock(topBlockPos, card.topBlockFamily.getBlockForPlacing(worldProvider, topBlockPos, facingDir,
            Side.TOP), topBlock);

        EntityRef newCard = entityManager.copy(entity);
        newCard.addComponent(new BlockRegionComponent(Region3i.createBounded(bottomBlockPos, topBlockPos)));
        Vector3f cardCenter = bottomBlockPos.toVector3f();
        cardCenter.y += 0.5f;
        newCard.addComponent(new LocationComponent(cardCenter));
        CardComponent newCardComponent = newCard.getComponent(CardComponent.class);
        newCard.saveComponent(newCardComponent);
        newCard.removeComponent(ItemComponent.class);
        audioManager.playSound(Assets.getSound("engine:PlaceBlock"), 0.5f);
    }

    @ReceiveEvent(components = {CardComponent.class, LocationComponent.class})
    public void onDamaged(DamageEvent event, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        CardComponent cardComponent = entity.getComponent(CardComponent.class);
        Vector3f center = location.getWorldPosition();
        EntityRef particlesEntity = entityManager.create();
        particlesEntity.addComponent(new LocationComponent(center));

        BlockParticleEffectComponent particleEffect = new BlockParticleEffectComponent();
        particleEffect.spawnCount = 64;
        particleEffect.blockType = cardComponent.bottomBlockFamily;
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

        audioManager.playSound(Assets.getSound("engine:Dig"), 1.0f);
    }

    @ReceiveEvent(components = {CardComponent.class, BlockRegionComponent.class})
    public void onOutOfHealth(NoHealthEvent event, EntityRef entity) {
        BlockRegionComponent blockRegionComponent = entity.getComponent(BlockRegionComponent.class);
        for (Vector3i blockPos : blockRegionComponent.region) {
            worldProvider.setBlock(blockPos, BlockManager.getInstance().getAir(), worldProvider.getBlock(blockPos));
        }
        EntityInfoComponent entityInfo = entity.getComponent(EntityInfoComponent.class);
        if (entityInfo != null) {
            EntityRef cardItem = entityManager.create(entityInfo.parentPrefab);
            if (event.getInstigator().exists()) {
                event.getInstigator().send(new ReceiveItemEvent(cardItem));
            }
            ItemComponent itemComp = cardItem.getComponent(ItemComponent.class);
            if (itemComp != null && !itemComp.container.exists()) {
                cardItem.destroy();
            }
        }
        entity.destroy();
        audioManager.playSound(Assets.getSound("engine:RemoveBlock"), 0.6f);
    }
}
