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
import org.terasology.entitySystem.metadata.AbstractTypeHandler;
import org.terasology.engine.CoreRegistry;
import org.terasology.physics.CollisionGroup;
import org.terasology.physics.CollisionGroupManager;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius
 */
public class CollisionGroupTypeHandler extends AbstractTypeHandler<CollisionGroup> {

    private CollisionGroupManager groupManager;

    public CollisionGroupTypeHandler() {
        groupManager = CoreRegistry.get(CollisionGroupManager.class);
    }

    @Override
    public EntityData.Value serialize(CollisionGroup value) {
        return EntityData.Value.newBuilder().addString(value.getName()).build();
    }

    @Override
    public CollisionGroup deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return groupManager.getCollisionGroup(value.getString(0));
        }
        return null;
    }

    @Override
    public CollisionGroup copy(CollisionGroup value) {
        return value;
    }

    public EntityData.Value serialize(Iterable<CollisionGroup> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (CollisionGroup group : value) {
            if (group != null) {
                result.addString(group.getName());
            }
        }
        return result.build();
    }

    public List<CollisionGroup> deserializeList(EntityData.Value value) {
        List<CollisionGroup> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String name : value.getStringList()) {
            CollisionGroup group = groupManager.getCollisionGroup(name);
            if (group != null) {
                result.add(group);
            }
        }
        return result;
    }
}
