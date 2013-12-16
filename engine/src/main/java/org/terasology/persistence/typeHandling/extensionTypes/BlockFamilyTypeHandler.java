/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.extensionTypes;

import org.terasology.engine.CoreRegistry;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockFamilyTypeHandler extends StringRepresentationTypeHandler<BlockFamily> {

    private BlockManager blockManager = CoreRegistry.get(BlockManager.class);

    @Override
    public String getAsString(BlockFamily item) {
        return item.getURI().toString();
    }

    @Override
    public BlockFamily getFromString(String representation) {
        return blockManager.getBlockFamily(representation);
    }

}
