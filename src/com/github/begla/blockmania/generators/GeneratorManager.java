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

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.world.WorldProvider;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import javolution.util.FastList;

import java.io.IOException;
import java.util.ArrayList;
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
    protected final ArrayList<TreeGenerator> _treeGenerators = new ArrayList<TreeGenerator>(8);

    public GeneratorManager(WorldProvider parent) {
        _parent = parent;

        // Init. static generators
        _chunkGenerators.add(new ChunkGeneratorTerrain(this));
        _chunkGenerators.add(new ChunkGeneratorFlora(this));
        _chunkGenerators.add(new ChunkGeneratorResources(this));
        _treeGenerators.add(new TreeGeneratorCactus(this));

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

    public ArrayList<TreeGenerator> getTreeGenerators() {
        return _treeGenerators;
    }

    public ArrayList<ChunkGenerator> getChunkGenerators() {
        return _chunkGenerators;
    }

    public WorldProvider getParent() {
        return _parent;
    }

}
