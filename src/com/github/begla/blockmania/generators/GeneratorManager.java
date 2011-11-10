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

import com.github.begla.blockmania.blocks.BlockManager;
import com.github.begla.blockmania.world.WorldProvider;
import javolution.util.FastMap;

import java.util.HashMap;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GeneratorManager {

    private WorldProvider _parent;

    /* WORLD GENERATION */
    protected final FastMap<String, ChunkGenerator> _chunkGenerators = new FastMap<String, ChunkGenerator>(8);
    protected final FastMap<String, ObjectGenerator> _objectGenerators = new FastMap<String, ObjectGenerator>(8);

    public GeneratorManager(WorldProvider parent) {
        _parent = parent;

        // Init. generators
        _chunkGenerators.put("terrain", new ChunkGeneratorTerrain(this));
        _chunkGenerators.put("forest", new ChunkGeneratorFlora(this));
        _chunkGenerators.put("resources", new ChunkGeneratorResources(this));

        // L-System tree 1
        HashMap<String, String> rules = new HashMap<String, String>();
        rules.put("A", "[&FFFA]////[&FFFA]////[&FFFA]");

        ObjectGeneratorLSystemTree t1 = new ObjectGeneratorLSystemTree(this, "FFFFFFA", rules);
        _objectGenerators.put("lindenTree1", t1);

        // L-System tree 2
        rules = new HashMap<String, String>();
        rules.put("A", "[&FFA]////[&FFA]////[&FFA]//[&FFFFFFB]");
        rules.put("B", "[&FFFFB][&FFB]////[&FFB]////[&FFB]");

        ObjectGeneratorLSystemTree t2 = new ObjectGeneratorLSystemTree(this, "FFFFFFFFA", rules);
        t2.setLeafType(BlockManager.getInstance().getBlock("Dark leaf").getId());

        _objectGenerators.put("lindenTree2", t2);

        _objectGenerators.put("cactus", new ObjectGeneratorCactus(this));
    }

    /**
     * Returns the object generator for the given title.
     *
     * @param s The title
     * @return The object generator
     */
    public ObjectGenerator getObjectGenerator(String s) {
        return _objectGenerators.get(s);
    }

    /**
     * Returns the chunk generator for the given title.
     *
     * @param s The title
     * @return The chunk generator
     */
    public ChunkGenerator getChunkGenerator(String s) {
        return _chunkGenerators.get(s);
    }

    public WorldProvider getParent() {
        return _parent;
    }

}
