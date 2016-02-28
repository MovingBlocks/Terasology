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
package org.terasology.logic.particles;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.utilities.Assets;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.RenderSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent.Particle;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.logic.NearestSortingList;
import org.terasology.rendering.opengl.OpenGLUtil;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.tiles.WorldAtlas;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glDeleteLists;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 */
// TODO: Generalise for non-block particles
// TODO: Dispose display lists
@RegisterSystem(RegisterMode.CLIENT)
public class BlockParticleEmitterSystem extends BaseComponentSystem implements UpdateSubscriberSystem, RenderSystem {
    private static final int PARTICLES_PER_UPDATE = 32;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private WorldAtlas worldAtlas;

    // TODO: lose dependency on worldRenderer?
    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @In
    private BlockManager blockManager;

    private Random random = new FastRandom();
    private NearestSortingList sorter = new NearestSortingList();
    private int displayList;

    @Override
    public void initialise() {
        if (displayList == 0) {
            displayList = glGenLists(1);
            glNewList(displayList, GL11.GL_COMPILE);
            drawParticle();
            glEndList();
        }
        sorter.initialise(worldRenderer.getActiveCamera());
    }

    @Override
    public void shutdown() {
        glDeleteLists(displayList, 1);
        sorter.stop();
    }

    @Override
    public void update(float delta) {
        for (EntityRef entity : entityManager.getEntitiesWith(BlockParticleEffectComponent.class, LocationComponent.class)) {
            BlockParticleEffectComponent particleEffect = entity.getComponent(BlockParticleEffectComponent.class);
            Iterator<Particle> iterator = particleEffect.particles.iterator();
            while (iterator.hasNext()) {
                BlockParticleEffectComponent.Particle p = iterator.next();
                p.lifeRemaining -= delta;
                if (p.lifeRemaining <= 0) {
                    iterator.remove();
                } else {
                    updateVelocity(entity, particleEffect, p, delta);
                    updatePosition(p, delta);
                }
            }

            for (int i = 0; particleEffect.spawnCount > 0 && i < PARTICLES_PER_UPDATE; ++i) {
                spawnParticle(particleEffect);
            }

            if (particleEffect.particles.size() == 0 && particleEffect.destroyEntityOnCompletion) {
                entity.destroy();
            }
        }
    }

    @ReceiveEvent(components = {BlockParticleEffectComponent.class, LocationComponent.class})
    public void onActivated(OnActivatedComponent event, EntityRef entity) {
        sorter.add(entity);
    }

    @ReceiveEvent(components = {BlockParticleEffectComponent.class, LocationComponent.class})
    public void onDeactivated(BeforeDeactivateComponent event, EntityRef entity) {
        sorter.remove(entity);
    }


    private void spawnParticle(BlockParticleEffectComponent particleEffect) {

        Particle p = new Particle();
        p.lifeRemaining = random.nextFloat() * (particleEffect.maxLifespan - particleEffect.minLifespan) + particleEffect.minLifespan;
        p.velocity = random.nextVector3f();
        p.size = random.nextFloat() * (particleEffect.maxSize - particleEffect.minSize) + particleEffect.minSize;
        p.position.set(
                random.nextFloat(-particleEffect.spawnRange.x, particleEffect.spawnRange.x),
                random.nextFloat(-particleEffect.spawnRange.y, particleEffect.spawnRange.y),
                random.nextFloat(-particleEffect.spawnRange.z, particleEffect.spawnRange.z));
        p.color = particleEffect.color;

        if (particleEffect.blockType != null) {
            final float tileSize = worldAtlas.getRelativeTileSize();
            p.texSize.set(tileSize, tileSize);

            Block b = blockManager.getBlock(particleEffect.blockType).getBlockFamily().getArchetypeBlock();
            p.texOffset.set(b.getPrimaryAppearance().getTextureAtlasPos(BlockPart.FRONT));

            if (particleEffect.randBlockTexDisplacement) {
                final float relTileSize = worldAtlas.getRelativeTileSize();
                Vector2f particleTexSize = new Vector2f(
                        relTileSize * particleEffect.randBlockTexDisplacementScale.y,
                        relTileSize * particleEffect.randBlockTexDisplacementScale.y);

                p.texSize.x *= particleEffect.randBlockTexDisplacementScale.x;
                p.texSize.y *= particleEffect.randBlockTexDisplacementScale.y;

                p.texOffset.set(
                        p.texOffset.x + random.nextFloat() * (tileSize - particleTexSize.x),
                        p.texOffset.y + random.nextFloat() * (tileSize - particleTexSize.y));
            }
        }

        //p.texSize.set(TEX_SIZE,TEX_SIZE);
        particleEffect.particles.add(p);
        particleEffect.spawnCount--;
    }

