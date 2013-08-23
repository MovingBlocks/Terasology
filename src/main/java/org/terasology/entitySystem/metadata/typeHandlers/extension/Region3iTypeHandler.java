/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.entitySystem.metadata.typeHandlers.extension;

import org.terasology.entitySystem.metadata.typeHandlers.SimpleTypeHandler;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;

/**
 * @author Immortius
 */
public class Region3iTypeHandler extends SimpleTypeHandler<Region3i> {
    private Vector3iTypeHandler vector3iTypeHandler;

    public Region3iTypeHandler(Vector3iTypeHandler vector3iTypeHandler) {
        this.vector3iTypeHandler = vector3iTypeHandler;
    }

    @Override
    public EntityData.Value serialize(Region3i value) {
        EntityData.NameValue min = EntityData.NameValue.newBuilder().setName("min").setValue(vector3iTypeHandler.serialize(value.min())).build();
        EntityData.NameValue size = EntityData.NameValue.newBuilder().setName("size").setValue(vector3iTypeHandler.serialize(value.size())).build();
        return EntityData.Value.newBuilder().addNameValue(min).addNameValue(size).build();
    }

    @Override
    public Region3i deserialize(EntityData.Value value) {
        Vector3i min = null;
        Vector3i size = null;
        for (EntityData.NameValue item : value.getNameValueList()) {
            if (!item.hasName() || !item.hasValue()) {
                continue;
            } else if (item.getName().equals("min")) {
                min = vector3iTypeHandler.deserialize(item.getValue());
            } else if (item.getName().equals("size")) {
                size = vector3iTypeHandler.deserialize(item.getValue());
            }
        }
        if (min != null && size != null) {
            return Region3i.createFromMinAndSize(min, size);
        }
        return null;
    }

    @Override
    public Region3i copy(Region3i value) {
        // Immutable, so need not copy.
        return value;
    }
}
