/*
 * Copyright 2013 Moving Blocks
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

import org.lwjgl.opengl.GL11;
import org.terasology.engine.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.Vector3i;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.opengl.GL11.glColorMask;

/**
 * Renderable block grid. Can be used for displaying a set of block selection boxes.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockGrid {

    public class GridPosition {
        public GridPosition(Vector3i position, byte blockType) {
            this.position = position;
            this.blockType = blockType;
        }

        public Vector3i position;
        public byte blockType;
    }

    /* CONST */
    private final Mesh _mesh;

    private final Set<GridPosition> _gridPositions = new HashSet<GridPosition>();
    private Vector3i _minBounds = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private Vector3i _maxBounds = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    public BlockGrid() {
        Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(0.0f, 0.0f, 1.0f, 0.25f), 1.005f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f);
        _mesh = tessellator.generateMesh();
    }

    public void render() {
        ShaderManager.getInstance().enableDefault();

        for (int i = 0; i < 2; i++) {
            if (i == 0) {
                glColorMask(false, false, false, false);
            } else {
                glColorMask(true, true, true, true);
            }

            for (GridPosition gp : _gridPositions) {
                GL11.glPushMatrix();

                Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
                GL11.glTranslated(gp.position.x - cameraPosition.x, gp.position.y - cameraPosition.y, gp.position.z - cameraPosition.z);

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
    public void addGridPosition(Vector3i gridPosition, byte blockType) {
        if (gridPosition.x < _minBounds.x) {
            _minBounds.x = gridPosition.x;
        }
        if (gridPosition.y < _minBounds.y) {
            _minBounds.y = gridPosition.y;
        }
        if (gridPosition.z < _minBounds.z) {
            _minBounds.z = gridPosition.z;
        }

        if (gridPosition.x > _maxBounds.x) {
            _maxBounds.x = gridPosition.x;
        }
        if (gridPosition.y > _maxBounds.y) {
            _maxBounds.y = gridPosition.y;
        }
        if (gridPosition.z > _maxBounds.z) {
            _maxBounds.z = gridPosition.z;
        }

        _gridPositions.add(new GridPosition(gridPosition, blockType));
    }

    /**
     * Removes a block position from the grid.
     *
     * @param gridPosition The block position to remove
     */
    public void removeGridPosition(BlockPosition gridPosition) {
        _gridPositions.remove(gridPosition);
    }

    public Set<GridPosition> getGridPositions() {
        return _gridPositions;
    }

    public Vector3i getMinBounds() {
        return _minBounds;
    }

    public Vector3i getMaxBounds() {
        return _maxBounds;
    }

    /**
     * Removes all block positions from the grid.
     */
    public void clear() {
        _minBounds = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
        _maxBounds = new Vector3i(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        _gridPositions.clear();
    }

}
