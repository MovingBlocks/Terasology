package org.terasology.codecity.world.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.terasology.codecity.world.map.CodeMap;
import org.terasology.codecity.world.map.CodeMapFactory;
import org.terasology.codecity.world.map.DrawableCode;
import org.terasology.codecity.world.map.MapObject;
import org.terasology.codecity.world.structure.CodeClass;
import org.terasology.codecity.world.structure.CodePackage;
import org.terasology.codecity.world.structure.scale.CodeScale;
import org.terasology.codecity.world.structure.scale.SquaredCodeScale;
import org.terasology.math.ChunkMath;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

public class CodeCityBuildingRasterizer implements WorldRasterizer {
	private Set<MapObject> objects;
	private Block stone;

	private final CodeScale scale = new SquaredCodeScale();
	private final CodeMapFactory factory = new CodeMapFactory(scale);

	@Override
	public void initialize() {
		stone = CoreRegistry.get(BlockManager.class).getBlock("core:stone");

        CodeClass c = new CodeClass(100,150);
        CodePackage p = new CodePackage();
        p.addCodeContent(c);

        List<DrawableCode> code = new ArrayList<DrawableCode>();
        code.add(c.getDrawableCode());
        code.add(p.getDrawableCode());

        CodeMap map = factory.generateMap(code);
        objects = map.getMapObjects();
	}

	@Override
	public void generateChunk(CoreChunk chunk, Region chunkRegion) {
		SurfaceHeightFacet surfaceHeightFacet = chunkRegion.getFacet(SurfaceHeightFacet.class);
		int yBase = (int)surfaceHeightFacet.get(0, 0);
		for (MapObject object : objects) {
			if (chunkRegion.getRegion().encompasses(object.getPositionX(), object.getPositionZ(), yBase)) {
				for (int i = 0; i < object.getHeight(scale, factory); i++)
					chunk.setBlock(ChunkMath.calcBlockPos(object.getPositionX(), yBase+i, object.getPositionZ()), stone);
			}
		}
	}
}