    protected void updateVelocity(EntityRef entity, BlockParticleEffectComponent particleEffect, Particle particle, float delta) {
        Vector3f diff = new Vector3f(particleEffect.targetVelocity);
        diff.sub(particle.velocity);
        diff.x *= particleEffect.acceleration.x * delta;
        diff.y *= particleEffect.acceleration.y * delta;
        diff.z *= particleEffect.acceleration.z * delta;
        particle.velocity.add(diff);
        if (particleEffect.collideWithBlocks) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f pos = location.getWorldPosition();
            pos.add(particle.position);
            if (worldProvider.getBlock(new Vector3f(pos.x, pos.y + 2 * Math.signum(particle.velocity.y) * particle.size, pos.z)).getId() != 0x0) {
                particle.velocity.y = 0;
            }
        }
    }

    protected void updatePosition(Particle particle, float delta) {
        particle.position.x += particle.velocity.x * delta;
        particle.position.y += particle.velocity.y * delta;
        particle.position.z += particle.velocity.z * delta;
    }

    @Override
    public void renderAlphaBlend() {
        if (config.getRendering().isRenderNearest()) {
            render(Arrays.asList(sorter.getNearest(config.getRendering().getParticleEffectLimit())));
        } else {
            render(entityManager.getEntitiesWith(BlockParticleEffectComponent.class, LocationComponent.class));
        }
    }

    private void render(Iterable<EntityRef> particleEntities) {
        Assets.getMaterial("engine:prog.particle").get().enable();
        glDisable(GL11.GL_CULL_FACE);

        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        for (EntityRef entity : particleEntities) {
            LocationComponent location = entity.getComponent(LocationComponent.class);

            if (null == location) {
                continue;
            }

            Vector3f worldPos = location.getWorldPosition();

            if (!worldProvider.isBlockRelevant(worldPos)) {
                continue;
            }

            BlockParticleEffectComponent particleEffect = entity.getComponent(BlockParticleEffectComponent.class);

            if (particleEffect.texture == null) {
                Texture terrainTex = Assets.getTexture("engine:terrain").get();
                if (terrainTex == null || !terrainTex.isLoaded()) {
                    return;
                }

                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());
            } else if (particleEffect.texture.isLoaded()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                glBindTexture(GL11.GL_TEXTURE_2D, particleEffect.texture.getId());
            } else {
                return;
            }

            if (particleEffect.blendMode == BlockParticleEffectComponent.ParticleBlendMode.ADD) {
                glBlendFunc(GL_ONE, GL_ONE);
            }

            if (particleEffect.blockType != null) {
                renderBlockParticles(worldPos, cameraPosition, particleEffect);
            } else {
                renderParticles(worldPos, cameraPosition, particleEffect);
            }

            if (particleEffect.blendMode == BlockParticleEffectComponent.ParticleBlendMode.ADD) {
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            }
        }

        glEnable(GL11.GL_CULL_FACE);
    }

    private void renderBlockParticles(Vector3f worldPos, Vector3f cameraPosition, BlockParticleEffectComponent particleEffect) {

        Vector3i worldPos3i = new Vector3i(worldPos, RoundingMode.HALF_UP);
        Biome biome = worldProvider.getBiome(worldPos3i);

        glPushMatrix();
        glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);

        for (Particle particle : particleEffect.particles) {
            glPushMatrix();
            glTranslatef(particle.position.x, particle.position.y, particle.position.z);
            OpenGLUtil.applyBillboardOrientation();
            glScalef(particle.size, particle.size, particle.size);

            float light = worldRenderer.getRenderingLightValueAt(new Vector3f(worldPos.x + particle.position.x,
                    worldPos.y + particle.position.y, worldPos.z + particle.position.z));
            renderParticle(particle, blockManager.getBlock(particleEffect.blockType).getBlockFamily().getArchetypeBlock(), biome, light);
            glPopMatrix();
        }
        glPopMatrix();
    }

    private void renderParticles(Vector3f worldPos, Vector3f cameraPosition, BlockParticleEffectComponent particleEffect) {
        glPushMatrix();
        glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);

        for (Particle particle : particleEffect.particles) {
            glPushMatrix();
            glTranslatef(particle.position.x, particle.position.y, particle.position.z);
            OpenGLUtil.applyBillboardOrientation();
            glScalef(particle.size, particle.size, particle.size);

            float light = worldRenderer.getRenderingLightValueAt(new Vector3f(worldPos.x + particle.position.x,
                    worldPos.y + particle.position.y, worldPos.z + particle.position.z));

            renderParticle(particle, light);
            glPopMatrix();
        }
        glPopMatrix();
    }




    protected void renderParticle(Particle particle, float light) {
        Material mat = Assets.getMaterial("engine:prog.particle").get();

        mat.setFloat4("colorOffset", particle.color.x, particle.color.y, particle.color.z, particle.color.w, true);
        mat.setFloat2("texOffset", particle.texOffset.x, particle.texOffset.y, true);
        mat.setFloat2("texScale", particle.texSize.x, particle.texSize.y, true);
        mat.setFloat("light", light, true);

        glCallList(displayList);
    }

    protected void renderParticle(Particle particle, Block block, Biome biome, float light) {
        Material mat = Assets.getMaterial("engine:prog.particle").get();

        Vector4f colorMod = block.calcColorOffsetFor(BlockPart.FRONT, biome);
        mat.setFloat4("colorOffset", particle.color.x * colorMod.x, particle.color.y * colorMod.y, particle.color.z * colorMod.z, particle.color.w * colorMod.w, true);

        mat.setFloat2("texOffset", particle.texOffset.x, particle.texOffset.y, true);
        mat.setFloat2("texScale", particle.texSize.x, particle.texSize.y, true);
        mat.setFloat("light", light, true);

        glCallList(displayList);
    }

    private void drawParticle() {
        glBegin(GL_QUADS);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.0f);

        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(0.5f, 0.5f, 0.0f);

        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.0f);
        glEnd();
    }

    @Override
    public void renderOpaque() {
    }

    @Override
    public void renderOverlay() {
    }

    @Override
    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }
}
