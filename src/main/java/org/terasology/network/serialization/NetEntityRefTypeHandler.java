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

package org.terasology.network.serialization;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.network.internal.NetEntityRef;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;

import java.util.List;

/**
 * This type handler encodes EntityRef for network transferals. For normal entities, the Network Id of the entity is used.
 * For block entities the block position is used instead (this allows overriding simulated block entities).
 *
 * @author Immortius
 */
public class NetEntityRefTypeHandler implements TypeHandler<EntityRef> {
    private NetworkSystemImpl networkSystem;
    private BlockEntityRegistry blockEntityRegistry;

    public NetEntityRefTypeHandler(NetworkSystemImpl networkSystem, BlockEntityRegistry blockEntityRegistry) {
        this.networkSystem = networkSystem;
        this.blockEntityRegistry = blockEntityRegistry;
    }

    @Override
    public EntityData.Value serialize(EntityRef value) {
        BlockComponent blockComponent = value.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            Vector3i pos = blockComponent.getPosition();
            return EntityData.Value.newBuilder().addInteger(pos.x).addInteger(pos.y).addInteger(pos.z).build();
        }
        NetworkComponent netComponent = value.getComponent(NetworkComponent.class);
        if (netComponent != null) {
            return EntityData.Value.newBuilder().addInteger(netComponent.getNetworkId()).build();
        }
        return null;
    }

    @Override
    public EntityRef deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 2) {
            Vector3i pos = new Vector3i(value.getInteger(0), value.getInteger(1), value.getInteger(2));
            return blockEntityRegistry.getBlockEntityAt(pos);
        }
        if (value.getIntegerCount() > 0) {
            return new NetEntityRef(value.getInteger(0), networkSystem);
        }
        return EntityRef.NULL;
    }

    @Override
    public EntityData.Value serializeCollection(Iterable<EntityRef> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (EntityRef ref : value) {
            BlockComponent blockComponent = ref.getComponent(BlockComponent.class);
            if (blockComponent != null) {
                Vector3i blockPos = blockComponent.getPosition();
                result.addValue(EntityData.Value.newBuilder().addInteger(blockPos.x).addInteger(blockPos.y).addInteger(blockPos.z));
            } else {
                NetworkComponent netComponent = ref.getComponent(NetworkComponent.class);
                if (netComponent != null) {
                    result.addInteger(netComponent.getNetworkId());
                } else {
                    result.addInteger(0);
                }
            }
        }
        return result.build();
    }

    @Override
    public List<EntityRef> deserializeCollection(EntityData.Value value) {
        List<EntityRef> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
        for (Integer item : value.getIntegerList()) {
            if (item == 0) {
                result.add(EntityRef.NULL);
            } else {
                result.add(new NetEntityRef(item, networkSystem));
            }
        }
        for (EntityData.Value blockValue : value.getValueList()) {
            if (blockValue.getIntegerCount() > 2) {
                result.add(blockEntityRegistry.getBlockEntityAt(new Vector3i(blockValue.getInteger(0), blockValue.getInteger(1), blockValue.getInteger(2))));
            } else {
                result.add(EntityRef.NULL);
            }
        }
        return result;
    }
}
