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
import org.terasology.assets.ResourceUrn;
import org.terasology.math.Side;
import org.terasology.math.geom.Vector3i;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.shapes.BlockMeshPart;

import java.util.ArrayList;
import java.util.List;
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
            Vector3i offset = side.getVector3i();
            Block blockToCheck = view.getBlock(x + offset.x, y + offset.y, z + offset.z);
            adjacentBlocks.put(side, blockToCheck);
        }

        final ChunkMesh.RenderType renderType = getRenderType(selfBlock);
        final BlockAppearance blockAppearance = selfBlock.getPrimaryAppearance();
        final ChunkVertexFlag vertexFlag = getChunkVertexFlag(view, x, y, z, selfBlock);
        final List<String> sectionsToRender = new ArrayList<>();

        //Collect all sections that should be visible
        for (final Side side : Side.getAllSides()) {
            if (isSideVisibleForBlockTypes(adjacentBlocks.get(side), selfBlock, side)) {
                List<String> blockMeshParts = blockAppearance.getParts(side);
                List<BlockMeshPart> renderableParts = new ArrayList<>();

                // If the selfBlock isn't lowered, some more faces may have to be drawn
                if (selfBlock.isLiquid()) {
                    final Block topBlock = adjacentBlocks.get(Side.TOP);
                    // Draw horizontal sides if visible from below
                    if (topBlock.isLiquid() && Side.horizontalSides().contains(side)) {
                        final Vector3i offset = side.getVector3i();
                        final Block adjacentAbove = view.getBlock(x + offset.x, y + 1, z + offset.z);
                        final Block adjacent = adjacentBlocks.get(side);

                        if (adjacent.isLiquid() && !adjacentAbove.isLiquid()) {
                            renderableParts.add(selfBlock.getTopLiquidMesh(side));
                        }
                    } else {
                        if (blockMeshParts != null && !blockMeshParts.isEmpty()) {
                            renderableParts.add(selfBlock.getLowLiquidMesh(side));
                        }
                    }
                }

                if (!renderableParts.isEmpty()) {
                    for (BlockMeshPart part : renderableParts) {
                        Vector4f colorOffset = selfBlock.getColorOffset("default");
                        Vector4f colorSource = selfBlock.getColorSource("default").calcColor(null, x, y, z);
                        Vector4f colorResult = new Vector4f(colorSource.x * colorOffset.x, colorSource.y * colorOffset.y, colorSource.z * colorOffset.z, colorSource.w * colorOffset.w);
                        part.appendTo(chunkMesh, x, y, z, renderType, colorResult, vertexFlag);
                    }
                }

                //Add the visible pieces to the list
                if (blockMeshParts != null && !blockMeshParts.isEmpty()) {
                    for (String name : blockMeshParts) {
                        if (!sectionsToRender.contains(name)) {
                            sectionsToRender.add(name);
                        }
                    }
                }
            }
        }

        //Iterate through the list of visible sections we gathered per-side
        for (String name : sectionsToRender) {
            // TODO: Needs review since the new per-vertex flags introduce a lot of special scenarios - probably a per-side setting?
            ChunkVertexFlag sideVertexFlag = vertexFlag;
            if (selfBlock.isGrass() && !name.equals("top") && !name.equals("bottom")) {
                sideVertexFlag = ChunkVertexFlag.COLOR_MASK;
            }
            Vector4f colorOffset = selfBlock.getColorOffset(name);
            Vector4f colorSource = selfBlock.getColorSource(name).calcColor(null, x, y, z);
            Vector4f colorResult = new Vector4f(colorSource.x * colorOffset.x, colorSource.y * colorOffset.y, colorSource.z * colorOffset.z, colorSource.w * colorOffset.w);
            blockAppearance.getPart(name).appendTo(chunkMesh, x, y, z, renderType, colorResult, sideVertexFlag);
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
        for (Side side : Side.getAllSides()) {
            List<String> parts = block.getPrimaryAppearance().getParts(side);
            for (String name : parts) {
                BlockMeshPart part = block.getPrimaryAppearance().getPart(name);
                if (part != null) {
                    if (block.isDoubleSided()) {
                        tessellator.addMeshPartDoubleSided(part);
                    } else {
                        tessellator.addMeshPart(part);
                    }
                }
            }
        }
        mesh = tessellator.generateMesh(new ResourceUrn("engine", "blockmesh", block.getURI().toString()));
    }
}
