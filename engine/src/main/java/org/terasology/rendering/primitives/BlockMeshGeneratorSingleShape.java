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
package org.terasology.rendering.primitives;

import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.Side;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.shapes.BlockMeshPart;

import java.util.Map;

public class BlockMeshGeneratorSingleShape implements BlockMeshGenerator {

    private Block block;
    private Mesh mesh;

    public BlockMeshGeneratorSingleShape(Block block) {
        this.block = block;
    }

    @Override
    public void generateChunkMesh(ChunkView view, ChunkMesh chunkMesh, int x, int y, int z) {
        final Block selfBlock = view.getBlock(x, y, z);

        // Gather adjacent blocks
        final Map<Side, Block> adjacentBlocks = Maps.newEnumMap(Side.class);
        for (Side side : Side.getAllSides()) {
            Vector3ic offset = side.direction();
            Block blockToCheck = view.getBlock(x + offset.x(), y + offset.y(), z + offset.z());
            adjacentBlocks.put(side, blockToCheck);
        }
        for (final Side side : Side.getAllSides()) {
            if (isSideVisibleForBlockTypes(adjacentBlocks.get(side), selfBlock, side)) {
                final ChunkMesh.RenderType renderType = getRenderType(selfBlock);
                final BlockAppearance blockAppearance = selfBlock.getPrimaryAppearance();
                final ChunkVertexFlag vertexFlag = getChunkVertexFlag(view, x, y, z, selfBlock);

                if (blockAppearance.getPart(BlockPart.CENTER) != null) {
                    blockAppearance.getPart(BlockPart.CENTER).appendTo(chunkMesh, x, y, z, renderType, vertexFlag);
                }

                BlockMeshPart blockMeshPart = blockAppearance.getPart(BlockPart.fromSide(side));

                // If the selfBlock isn't lowered, some more faces may have to be drawn
                if (selfBlock.isLiquid()) {
                    final Block topBlock = adjacentBlocks.get(Side.TOP);
                    // Draw horizontal sides if visible from below
                    if (topBlock.isLiquid() && Side.horizontalSides().contains(side)) {
                        final Vector3ic offset = side.direction();
                        final Block adjacentAbove = view.getBlock(x + offset.x(), y + 1, z + offset.z());
                        final Block adjacent = adjacentBlocks.get(side);

                        if (adjacent.isLiquid() && !adjacentAbove.isLiquid()) {
                            blockMeshPart = selfBlock.getTopLiquidMesh(side);
                        }
                    } else {
                        if (blockMeshPart != null) {
                            blockMeshPart = selfBlock.getLowLiquidMesh(side);
                        }
                    }
                }

                if (blockMeshPart != null) {
                    // TODO: Needs review since the new per-vertex flags introduce a lot of special scenarios - probably a per-side setting?
                    ChunkVertexFlag sideVertexFlag = vertexFlag;
                    if (selfBlock.isGrass() && side != Side.TOP && side != Side.BOTTOM) {
                        sideVertexFlag = ChunkVertexFlag.COLOR_MASK;
                    }
                    blockMeshPart.appendTo(chunkMesh, x, y, z, renderType, sideVertexFlag);
                }
            }
        }
    }

    private ChunkVertexFlag getChunkVertexFlag(ChunkView view, int x, int y, int z, Block selfBlock) {
        // TODO: Needs review - too much hardcoded special cases and corner cases resulting from this.
        ChunkVertexFlag vertexFlag = ChunkVertexFlag.NORMAL;
        if (selfBlock.isWater()) {
            if (view.getBlock(x, y + 1, z).isWater()) {
                vertexFlag = ChunkVertexFlag.WATER;
            } else {
                vertexFlag = ChunkVertexFlag.WATER_SURFACE;
            }
        } else if (selfBlock.isWaving() && selfBlock.isDoubleSided()) {
            vertexFlag = ChunkVertexFlag.WAVING;
        } else if (selfBlock.isWaving()) {
            vertexFlag = ChunkVertexFlag.WAVING_BLOCK;
        }
        return vertexFlag;
    }

    /**
     * Determine the render process of the block.
     * @return The render process for the block
     */
    private ChunkMesh.RenderType getRenderType(final Block selfBlock) {
        ChunkMesh.RenderType renderType = ChunkMesh.RenderType.TRANSLUCENT;

        if (!selfBlock.isTranslucent()) {
            renderType = ChunkMesh.RenderType.OPAQUE;
        }
        // TODO: Review special case, or alternatively compare uris.
        if (selfBlock.isWater() || selfBlock.isIce()) {
            renderType = ChunkMesh.RenderType.WATER_AND_ICE;
        }
        if (selfBlock.isDoubleSided()) {
            renderType = ChunkMesh.RenderType.BILLBOARD;
        }
        return renderType;
    }


    /**
     * Returns true if the side should be rendered adjacent to the second side provided.
     *
     * @param blockToCheck The block to check
     * @param currentBlock The current block
     * @return True if the side is visible for the given block types
     */
    private boolean isSideVisibleForBlockTypes(Block blockToCheck, Block currentBlock, Side side) {
        // Liquids can be transparent but there should be no visible adjacent faces
        if (currentBlock.isLiquid() && blockToCheck.isLiquid()) {
            return false;
        }

        //TODO: This only fixes the "water under block" issue of the top side not being rendered. (see bug #3889)
        //Note: originally tried .isLiquid() instead of isWater for both checks, but IntelliJ was warning that
        //      !blockToCheck.isWater() is always true, may need further investigation
        if (currentBlock.isWater() && (side == Side.TOP) && !blockToCheck.isWater()){
            return true;
        }

        return currentBlock.isWaving() != blockToCheck.isWaving() || blockToCheck.getMeshGenerator() == null
                || !blockToCheck.isFullSide(side.reverse()) || (!currentBlock.isTranslucent() && blockToCheck.isTranslucent());

    }

    @Override
    public Mesh getStandaloneMesh() {
        if (mesh == null || mesh.isDisposed()) {
            generateMesh();
        }
        return mesh;
    }

    private void generateMesh() {
        Tessellator tessellator = new Tessellator();
        for (BlockPart dir : BlockPart.values()) {
            BlockMeshPart part = block.getPrimaryAppearance().getPart(dir);
            if (part != null) {
                if (block.isDoubleSided()) {
                    tessellator.addMeshPartDoubleSided(part);
                } else {
                    tessellator.addMeshPart(part);
                }
            }
        }
        mesh = tessellator.generateMesh(new ResourceUrn("engine", "blockmesh", block.getURI().toString()));
    }
}
