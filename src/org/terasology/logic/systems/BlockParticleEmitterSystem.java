package org.terasology.logic.systems;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.components.BlockParticleEffectComponent;
import org.terasology.components.BlockParticleEffectComponent.Particle;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.componentSystem.RenderSystem;
import org.terasology.entitySystem.componentSystem.UpdateSubscriberSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.world.IWorldProvider;
import org.terasology.math.Side;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.rendering.particles.BlockParticleEmitter;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;


import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glScalef;

/**
 * @author Immortius <immortius@gmail.com>
 */
// TODO: Generalise for non-block particles
public class BlockParticleEmitterSystem implements UpdateSubscriberSystem, RenderSystem {
    private static final int PARTICLES_PER_UPDATE = 32;
    private static final float TEX_SIZE = Block.TEXTURE_OFFSET / 4f;

    private EntityManager entityManager;
    private IWorldProvider worldProvider;
    // TODO: lose dependency on worldRenderer?
    private WorldRenderer worldRenderer;

    private FastRandom random = new FastRandom();
    private final int[] _displayLists = new int[BlockManager.getInstance().availableBlocksSize()];

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldProvider = CoreRegistry.get(IWorldProvider.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
    }
    
    public void update(float deltaMS) {
        float delta = deltaMS / 1000;
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
            if (worldProvider.getBlockAtPosition(new Vector3d(pos.x, pos.y + 2 * Math.signum(particle.velocity.y) * particle.size, pos.z)) != 0x0)
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

        Vector3d cameraPosition = Terasology.getInstance().getActiveCamera().getPosition();

        for (EntityRef entity : entityManager.iteratorEntities(BlockParticleEffectComponent.class, LocationComponent.class)) {
            LocationComponent location = entity.getComponent(LocationComponent.class);
            Vector3f worldPos = location.getWorldPosition();

            if (!worldProvider.isChunkAvailableAt(worldPos)) {
                continue;
            }
            double temperature = worldProvider.getTemperatureAt((int)worldPos.x, (int)worldPos.z);
            double humidity = worldProvider.getHumidityAt((int)worldPos.x, (int)worldPos.z);

            glPushMatrix();
            glTranslated(worldPos.x - cameraPosition.x, worldPos.y - cameraPosition.y, worldPos.z - cameraPosition.z);
            
            BlockParticleEffectComponent particleEffect = entity.getComponent(BlockParticleEffectComponent.class);
            for (Particle particle : particleEffect.particles) {
                glPushMatrix();
                glTranslatef(particle.position.x, particle.position.y, particle.position.z);
                applyOrientation();
                glScalef(particle.size, particle.size, particle.size);

                float light = worldRenderer.getRenderingLightValueAt(new Vector3d(worldPos.x + particle.position.x, worldPos.y + particle.position.y, worldPos.z + particle.position.z));
                renderParticle(particle, particleEffect.blockType, temperature, humidity, light);
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

    protected void renderParticle(Particle particle, byte blockType, double temperature, double humidity, float light) {
        if (_displayLists[blockType] == 0) {
            _displayLists[blockType] = glGenLists(1);
            glNewList(_displayLists[blockType], GL11.GL_COMPILE);
            drawParticle(blockType);
            glEndList();
        }

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("particle");

        Vector4f color = BlockManager.getInstance().getBlock(blockType).calcColorOffsetFor(Side.FRONT, temperature, humidity);
        shader.setFloat3("colorOffset", color.x, color.y, color.z);
        shader.setFloat("texOffsetX", particle.texOffset.x);
        shader.setFloat("texOffsetY", particle.texOffset.y);
        shader.setFloat("light", light);

        glCallList(_displayLists[blockType]);
    }

    private void drawParticle(byte blockType) {
        Block b = BlockManager.getInstance().getBlock(blockType);

        glBegin(GL_QUADS);
        GL11.glTexCoord2f(b.calcTextureOffsetFor(Side.FRONT).x, b.calcTextureOffsetFor(Side.FRONT).y);
        GL11.glVertex3f(-0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Side.FRONT).x + TEX_SIZE, b.calcTextureOffsetFor(Side.FRONT).y);
        GL11.glVertex3f(0.5f, -0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Side.FRONT).x + TEX_SIZE, b.calcTextureOffsetFor(Side.FRONT).y + TEX_SIZE);
        GL11.glVertex3f(0.5f, 0.5f, 0.0f);

        GL11.glTexCoord2f(b.calcTextureOffsetFor(Side.FRONT).x, b.calcTextureOffsetFor(Side.FRONT).y + TEX_SIZE);
        GL11.glVertex3f(-0.5f, 0.5f, 0.0f);
        glEnd();

    }

    public void renderOpaque() {
    }

    public void renderOverlay() {
    }
}
