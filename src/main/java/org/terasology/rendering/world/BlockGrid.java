/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.rendering.world;

import static org.lwjgl.opengl.GL11.glColorMask;

import java.util.HashSet;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.interfaces.IGameObject;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

/**
 * Renderable block grid. Can be used for displaying a set of block selection boxes.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockGrid implements IGameObject {

    /* CONST */
    private final Mesh _mesh;

    private final HashSet<BlockPosition> _gridPositions = new HashSet<BlockPosition>();

    public BlockGrid() {
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(0.0f, 0.0f, 1.0f, 0.25f), 1.005f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f);
        _mesh = tessellator.generateMesh();
    }

    @Override
    public void render() {
        ShaderManager.getInstance().enableDefault();

        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                glColorMask(false, false, false, false);
            } else {
                glColorMask(true, true, true, true);
            }

            for (BlockPosition gp : _gridPositions) {
                GL11.glPushMatrix();

                Vector3d cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
                GL11.glTranslated(gp.x - cameraPosition.x, gp.y - cameraPosition.y, gp.z - cameraPosition.z);

                _mesh.render();

                GL11.glPopMatrix();
            }
        }
    }

    /**
     * Adds a block position to the grid.
     *
     * @param gridPosition The block position to add
     */
    public void addGridPosition(BlockPosition gridPosition) {
        _gridPositions.add(gridPosition);
    }

    /**
     * Removes a block position from the grid.
     *
     * @param gridPosition The block position to remove
     */
    public void removeGridPosition(BlockPosition gridPosition) {
        _gridPositions.remove(gridPosition);
    }

    /**
     * Removes all block positions from the grid.
     */
    public void clear() {
        _gridPositions.clear();
    }

    @Override
    public void update(float delta) {
        // Nothing to do.
    }
}
