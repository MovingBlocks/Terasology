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
package org.terasology.rendering.particles.internal.rendering;

import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.terasology.asset.Assets;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.particles.internal.data.ParticleSystemStateData;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Created by Linus on 7-4-2015.
 */
public abstract class ParticleRenderer {

    protected static final String PARTICLE_MATERIAL_URI = "engine:prog.newParticle";

    /**
     * Vertices of a unit quad on the xy plane, centered on the origin.
     * Vertices are in clockwise order starting from the bottom left vertex.
     * @return vertices coordinates
     */
    protected static final float[] UNIT_QUAD_VERTICES = {
                -0.5f, -0.5f, 0.0f,
                -0.5f, +0.5f, 0.0f,
                +0.5f, +0.5f, 0.0f,
                +0.5f, -0.5f, 0.0f
    };

    public static ParticleRenderer create(Logger logger) {
        return new DisplayListParticleRenderer();
    }

    protected abstract void drawParticles(Material material, ParticleSystemStateData particleSystem, Vector3f camera);

    public abstract void dispose();

    public void render(WorldRenderer worldRenderer, Iterable<ParticleSystemStateData> particleSystems) {
        Material material = Assets.getMaterial(PARTICLE_MATERIAL_URI);
        material.enable();
        Vector3f camPos = worldRenderer.getActiveCamera().getPosition();

        GL11.glDisable(GL11.GL_CULL_FACE);

        for (ParticleSystemStateData particleSystem: particleSystems) {
            drawParticles(material, particleSystem, camPos);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
    }
}
