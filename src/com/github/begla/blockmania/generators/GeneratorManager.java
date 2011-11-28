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
package com.github.begla.blockmania.generators;

import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.world.main.WorldProvider;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GeneratorManager {

    private WorldProvider _parent;

    private Binding _binding;
    private static final String DEFAULT_SCRIPT_PATH = "groovy/generators/";

    /* WORLD GENERATION */
    protected final ArrayList<ChunkGenerator> _chunkGenerators = new ArrayList<ChunkGenerator>(8);
    protected final HashMap<ChunkGeneratorTerrain.BIOME_TYPE, ArrayList<TreeGenerator>> _treeGenerators = new HashMap<ChunkGeneratorTerrain.BIOME_TYPE, ArrayList<TreeGenerator>>(8);

    public GeneratorManager(WorldProvider parent) {
        _parent = parent;

        // Init. static generators
        _chunkGenerators.add(new ChunkGeneratorTerrain(this));
        _chunkGenerators.add(new ChunkGeneratorFlora(this));
        _chunkGenerators.add(new ChunkGeneratorResources(this));

        _binding = new Binding();
        _binding.setVariable("generatorManager", this);
        loadTrees();
    }

    public void loadTrees() {
        try {
            GroovyScriptEngine scriptEngine = new GroovyScriptEngine(DEFAULT_SCRIPT_PATH);
            scriptEngine.run("Default.groovy", _binding);
        } catch (IOException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
            e.printStackTrace();
        } catch (ResourceException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        } catch (ScriptException e) {
            Blockmania.getInstance().getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    public ArrayList<TreeGenerator> getTreeGenerators(ChunkGeneratorTerrain.BIOME_TYPE type) {
        return _treeGenerators.get(type);
    }

    public ArrayList<ChunkGenerator> getChunkGenerators() {
        return _chunkGenerators;
    }

    public void addTreeGenerator(ChunkGeneratorTerrain.BIOME_TYPE type, TreeGenerator gen) {
        ArrayList<TreeGenerator> list;

        if (!_treeGenerators.containsKey(type)) {
            list = new ArrayList<TreeGenerator>();
            _treeGenerators.put(type, list);
        } else {
            list = _treeGenerators.get(type);
        }

        list.add(gen);
    }

    public void removeTreeGenerator(ChunkGeneratorTerrain.BIOME_TYPE type, TreeGenerator gen) {
        ArrayList<TreeGenerator> list = _treeGenerators.get(type);

        if (list == null)
            return;

        list.remove(gen);
    }

    public void removeTreeGenerator(ChunkGeneratorTerrain.BIOME_TYPE type, int id) {
        ArrayList<TreeGenerator> list = _treeGenerators.get(type);

        if (list == null)
            return;

        list.remove(id);
    }

    public TreeGenerator getTreeGenerator(ChunkGeneratorTerrain.BIOME_TYPE type, int id) {
        ArrayList<TreeGenerator> list = _treeGenerators.get(type);

        if (list == null)
            return null;

        return list.get(id);
    }


    public WorldProvider getParent() {
        return _parent;
    }

}
