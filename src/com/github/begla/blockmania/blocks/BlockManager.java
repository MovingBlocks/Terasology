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

import javolution.util.FastMap;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.newdawn.slick.util.ResourceLoader;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockManager {

    private FastMap<String, Block> _blocksByTitle = new FastMap<String, Block>();
    private FastMap<Byte, Block> _blocksById = new FastMap<Byte, Block>();
    /* ------- */
    private static BlockManager _instance;

    public static BlockManager getInstance() {
        if (_instance == null)
            _instance = new BlockManager();

        return _instance;
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

    private BlockManager() {
        createAirBlock();
        loadBlocks();
    }

    private void createAirBlock() {
        BlockAir airBlock = new BlockAir();
        _blocksById.put((byte) 0x0, airBlock);
        _blocksByTitle.put(airBlock.getTitle(), airBlock);
    }

    private boolean loadBlocks() {
        try {
            SAXBuilder builder = new SAXBuilder();
            InputSource is = new InputSource(ResourceLoader.getResource("com/github/begla/blockmania/data/blocks/blocks.xml").openStream());
            Document doc = builder.build(is);
            Element root = doc.getRootElement();

            List<Element> content = root.getChildren();

            for (Element e : content) {
                Block b = new Block(e);
                _blocksById.put(b.getId(), b);
                _blocksByTitle.put(b.getTitle(), b);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
