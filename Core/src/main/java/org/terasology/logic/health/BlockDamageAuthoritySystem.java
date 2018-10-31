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
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.particles.components.ParticleDataSpriteComponent;
import org.terasology.particles.components.generators.TextureOffsetGeneratorComponent;
import org.terasology.registry.In;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.utilities.Assets;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.entity.damage.BlockDamageModifierComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.regions.ActAsBlockComponent;
import org.terasology.world.block.sounds.BlockSounds;
import org.terasology.world.block.tiles.WorldAtlas;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    @In
    private WorldAtlas worldAtlas;

    @In
    private BlockManager blockManager;

    private Random random = new FastRandom();

    @ReceiveEvent
    public void beforeDamaged(BeforeDamagedEvent event, EntityRef blockEntity, BlockComponent blockComp) {
        if (!blockComp.block.isDestructible()) {
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
        onDamagedCommon(event, blockComponent.block.getBlockFamily(), locComp.getWorldPosition(), entity);
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

    private void onDamagedCommon(OnDamagedEvent event, BlockFamily blockFamily, Vector3f location, EntityRef entityRef) {
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
        createBlockParticleEffect(family, location);

        BlockSounds sounds = family.getArchetypeBlock().getSounds();
        if (!sounds.getDigSounds().isEmpty()) {
            StaticSound sound = random.nextItem(sounds.getDigSounds());
            entityRef.send(new PlaySoundEvent(sound, 1f));
        }
    }

    /**
     * Creates a new entity for the block damage particle effect.
     *
     * If the terrain texture of the damaged block is available, the particles will have the block texture. Otherwise,
     * the default sprite (smoke) is used.
     *
     * @param family the {@link BlockFamily} of the damaged block
     * @param location the location of the damaged block
     */
    private void createBlockParticleEffect(BlockFamily family, Vector3f location) {
        EntityBuilder builder = entityManager.newBuilder("core:defaultBlockParticles");
        builder.getComponent(LocationComponent.class).setWorldPosition(location);

        Optional<Texture> terrainTexture = Assets.getTexture("engine:terrain");
        if (terrainTexture.isPresent() && terrainTexture.get().isLoaded()) {
            final BlockAppearance blockAppearance = family.getArchetypeBlock().getPrimaryAppearance();

            final float relativeTileSize = worldAtlas.getRelativeTileSize();
            final float particleScale = 0.25f;

            final float spriteSize = relativeTileSize * particleScale;

            ParticleDataSpriteComponent spriteComponent = builder.getComponent(ParticleDataSpriteComponent.class);
            spriteComponent.texture = terrainTexture.get();
            spriteComponent.textureSize.set(spriteSize, spriteSize);

            final List<Vector2f> offsets = computeOffsets(blockAppearance, particleScale);

            TextureOffsetGeneratorComponent textureOffsetGeneratorComponent = builder.getComponent(TextureOffsetGeneratorComponent.class);
            textureOffsetGeneratorComponent.validOffsets.addAll(offsets);
        }

        builder.build();
    }

    /**
     * Computes n random offset values for each block part texture.
     *
     * @param blockAppearance the block appearance information to generate offsets from
     * @param scale the scale of the texture area (should be in 0 < scale <= 1.0)
     *
     * @return a list of random offsets sampled from all block parts
     */
    private List<Vector2f> computeOffsets(BlockAppearance blockAppearance, float scale) {
        final float relativeTileSize = worldAtlas.getRelativeTileSize();
        final int absoluteTileSize = worldAtlas.getTileSize();
        final float pixelSize = relativeTileSize / absoluteTileSize;
        final int spriteWidth = TeraMath.ceilToInt(scale * absoluteTileSize);

        final Stream<Vector2f> baseOffsets = Arrays.stream(BlockPart.sideValues()).map(blockAppearance::getTextureAtlasPos);

        return baseOffsets.flatMap(baseOffset ->
                    IntStream.range(0, 8).boxed().map(i ->
                        new Vector2f(baseOffset).add(random.nextInt(absoluteTileSize - spriteWidth) * pixelSize, random.nextInt(absoluteTileSize - spriteWidth) * pixelSize)
                    )
                ).collect(Collectors.toList());
    }

    @ReceiveEvent(netFilter = RegisterMode.AUTHORITY)
    public void beforeDamage(BeforeDamagedEvent event, EntityRef entity, BlockComponent blockComp) {
        beforeDamageCommon(event, blockComp.block);
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
            Block type = blockComponent.block;
            if (type.isDestructible()) {
                HealthComponent healthComponent = new HealthComponent(type.getHardness(), type.getHardness() / BLOCK_REGEN_SECONDS, 1.0f);
                healthComponent.destroyEntityOnNoHealth = true;
                blockEntity.addComponent(healthComponent);
            }
        }
    }
}
