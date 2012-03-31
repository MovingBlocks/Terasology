package org.terasology.entitySystem.pojo.persistence.extension;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockGroupTypeHandler implements TypeHandler<BlockGroup> {

    public EntityData.Value serialize(BlockGroup value) {
        return EntityData.Value.newBuilder().addString(value.getTitle()).build();
    }

    public BlockGroup deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return BlockManager.getInstance().getBlockGroup(value.getString(0));
        }
        return null;
    }

    public EntityData.Value serialize(Iterable<BlockGroup> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (BlockGroup item : value) {
            result.addString(item.getTitle());
        }
        return result.build();
    }

    public List<BlockGroup> deserializeList(EntityData.Value value) {
        List<BlockGroup> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String item : value.getStringList()) {
            result.add(BlockManager.getInstance().getBlockGroup(item));
        }
        return result;
    }
}
