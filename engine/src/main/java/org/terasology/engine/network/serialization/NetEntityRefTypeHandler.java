// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.serialization;

import gnu.trove.list.TIntList;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.internal.NetworkSystemImpl;
import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataArray;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandler;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.BlockComponent;

import java.util.Optional;

/**
 * This type handler encodes EntityRef for network transferals. For normal entities, the Network Id of the entity is used.
 * For block entities the block position is used instead (this allows overriding simulated block entities).
 *
 */
public class NetEntityRefTypeHandler extends TypeHandler<EntityRef> {
    private final NetworkSystemImpl networkSystem;
    private final BlockEntityRegistry blockEntityRegistry;

    public NetEntityRefTypeHandler(NetworkSystemImpl networkSystem, BlockEntityRegistry blockEntityRegistry) {
        this.networkSystem = networkSystem;
        this.blockEntityRegistry = blockEntityRegistry;
    }

    @Override
    public PersistedData serializeNonNull(EntityRef value, PersistedDataSerializer serializer) {
        BlockComponent blockComponent = value.getComponent(BlockComponent.class);
        if (blockComponent != null) {
            Vector3i pos = blockComponent.position;
            return serializer.serialize(pos.x, pos.y, pos.z);
        }
        NetworkComponent netComponent = value.getComponent(NetworkComponent.class);
        if (netComponent != null) {
            return serializer.serialize(netComponent.getNetworkId());
        }
        return serializer.serializeNull();
    }

    @Override
    public Optional<EntityRef> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray array = data.getAsArray();
            if (array.isNumberArray() && array.size() == 3) {
                TIntList items = data.getAsArray().getAsIntegerArray();
                Vector3i pos = new Vector3i(items.get(0), items.get(1), items.get(2));
                return Optional.ofNullable(blockEntityRegistry.getBlockEntityAt(pos));
            }
        }
        if (data.isNumber()) {
            return Optional.ofNullable(networkSystem.getEntity(data.getAsInteger()));
        }
        return Optional.ofNullable(EntityRef.NULL);
    }

}
