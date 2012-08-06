/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.model.blocks.management

import org.terasology.math.Rotation
import org.terasology.model.blocks.Block
import org.terasology.model.blocks.TreeBlock

/**
 * @author Immortius <immortius@gmail.com>
 */
class TreeBlockLoader extends SimpleBlockLoader {

    public TreeBlockLoader(Map<String, Integer> imageIndex)
    {
        super(imageIndex);
    }

    public Block loadBlock(ConfigObject blockConfig, Rotation rotation) {
        TreeBlock block = new TreeBlock();
        configureBlock(block, blockConfig, rotation);
        return block;
    }

    protected void configureBlock(TreeBlock b, ConfigObject c, Rotation rotation)
    {
        super.configureBlock(b, c, rotation);

        // Now load extra stuff
    }
}
