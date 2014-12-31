/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.commands.adapter;

import com.google.common.base.Preconditions;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

/**
 * @author Limeth
 */
public class BlockFamilyAdapter implements CommandParameterAdapter<BlockFamily> {
    @Override
    public BlockFamily parse(String composed) {
        Preconditions.checkNotNull(composed, "'composed' must not be null!");
        return CoreRegistry.get(BlockManager.class).getBlockFamily(composed);
    }

    @Override
    public String compose(BlockFamily parsed) {
        Preconditions.checkNotNull(parsed, "'parsed' must not be null!");
        return parsed.getURI().toString();
    }
}
