package org.terasology.components.actions;

import org.terasology.componentSystem.RenderSystem;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.entitySystem.Component;
import org.terasology.logic.world.MiniatureChunk;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.BlockGrid;

import javax.vecmath.Vector3f;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class MiniaturizerComponent implements Component {

    public static final float SCALE = 1f / 32f;

    public transient ChunkMesh chunkMesh;
    public float orientation;
    public Vector3f renderPosition;
    public MiniatureChunk miniatureChunk;
    public BlockGrid blockGrid = new BlockGrid();

    public void reset() {
        if (chunkMesh != null) {
            chunkMesh.dispose();
            chunkMesh = null;
        }

        orientation = 0;
        renderPosition = null;
        miniatureChunk = null;
    }

}
