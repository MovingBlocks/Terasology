/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeSerialization.typeHandlers.extension;

import org.terasology.asset.Assets;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.persistence.typeSerialization.typeHandlers.SimpleTypeHandler;
import org.terasology.protobuf.EntityData;

/**
 * @author synopia
 */
public class BehaviorTreeTypeHandler extends SimpleTypeHandler<BehaviorTree> {
    @Override
    public EntityData.Value serialize(BehaviorTree value) {
        return EntityData.Value.newBuilder().addString(value.getURI().toString()).build();
    }

    @Override
    public BehaviorTree deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return Assets.getBehaviorTree(value.getString(0));
        }
        return null;
    }
}
