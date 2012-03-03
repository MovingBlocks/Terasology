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
package org.terasology.model.blocks.management;

import gnu.trove.map.hash.TByteObjectHashMap;
import org.lwjgl.BufferUtils;
import org.terasology.math.Side;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockGroup;

import javax.vecmath.Vector2f;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides access to blocks by block id or block title.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockManager {

    /* SINGLETON */
    private static BlockManager _instance;

    /* GROOVY */
    private BlockManifestor _manifestor;

    /* BLOCKS */
    private final HashMap<String, Block> _blocksByTitle = new HashMap<String, Block>(128);
    private final TByteObjectHashMap<Block> _blocksById = new TByteObjectHashMap<Block>(128);
    
    private final HashMap<String, BlockGroup> _blockGroupsByTitle = new HashMap<String, BlockGroup>(128);

    public static BlockManager getInstance() {
        if (_instance == null)
            _instance = new BlockManager();

        return _instance;
    }

    private BlockManager() {
        _manifestor = new BlockManifestor(this);
        loadBlocks();
    }

    private void loadBlocks() {
        try {
            _manifestor.loadConfig(); // Might have to catch plain Exception also for this step
            // System.out.println("Blocks by title: " + _blocksByTitle);
            // System.out.println("Blocks by id: " + _blocksById);
        } catch (Exception e) {
            // TODO: Totally placeholder error handling, needs to be fancier
            // System.out.println("Exception loading blocks. Sad :-(");
            // e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public BlockGroup getBlockGroup(String title) {
        return _blockGroupsByTitle.get(title);
    }

    public Block getBlock(String title) {
        return _blocksByTitle.get(title);
    }

    public Block getBlock(byte id) {
        return _blocksById.get(id);
    }

    public int availableBlocksSize() {
        return _blocksById.size();
    }

    public void addBlock(Block block) {
        _blocksById.put(block.getId(), block);
        _blocksByTitle.put(block.getTitle(), block);
    }

    public void removeBlock(Block block) {
        _blocksById.remove(block.getId());
        _blocksByTitle.remove(block.getTitle());
    }

    public void addAllBlocks(Map<Byte, Block> blocks) {
        _blocksById.putAll(blocks);
        for (Block b : blocks.values()) {
            _blocksByTitle.put(b.getTitle(), b);
        }
    }
    
    public void addAllBlockGroups(Iterable<BlockGroup> groups) {
        for (BlockGroup group : groups) {
            _blockGroupsByTitle.put(group.getTitle(), group);
        }
    }
            

    public FloatBuffer calcCoordinatesForWavingBlocks() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(32);

        int counter = 0;
        for (Block b : _blocksByTitle.values()) {
            if (b.isWaving()) {
                Vector2f pos = b.getTextureAtlasPos(Side.TOP);
                buffer.put(pos.x * Block.TEXTURE_OFFSET);
                buffer.put(pos.y * Block.TEXTURE_OFFSET);
                counter++;
            }
        }

        while (counter < 16) {
            buffer.put(-1);
            buffer.put(-1);
            counter++;
        }

        buffer.flip();
        return buffer;
    }

    public FloatBuffer calcCoordinate(String title) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(2);

        if (_blocksByTitle.containsKey(title)) {
            Vector2f position = _blocksByTitle.get(title).getTextureAtlasPos(Side.LEFT);
            buffer.put(position.x * Block.TEXTURE_OFFSET);
            buffer.put(position.y * Block.TEXTURE_OFFSET);
        }

        buffer.flip();
        return buffer;
    }
}
