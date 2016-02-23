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
package org.terasology.logic.health;

import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.entitySystem.entity.EntityBuilder;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.AttackEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.regions.ActAsBlockComponent;
import org.terasology.world.block.sounds.BlockSounds;

/**
 * This system is responsible for giving blocks health when they are attacked and damaging them instead of destroying them.
 */
@RegisterSystem
public class BlockDamageAuthoritySystem extends BaseComponentSystem {
    private static final float BLOCK_REGEN_SECONDS = 4.0f;

    @In
    private EntityManager entityManager;

    @In
    private AudioManager audioManager;

    private Random random = new FastRandom();

    @ReceiveEvent
    public void beforeDamaged(BeforeDamagedEvent event, EntityRef blockEntity, BlockComponent blockComp) {
        if (!blockComp.getBlock().isDestructible()) {
            event.consume();
        }
    }

    @ReceiveEvent
    public void beforeDamaged(BeforeDamagedEvent event, EntityRef blockEntity, ActAsBlockComponent blockComp) {
        if (blockComp.block != null && !blockComp.block.getArchetypeBlock().isDestructible()) {
            event.consume();
        }
    }

    @ReceiveEvent(components = {BlockDamagedComponent.class})
    public void onRepaired(FullHealthEvent event, EntityRef entity) {
        entity.removeComponent(BlockDamagedComponent.class);
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity, BlockComponent blockComponent, LocationComponent locComp) {
        onDamagedCommon(event, blockComponent.getBlock().getBlockFamily(), locComp.getWorldPosition(), entity);
        if (!entity.hasComponent(BlockDamagedComponent.class)) {
            entity.addComponent(new BlockDamagedComponent());
        }
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity, ActAsBlockComponent blockComponent, LocationComponent locComp) {
        if (blockComponent.block != null) {
            onDamagedCommon(event, blockComponent.block, locComp.getWorldPosition(), entity);
        }

    }

    public void onDamagedCommon(OnDamagedEvent event, BlockFamily blockFamily, Vector3f location, EntityRef entityRef) {
        BlockDamageModifierComponent blockDamageSettings = event.getType().getComponent(BlockDamageModifierComponent.class);
        boolean skipDamageEffects = false;
        if (blockDamageSettings != null) {
            skipDamageEffects = blockDamageSettings.skipPerBlockEffects;
        }
        if (!skipDamageEffects) {
            onPlayBlockDamageCommon(blockFamily, location, entityRef);
        }
    }

    private void onPlayBlockDamageCommon(BlockFamily family, Vector3f location, EntityRef entityRef) {
        EntityBuilder builder = entityManager.newBuilder("engine:defaultBlockParticles");
        builder.getComponent(LocationComponent.class).setWorldPosition(location);
        builder.getComponent(BlockParticleEffectComponent.class).blockType = family.getURI().toString();
        builder.build();

        if (family.getArchetypeBlock().isDebrisOnDestroy()) {
            EntityBuilder dustBuilder = entityManager.newBuilder("engine:dustEffect");
            dustBuilder.getComponent(LocationComponent.class).setWorldPosition(location);
            dustBuilder.build();
        }

        BlockSounds sounds = family.getArchetypeBlock().getSounds();
        if (!sounds.getDigSounds().isEmpty()) {
            StaticSound sound = random.nextItem(sounds.getDigSounds());
            entityRef.send(new PlaySoundEvent(sound, 1f));
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity, BlockComponent blockComp) {
        beforeDamageCommon(event, blockComp.getBlock());
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity, ActAsBlockComponent blockComp) {
        if (blockComp.block != null) {
            beforeDamageCommon(event, blockComp.block.getArchetypeBlock());
        }
    }

    private void beforeDamageCommon(BeforeDamagedEvent event, Block block) {
        if (event.getDamageType() != null) {
            BlockDamageModifierComponent blockDamage = event.getDamageType().getComponent(BlockDamageModifierComponent.class);
            if (blockDamage != null) {
                BlockFamily blockFamily = block.getBlockFamily();
                for (String category : blockFamily.getCategories()) {
                    if (blockDamage.materialDamageMultiplier.containsKey(category)) {
                        event.multiply(blockDamage.materialDamageMultiplier.get(category));
                    }
                }
            }
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onAttackHealthlessBlock(AttackEvent event, EntityRef targetEntity, BlockComponent blockComponent) {
        if (!targetEntity.hasComponent(HealthComponent.class)) {
            HealthAuthoritySystem.damageEntity(event, targetEntity);
        }
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void onAttackHealthlessActAsBlock(AttackEvent event, EntityRef targetEntity, ActAsBlockComponent actAsBlockComponent) {
        if (!targetEntity.hasComponent(HealthComponent.class)) {
            HealthAuthoritySystem.damageEntity(event, targetEntity);
        }
    }

    @ReceiveEvent
    public void beforeDamagedEnsureHealthPresent(BeforeDamagedEvent event, EntityRef blockEntity, BlockComponent blockComponent) {
        if (!blockEntity.hasComponent(HealthComponent.class)) {
            Block type = blockComponent.getBlock();
            if (blockComponent != null) {
                if (type.isDestructible()) {
                    HealthComponent healthComponent = new HealthComponent(type.getHardness(), type.getHardness() / BLOCK_REGEN_SECONDS, 1.0f);
                    healthComponent.destroyEntityOnNoHealth = true;
                    blockEntity.addComponent(healthComponent);
                }
            }
        }
    }
}
