package org.terasology.codecity.world.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.terasology.codecity.world.facet.CodeCityFacet;
import org.terasology.codecity.world.map.CodeMap;
import org.terasology.codecity.world.map.CodeMapFactory;
import org.terasology.codecity.world.map.DrawableCode;
import org.terasology.codecity.world.map.MapObject;
import org.terasology.codecity.world.structure.CodeClass;
import org.terasology.codecity.world.structure.CodePackage;
import org.terasology.codecity.world.structure.scale.CodeScale;
import org.terasology.codecity.world.structure.scale.SquaredCodeScale;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Rasterizes buildings using the information provided by CodeCityFacet
 * @author alstrat
 *
 */
public class CodeCityBuildingRasterizer implements WorldRasterizer {
    private Block stone;	

    @Override
    public void initialize() {
        stone = CoreRegistry.get(BlockManager.class).getBlock("core:stone");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        CodeCityFacet codeCityFacet = chunkRegion
                .getFacet(CodeCityFacet.class);
        //Ground height
        int baseHeight = codeCityFacet.getBaseHeight();
        
        for (Vector3i position : chunkRegion.getRegion()) {
        	int height = (int) codeCityFacet.getWorld(position.x, position.z);
        	if(position.y >= baseHeight && position.y<height){
        	    chunk.setBlock(ChunkMath.calcBlockPos(position.x, position.y, position.z), stone);
        	}
        }
    }
}
