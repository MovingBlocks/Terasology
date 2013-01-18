package org.terasology.network.serialization;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.math.Vector3i;
import org.terasology.network.NetEntityRef;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.protobuf.EntityData;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BlockComponent;

import java.util.List;

/**
 * Encoding of EntityRef for network transferals. This uses with the network id of the entity from the NetworkComponent,
 * or the block position from the block component
 * @author Immortius
 */
public class NetEntityRefTypeHandler implements TypeHandler<EntityRef> {
    private NetworkSystem networkSystem;
    private BlockEntityRegistry blockEntityRegistry;

    public NetEntityRefTypeHandler(NetworkSystem networkSystem, BlockEntityRegistry blockEntityRegistry) {
        this.networkSystem = networkSystem;
        this.blockEntityRegistry = blockEntityRegistry;
    }

    @Override
    public EntityData.Value serialize(EntityRef value) {
        NetworkComponent netComponent = value.getComponent(NetworkComponent.class);
        if (netComponent != null) {
            return EntityData.Value.newBuilder().addInteger(netComponent.networkId).build();
        } else {
            BlockComponent blockComponent = value.getComponent(BlockComponent.class);
            if (blockComponent != null) {
                Vector3i pos = blockComponent.getPosition();
                return EntityData.Value.newBuilder().addValue(EntityData.Value.newBuilder().addInteger(pos.x).addInteger(pos.y).addInteger(pos.z)).build();
            }
        }
        return null;
    }

    @Override
    public EntityRef deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return new NetEntityRef(value.getInteger(0), networkSystem);
        } else if (value.getValueCount() > 0 && value.getValue(0).getIntegerCount() > 2) {
            EntityData.Value vec3iData = value.getValue(0);
            Vector3i pos = new Vector3i(vec3iData.getInteger(0), vec3iData.getInteger(1), vec3iData.getInteger(2));
            return blockEntityRegistry.getOrCreateBlockEntityAt(pos);
        }
        return EntityRef.NULL;
    }

    @Override
    public EntityRef copy(EntityRef value) {
        return value;
    }

    @Override
    public EntityData.Value serialize(Iterable<EntityRef> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (EntityRef ref : value) {
            NetworkComponent netComponent = ref.getComponent(NetworkComponent.class);
            if (netComponent != null) {
                result.addInteger(netComponent.networkId);
            } else {
                BlockComponent blockComponent = ref.getComponent(BlockComponent.class);
                if (blockComponent != null) {
                    Vector3i blockPos = blockComponent.getPosition();
                    result.addValue(EntityData.Value.newBuilder().addInteger(blockPos.x).addInteger(blockPos.y).addInteger(blockPos.z));
                } else {
                    result.addInteger(0);
                }
            }
        }
        return result.build();
    }

    @Override
    public List<EntityRef> deserializeList(EntityData.Value value) {
        List<EntityRef> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
        for (Integer item : value.getIntegerList()) {
            if (item.intValue() == 0) {
                result.add(EntityRef.NULL);
            } else {
                result.add(new NetEntityRef(item, networkSystem));
            }
        }
        for (EntityData.Value blockValue : value.getValueList()) {
            if (blockValue.getIntegerCount() > 2) {
                result.add(blockEntityRegistry.getOrCreateBlockEntityAt(new Vector3i(blockValue.getInteger(0), blockValue.getInteger(1), blockValue.getInteger(2))));
            } else {
                result.add(EntityRef.NULL);
            }
        }
        return result;
    }
}
