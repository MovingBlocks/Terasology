package org.terasology.entitySystem.pojo.persistence.extension;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class BlockFamilyTypeHandler implements TypeHandler<BlockFamily> {

    public EntityData.Value serialize(BlockFamily value) {
        return EntityData.Value.newBuilder().addString(value.getTitle()).build();
    }

    public BlockFamily deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return BlockManager.getInstance().getBlockFamily(value.getString(0));
        }
        return null;
    }

    public EntityData.Value serialize(Iterable<BlockFamily> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (BlockFamily item : value) {
            result.addString(item.getTitle());
        }
        return result.build();
    }

    public BlockFamily copy(BlockFamily value) {
        return value;
    }

    public List<BlockFamily> deserializeList(EntityData.Value value) {
        List<BlockFamily> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String item : value.getStringList()) {
            result.add(BlockManager.getInstance().getBlockFamily(item));
        }
        return result;
    }
}
