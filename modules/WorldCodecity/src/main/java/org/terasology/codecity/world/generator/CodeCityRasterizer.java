package org.terasology.codecity.world.generator;

import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

public class CodeCityRasterizer implements WorldRasterizer {
	private Block dirt;

	@Override
	public void initialize() {
		dirt = CoreRegistry.get(BlockManager.class).getBlock("Core:Dirt");
	}

	@Override
	public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        SurfaceHeightFacet surfaceHeightFacet = chunkRegion.getFacet(SurfaceHeightFacet.class);
        for(Vector3i position : chunkRegion.getRegion()) {
            if(position.y < surfaceHeightFacet.getWorld(position.x, position.z)) {
                chunk.setBlock(ChunkMath.calcBlockPos(position), dirt);
            }
            if (position.y < 20 && position.x % 10 == 0 && Math.abs(position.x) < 50) {
            	chunk.setBlock(ChunkMath.calcBlockPos(position), dirt);
            }
        }
	}

}
