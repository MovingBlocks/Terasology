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
import gnu.trove.list.TIntList;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;

import java.util.Collection;
import java.util.List;

/**
 * This type handler encodes EntityRef for network transferals. For normal entities, the Network Id of the entity is used.
 * For block entities the block position is used instead (this allows overriding simulated block entities).
 *
 */
public class NetEntityRefTypeHandler implements TypeHandler<EntityRef> {
    private NetworkSystemImpl networkSystem;
    private BlockEntityRegistry blockEntityRegistry;

    public NetEntityRefTypeHandler(NetworkSystemImpl networkSystem, BlockEntityRegistry blockEntityRegistry) {
        this.networkSystem = networkSystem;
        this.blockEntityRegistry = blockEntityRegistry;
    }

    @Override
    public PersistedData serialize(EntityRef value, SerializationContext context) {
        BlockComponent blockComponent = value.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            return context.create(blockComponent.position.x, blockComponent.position.y, blockComponent.position.z);
        }
        NetworkComponent netComponent = value.getComponent(NetworkComponent.class);
        if (netComponent != null) {
            return context.create(netComponent.getNetworkId());
        }
        return context.createNull();
    }

    @Override
    public EntityRef deserialize(PersistedData data, DeserializationContext context) {
        if (data.isArray()) {
            PersistedDataArray array = data.getAsArray();
            if (array.isNumberArray() && array.size() == 3) {
                TIntList items = data.getAsArray().getAsIntegerArray();
                Vector3i pos = new Vector3i(items.get(0), items.get(1), items.get(2));
                return blockEntityRegistry.getBlockEntityAt(pos);
            }
        }
        if (data.isNumber()) {
            return networkSystem.getEntity(data.getAsInteger());
        }
        return EntityRef.NULL;
    }

    @Override
    public PersistedData serializeCollection(Collection<EntityRef> value, SerializationContext context) {
        List<PersistedData> items = Lists.newArrayList();
        for (EntityRef ref : value) {
            BlockComponent blockComponent = ref.getComponent(BlockComponent.class);
            if (blockComponent != null) {
                Vector3i blockPos = blockComponent.getPosition();
                items.add(context.create(blockPos.x, blockPos.y, blockPos.z));
            } else {
                NetworkComponent netComponent = ref.getComponent(NetworkComponent.class);
                if (netComponent != null) {
                    items.add(context.create(netComponent.getNetworkId()));
                } else {
                    items.add(context.createNull());
                }
            }
        }
        return context.create(items);
    }

    @Override
    public List<EntityRef> deserializeCollection(PersistedData data, DeserializationContext context) {
        List<EntityRef> result = Lists.newArrayListWithCapacity(data.getAsArray().size());
        for (PersistedData item : data.getAsArray()) {
            result.add(deserialize(item, context));
        }
        return result;
    }

}
