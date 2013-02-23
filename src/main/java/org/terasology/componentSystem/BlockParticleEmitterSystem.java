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
package org.terasology.componentSystem;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glCallList;
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
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import java.nio.FloatBuffer;
import java.util.Iterator;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.components.BlockParticleEffectComponent;
import org.terasology.components.BlockParticleEffectComponent.Particle;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.management.BlockManager;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Generalise for non-block particles
// TODO: Dispose display lists
@RegisterComponentSystem(headedOnly = true)
public class BlockParticleEmitterSystem implements UpdateSubscriberSystem, RenderSystem {
    private static final int PARTICLES_PER_UPDATE = 32;
    private static final float TEX_SIZE = Block.TEXTURE_OFFSET / 4f;

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    // TODO: lose dependency on worldRenderer?
    @In
    private WorldRenderer worldRenderer;

    private FastRandom random = new FastRandom();
    private TObjectIntMap displayLists;

    public void initialise() {
        displayLists = new TObjectIntHashMap(BlockManager.getInstance().getBlockFamilyCount());
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(BlockParticleEffectComponent.class, LocationComponent.class)) {
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
            } else {
                entity.saveComponent(particleEffect);
            }
        }
    }

    private void spawnParticle(BlockParticleEffectComponent particleEffect) {
        Particle p = new Particle();
        p.lifeRemaining = random.randomPosFloat() * (particleEffect.maxLifespan - particleEffect.maxLifespan) + particleEffect.minLifespan;
        p.velocity.set(particleEffect.initialVelocityRange.x * random.randomFloat(), particleEffect.initialVelocityRange.y * random.randomFloat(), particleEffect.initialVelocityRange.z * random.randomFloat());
        p.size = random.randomPosFloat() * (particleEffect.maxSize - particleEffect.minSize) + particleEffect.minSize;
        p.position.set(particleEffect.spawnRange.x * random.randomFloat(), particleEffect.spawnRange.y * random.randomFloat(), particleEffect.spawnRange.z * random.randomFloat());
        p.texOffset.set(random.randomPosFloat() * (Block.TEXTURE_OFFSET - TEX_SIZE), random.randomPosFloat() * (Block.TEXTURE_OFFSET - TEX_SIZE));
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
        if (particleEffect.collideWithBlocks == true) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f pos = location.getWorldPosition();
            pos.add(particle.position);
            if (worldProvider.getBlock(new Vector3f(pos.x, pos.y + 2 * Math.signum(particle.velocity.y) * particle.size, pos.z)).getId() != 0x0)
                particle.velocity.y = 0;
        }
    }

    protected void updatePosition(Particle particle, float delta) {
        particle.position.x += particle.velocity.x * delta;
        particle.position.y += particle.velocity.y * delta;
        particle.position.z += particle.velocity.z * delta;
    }

    public void renderTransparent() {
        ShaderManager.getInstance().enableShader("particle");
        glDisable(GL11.GL_CULL_FACE);

        Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();

        for (EntityRef entity : entityManager.iteratorEntities(BlockParticleEffectComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            if (!worldProvider.isBlockActive(worldPos)) {
                continue;
            }
            float temperature = worldProvider.getBiomeProvider().getTemperatureAt((int) worldPos.x, (int) worldPos.z);
            float humidity = worldProvider.getBiomeProvider().getHumidityAt((int) worldPos.x, (int) worldPos.z);

            glPushMatrix();
            glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);

            BlockParticleEffectComponent particleEffect = entity.getComponent(BlockParticleEffectComponent.class);
            if (particleEffect.blockType == null) {
                return;
            }
            for (Particle particle : particleEffect.particles) {
                glPushMatrix();
                glTranslatef(particle.position.x, particle.position.y, particle.position.z);
                applyOrientation();
                glScalef(particle.size, particle.size, particle.size);

                float light = worldRenderer.getRenderingLightValueAt(new Vector3f(worldPos.x + particle.position.x, worldPos.y + particle.position.y, worldPos.z + particle.position.z));
                renderParticle(particle, particleEffect.blockType.getArchetypeBlock().getId(), temperature, humidity, light);
                glPopMatrix();
            }
            glPopMatrix();
        }

        glEnable(GL11.GL_CULL_FACE);
    }

    private void applyOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j)
                    model.put(i * 4 + j, 1.0f);
                else
                    model.put(i * 4 + j, 0.0f);
            }
        }

        GL11.glLoadMatrix(model);
    }

    protected void renderParticle(Particle particle, byte blockType, float temperature, float humidity, float light) {
        int displayList = displayLists.get(BlockManager.getInstance().getBlock(blockType).getBlockFamily());
        if (displayList == 0) {
            displayList = glGenLists(1);
            glNewList(displayList, GL11.GL_COMPILE);
            drawParticle(blockType);
            glEndList();
            displayLists.put(BlockManager.getInstance().getBlock(blockType).getBlockFamily(), displayList);
        }

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("particle");

        Vector4f color = BlockManager.getInstance().getBlock(blockType).calcColorOffsetFor(BlockPart.FRONT, temperature, humidity);
        shader.setFloat3("colorOffset", color.x, color.y, color.z);
        shader.setFloat("texOffsetX", particle.texOffset.x);
        shader.setFloat("texOffsetY", particle.texOffset.y);
        shader.setFloat("light", light);

        glCallList(displayList);
    }

    private void drawParticle(byte blockType) {
        Block b = BlockManager.getInstance().getBlock(blockType);

        glBegin(GL_QUADS);
        GL11.glTexCoord2f(b.getTextureOffsetFor(BlockPart.FRONT).x, b.getTextureOffsetFor(BlockPart.FRONT).y);
        GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.getTextureOffsetFor(BlockPart.FRONT).x + TEX_SIZE, b.getTextureOffsetFor(BlockPart.FRONT).y);
        GL11.glVertex3f(0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.getTextureOffsetFor(BlockPart.FRONT).x + TEX_SIZE, b.getTextureOffsetFor(BlockPart.FRONT).y + TEX_SIZE);
        GL11.glVertex3f(0.5f, 0.5f, 0.0f);

        GL11.glTexCoord2f(b.getTextureOffsetFor(BlockPart.FRONT).x, b.getTextureOffsetFor(BlockPart.FRONT).y + TEX_SIZE);
        GL11.glVertex3f(-0.5f, 0.5f, 0.0f);
        glEnd();

    }

    public void renderOpaque() {
    }

    public void renderOverlay() {
    }

    public void renderFirstPerson() {
    }

    @Override
    public void renderShadows() {
    }
}
