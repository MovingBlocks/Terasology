package org.terasology.entitySystem.pojo.persistence.extension;

import org.terasology.entitySystem.pojo.persistence.AbstractTypeHandler;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Vector3iTypeHandler extends AbstractTypeHandler<Vector3i> {

    public EntityData.Value serialize(Vector3i value) {
        return EntityData.Value.newBuilder().addInteger(value.x).addInteger(value.y).addInteger(value.z).build();
    }

    public Vector3i deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 2) {
            return new Vector3i(value.getInteger(0), value.getInteger(1), value.getInteger(2));
        }
        return null;
    }

    public Vector3i copy(Vector3i value) {
        if (value != null) {
            return new Vector3i(value);
        }
        return null;
    }
}
