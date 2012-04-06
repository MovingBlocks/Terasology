package org.terasology.entitySystem.pojo.persistence.extension;

import org.terasology.entitySystem.pojo.persistence.AbstractTypeHandler;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Quat4f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class Quat4fTypeHandler extends AbstractTypeHandler<Quat4f> {

    public EntityData.Value serialize(Quat4f value) {
        return EntityData.Value.newBuilder().addFloat(value.x).addFloat(value.y).addFloat(value.z).addFloat(value.w).build();
    }

    public Quat4f deserialize(EntityData.Value value) {
        if (value.getFloatCount() > 3) {
            return new Quat4f(value.getFloat(0), value.getFloat(1), value.getFloat(2), value.getFloat(3));
        }
        return null;
    }
}
