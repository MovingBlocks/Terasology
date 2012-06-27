package org.terasology.componentSystem;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.components.BlockParticleEffectComponent;
import org.terasology.components.BlockParticleEffectComponent.Particle;
import org.terasology.components.actions.MiniaturizerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.logic.world.WorldProvider;
import org.terasology.math.Side;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.util.Iterator;

import static org.lwjgl.opengl.GL11.*;

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
            Vector3d cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            GL11.glTranslated(min.renderPosition.x - cameraPosition.x, min.renderPosition.y - cameraPosition.y, min.renderPosition.z - cameraPosition.z);

            glScalef(MiniaturizerComponent.SCALE, MiniaturizerComponent.SCALE, MiniaturizerComponent.SCALE);
            glRotatef(min.orientation, 0, 1 ,0);

            ShaderManager.getInstance().enableShader("chunk");

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
}
