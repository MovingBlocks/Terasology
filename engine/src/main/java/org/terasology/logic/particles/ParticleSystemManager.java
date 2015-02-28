/*
 * Copyright 2015 MovingBlocks
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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.*;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.particles.BlockParticleEffectComponent.Particle;
import org.terasology.math.Vector3i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.physics.Physics;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.logic.NearestSortingList;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.loader.WorldAtlas;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;

/**
 * Component system responsible for all ParticleSystem components.
 *
 * @author Linus van Elswijk <linusvanelswijk@gmail.com>
 */
@RegisterSystem(RegisterMode.CLIENT)
public class ParticleSystemManager extends BaseComponentSystem implements UpdateSubscriberSystem, RenderSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @In
    private Physics physics;

    public void initialise() {
        /*
        if (displayList == 0) {
            displayList = glGenLists(1);
            glNewList(displayList, GL11.GL_COMPILE);
            drawParticle();
            glEndList();
        }
        sorter.initialise(worldRenderer.getActiveCamera());
        */
    }

    @Override
    public void shutdown() {
        /*
        glDeleteLists(displayList, 1);
        sorter.stop();
        */
    }

    public void update(float delta) {

    }

    public void renderAlphaBlend() {
        /*
        if (config.getRendering().isRenderNearest()) {
            render(Arrays.asList(sorter.getNearest(config.getRendering().getParticleEffectLimit())));
        } else {
            render(entityManager.getEntitiesWith(ParticleSystemComponent.class, LocationComponent.class));
        }
        */
    }

    public void renderOpaque() {
    }

    public void renderOverlay() {
    }

    public void renderFirstPerson() {
    }

    // managing particle system data


    public void update(ParticleSystemComponent particles) {
        /*for (int i = 0; i < particles.firstDeadParticleIndex; i++) {

        }*/
    }

    @Override
    public void renderShadows() {
    }

    private void render(Iterable<EntityRef> particleSystemEntities) {
        /**
        Assets.getMaterial("engine:prog.particle").enable();
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
                Texture terrainTex = Assets.getTexture("engine:terrain");
                if (terrainTex == null) {
                    return;
                }

                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());
            } else {
                GL13.glActiveTexture(GL13.GL_TEXTURE0);
                glBindTexture(GL11.GL_TEXTURE_2D, particleEffect.texture.getId());
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
         */
    }

    private void applyOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    model.put(i * 4 + j, 1.0f);
                } else {
                    model.put(i * 4 + j, 0.0f);
                }
            }
        }

        GL11.glLoadMatrix(model);
    }
}
