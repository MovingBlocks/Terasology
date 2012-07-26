package org.terasology.logic.world.generator.core;

import org.terasology.logic.world.WorldBiomeProvider;
import org.terasology.logic.world.chunks.Chunk;
import org.terasology.logic.world.generator.ChunkGenerator;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

public class FlatTerrainGenerator implements ChunkGenerator {

	// TODO FlatTerrainGenerator: What is a good value for MAX_Y?
	public static final int MAX_HEIGHT = Chunk.SIZE_Y - 100;
	public static final int MIN_HEIGHT = 0;
	public static final int DEFAULT_HEIGHT = 50;

	private WorldBiomeProvider biomeProvider;
	private int height;

	private final Block air;

	public FlatTerrainGenerator() {
		air = BlockManager.getInstance().getAir();
		height = DEFAULT_HEIGHT;
	}

	public FlatTerrainGenerator(int height) {
		this();
		setHeight(height);
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		if (height < MIN_HEIGHT) {
			this.height = MIN_HEIGHT;
		} else if (height > MAX_HEIGHT) {
			this.height = MAX_HEIGHT;
		} else {
			this.height = height;
		}
	}

	public boolean isValidHeight(int height) {
		return height >= MIN_HEIGHT && height <= MAX_HEIGHT;
	}

	/**
	 * Seed is not used at the moment.
	 */
	@Override
	public void setWorldSeed(String seed) {
	}

	@Override
	public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
		this.biomeProvider = biomeProvider;
	}

	@Override
	public void generateChunk(Chunk chunk) {
		for (int x = 0; x < Chunk.SIZE_X; x++) {
			for (int z = 0; z < Chunk.SIZE_Z; z++) {
				WorldBiomeProvider.Biome type = biomeProvider.getBiomeAt(
						chunk.getBlockWorldPosX(x), chunk.getBlockWorldPosZ(z));

				for (int y = Chunk.SIZE_Y; y >= 0; y--) {
					if (y == 0) {
						// bedrock/mantle
						chunk.setBlock(x, y, z, BlockManager.getInstance()
								.getBlock("MantleStone"));
					} else if (y < height) {
						// underground
						switch (type) {
						case FOREST:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Dirt"));
							break;
						case PLAINS:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Dirt"));
							break;
						case MOUNTAINS:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Stone"));
							break;
						case SNOW:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Snow"));
							break;
						case DESERT:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Sand"));
							break;
						}
					} else if (y == height) {
						// surface
						switch (type) {
						case FOREST:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Dirt"));
							break;
						case PLAINS:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Grass"));
							break;
						case MOUNTAINS:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Stone"));
							break;
						case SNOW:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Snow"));
							break;
						case DESERT:
							chunk.setBlock(x, y, z, BlockManager.getInstance()
									.getBlock("Sand"));
							break;
						}
					} else {
						// air
						chunk.setBlock(x, y, z, air);
					}
				}
			}
		}
	}

}
