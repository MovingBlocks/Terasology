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

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.terasology.components.actions.MiniaturizerComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.WorldRenderer;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
@RegisterComponentSystem(headedOnly = true)
public class MiniaturizerSystem implements UpdateSubscriberSystem, RenderSystem {

    private EntityManager entityManager;
    private WorldRenderer worldRenderer;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
        worldRenderer = CoreRegistry.get(WorldRenderer.class);
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(MiniaturizerComponent.class)) {
            MiniaturizerComponent min = entity.getComponent(MiniaturizerComponent.class);

            if (min.chunkMesh == null && min.miniatureChunk != null)
            {
                min.chunkMesh = worldRenderer.getChunkTesselator().generateMinaturizedMesh(min.miniatureChunk);
                min.chunkMesh.generateVBOs();
                min.chunkMesh._vertexElements = null;
            }

            //min.orientation += delta * 15f;
        }
    }

    public void renderTransparent() {

        for (EntityRef entity : entityManager.iteratorEntities(MiniaturizerComponent.class)) {
            MiniaturizerComponent min = entity.getComponent(MiniaturizerComponent.class);

            min.blockGrid.render();

            if (min.chunkMesh == null || min.renderPosition == null)
                continue;

            glPushMatrix();
            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            GL11.glTranslated(min.renderPosition.x - cameraPosition.x, min.renderPosition.y - cameraPosition.y, min.renderPosition.z - cameraPosition.z);

            glScalef(MiniaturizerComponent.SCALE, MiniaturizerComponent.SCALE, MiniaturizerComponent.SCALE);
            glRotatef(min.orientation, 0, 1 ,0);

            ShaderManager.getInstance().enableShader("chunk");
            ShaderManager.getInstance().getShaderProgram("chunk").setFloat("blockScale", MiniaturizerComponent.SCALE);

            min.chunkMesh.render(ChunkMesh.RENDER_PHASE.OPAQUE);
            min.chunkMesh.render(ChunkMesh.RENDER_PHASE.BILLBOARD_AND_TRANSLUCENT);
            min.chunkMesh.render(ChunkMesh.RENDER_PHASE.WATER_AND_ICE);
            glPopMatrix();

        }

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
