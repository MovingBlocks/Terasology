package org.terasology.entitySystem.pojo.persistence.extension;

import org.terasology.entitySystem.pojo.persistence.AbstractTypeHandler;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Vector3fTypeHandler extends AbstractTypeHandler<Vector3f> {

    public EntityData.Value serialize(Vector3f value) {
        return EntityData.Value.newBuilder().addFloat(value.x).addFloat(value.y).addFloat(value.z).build();
    }

    public Vector3f deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 2) {
            return new Vector3f(value.getFloat(0), value.getFloat(1), value.getFloat(2));
        }
        return null;
    }
}
