/*
 * Copyright 2013  Philip Wernersbach <philip.wernersbach@gmail.com>
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

package org.terasology.logic;

import java.util.Random;

import org.slf4j.LoggerFactory;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;

/*
 * This class contains the improved spawn block algorithm. The algorithm is simple and
 * world generator neutral. See isPointAnAcceptibleSpawnPoint() for more info. 
 * 
 * @author Philip Wernersbach
 */
public class SpawnManager {
	private WorldProvider worldProvider = null;
	private Vector3i spawnPoint = null;
	
	private Block[] acceptibleSpawnBlocks = null;
	private Block airBlock = null;
	
	private static final Random numberGenerator_Static = new Random();
	private int airAboveSpawn = 2;
	private boolean isWaterSpawnAcceptible = false;
	
	/*
	 * For some reason, there's a corner case where a person gets a bad Random Number Generator
	 * and the algorithm can never find a suitable block.
	 * (This behavior is seemingly random.)
	 * 
	 *  We have this one as non-static so we can reset the RNG if needed.
	 */
	private Random numberGenerator = numberGenerator_Static;
	
	
	public SpawnManager(WorldProvider provider) {
		Block[] blocks = new Block[] { BlockManager.getInstance().getBlock("engine:Grass"), 
									   BlockManager.getInstance().getBlock("engine:Sand"),
									   BlockManager.getInstance().getBlock("engine:Snow") };
		
		setProperties(provider, spawnPoint, blocks, airAboveSpawn, isWaterSpawnAcceptible);
	}
	
	public SpawnManager(WorldProvider provider, Vector3i point) {
		Block[] blocks = new Block[] { BlockManager.getInstance().getBlock("engine:Grass"), 
				   BlockManager.getInstance().getBlock("engine:Sand"),
				   BlockManager.getInstance().getBlock("engine:Snow") };
		
		setProperties(provider, point, blocks, airAboveSpawn, isWaterSpawnAcceptible);
	}
	
	public SpawnManager(WorldProvider provider, Vector3i point, Block[] spawnBlocks, int blockCount, boolean waterAcceptible) {
		setProperties(provider, point, spawnBlocks, blockCount, waterAcceptible);
	}
	
	private void setProperties(WorldProvider provider, Vector3i point, Block[] spawnBlocks, int blockCount, boolean waterAcceptible)
	{
		worldProvider = provider;
		spawnPoint = point;
		acceptibleSpawnBlocks = spawnBlocks;
		airBlock = BlockManager.getInstance().getAir();
		airAboveSpawn = blockCount;
		isWaterSpawnAcceptible = waterAcceptible;
	}
	
	public Vector3i getRandomPoint()
	{
		return new Vector3i(numberGenerator.nextInt(Chunk.SIZE_X + 1), numberGenerator.nextInt(Chunk.SIZE_Y + 1), numberGenerator.nextInt(Chunk.SIZE_Z + 1));
	}
	
	public Block pointToBlock(Vector3i point) {
		return worldProvider.getBlock(point);
	}
	
	public boolean isBlockOfAcceptibleType(Block block) {
		for (Block acceptibleBlock : acceptibleSpawnBlocks)
			if (block == acceptibleBlock)
				return true;
		
		return false;
	}
	
	/*
	 * The spawn algorithm. This algorithm is world generator neutral.
	 * Currently, here's what it does:
	 * 
	 * 1) Check to see if the chosen block is of an allowed type.
	 *    This is so we can ensure that the spawn point is outside.
	 *    
	 * 2) Check if the specified number of blocks above the spawn point
	 *    are empty
	 *    
	 * 2a) Check if the specified number of blocks above the spawn point
	 *     are water, and bail if they are and a water spawn is not allowed.
	 */
	public boolean isPointAnAcceptibleSpawnPoint(Vector3i point) {
		Block block = pointToBlock(point);
		Block aboveBlock = null;
		
		if (!isBlockOfAcceptibleType(block))
			return false;
		
		// Check if the required number of blocks above are air
		for (int i = 1; i <= airAboveSpawn; i++) {
			aboveBlock = pointToBlock(new Vector3i(point.x, point.y + i, point.z));
			if (aboveBlock == airBlock) {
				// If required, check if water spawn
				if (!isWaterSpawnAcceptible && aboveBlock.isLiquid()) {
					return false;
				}
			} else {
				return false;
			}
		}
					
		return true;
	}
	
	public Vector3i getRandomSpawnPoint() {
		int passes = 0;
		Vector3i point = getRandomPoint();
		
		while (!isPointAnAcceptibleSpawnPoint(point)) {
			// In tests, this loop never ran for more than
			// 1179 passes. If we get to 2000 passes, declare
			// this a bad RNG and use a new one.
			if (passes == 2000) {
				LoggerFactory.getLogger(SpawnManager.class).warn("Java gave us a bad RNG, getting a new one!");
				
				numberGenerator = new Random();
				passes = 0;
			}
			
			passes++;
			point = getRandomPoint();
		}
		
		return point;
	}
	
	public int getRequiredAirAboveSpawn() {
		return airAboveSpawn;
	}
	
	public void ensureSpawnIsClear() {
		Vector3i aboveBlockPoint = null;
		for (int i = 1; i <= airAboveSpawn; i++) {
			aboveBlockPoint = new Vector3i(spawnPoint.x, spawnPoint.y + i, spawnPoint.z);
			worldProvider.setBlock(spawnPoint, BlockManager.getInstance().getAir(), pointToBlock(aboveBlockPoint));
		}
	}
	
	public static void ensureSpawnIsClear(WorldProvider provider, Vector3i point) {
		(new SpawnManager(provider, point)).ensureSpawnIsClear();
	}
	
	public static Vector3i getRandomSpawnPoint(WorldProvider provider) {
		return (new SpawnManager(provider)).getRandomSpawnPoint();
	}
}
