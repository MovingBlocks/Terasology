/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.entitySystem.metadata.extension;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.engine.CoreRegistry;
import org.terasology.protobuf.EntityData;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockFamilyTypeHandler implements TypeHandler<BlockFamily> {

    private BlockManager blockManager = CoreRegistry.get(BlockManager.class);

    public EntityData.Value serialize(BlockFamily value) {
        return EntityData.Value.newBuilder().addString(value.getURI().toString()).build();
    }

    public BlockFamily deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return blockManager.getBlockFamily(value.getString(0));
        }
        return null;
    }

    public BlockFamily copy(BlockFamily value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<BlockFamily> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (BlockFamily item : value) {
            result.addString(item.getURI().toString());
        }
        return result.build();
    }

    public List<BlockFamily> deserializeList(EntityData.Value value) {
        List<BlockFamily> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String item : value.getStringList()) {
            result.add(blockManager.getBlockFamily(item));
        }
        return result;
    }
}
