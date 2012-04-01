package org.terasology.entitySystem.pojo.persistence.extension;

import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.PojoEntityRef;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.model.blocks.BlockGroup;
import org.terasology.model.blocks.management.BlockManager;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityRefTypeHandler implements TypeHandler<EntityRef> {
    private PojoEntityManager entityManager;

    public EntityRefTypeHandler(PojoEntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityData.Value serialize(EntityRef value) {
        if (value.exists()) {
            return EntityData.Value.newBuilder().addInteger(((PojoEntityRef)value).getId()).build();
        }
        return null;
    }

    public EntityRef deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return entityManager.getEntityRef(value.getInteger(0));
        }
        return EntityRef.NULL;
    }

    public EntityData.Value serialize(Iterable<EntityRef> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (EntityRef ref : value) {
            if (!ref.exists()) {
                result.addInteger(0);
            }
            else {
                result.addInteger(((PojoEntityRef)ref).getId());
            }
        }
        return result.build();
    }

    public List<EntityRef> deserializeList(EntityData.Value value) {
        List<EntityRef> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
        for (Integer item : value.getIntegerList()) {
            result.add(entityManager.getEntityRef(item));
        }
        return result;
    }
}
