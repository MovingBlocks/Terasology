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
package com.github.begla.blockmania.blocks;

import com.github.begla.blockmania.game.Blockmania;
import gnu.trove.map.hash.TByteObjectHashMap;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * Provides access to blocks by block id or block title.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockManager {

    /* CONST */
    private static final String DEFAULT_SCRIPT_PATH = "groovy/blocks/";

    /* SINGLETON */
    private static BlockManager _instance;

    /* GROOVY */
    private Binding _binding;

    /* BLOCKS */
    private HashMap<String, Block> _blocksByTitle = new HashMap<String, Block>(128);
    private TByteObjectHashMap<Block> _blocksById = new TByteObjectHashMap<Block>(128);

    public static BlockManager getInstance() {
        if (_instance == null)
            _instance = new BlockManager();

        return _instance;
    }

    private BlockManager() {
        _binding = new Binding();
        _binding.setVariable("blockManager", this);

        loadBlocks();
    }

    private void loadBlocks() {
        try {
            GroovyScriptEngine scriptEngine = new GroovyScriptEngine(DEFAULT_SCRIPT_PATH);
            scriptEngine.run("Default.groovy", _binding);
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        } catch (ResourceException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        } catch (ScriptException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
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
}
