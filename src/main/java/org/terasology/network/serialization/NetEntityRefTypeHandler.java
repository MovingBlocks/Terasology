package org.terasology.network.serialization;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.metadata.TypeHandler;
import org.terasology.network.NetEntityRef;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius
 */
public class NetEntityRefTypeHandler implements TypeHandler<EntityRef> {
    private NetworkSystem networkSystem;

    public NetEntityRefTypeHandler(NetworkSystem networkSystem) {
        this.networkSystem = networkSystem;
    }

    @Override
    public EntityData.Value serialize(EntityRef value) {
        NetworkComponent netComponent = value.getComponent(NetworkComponent.class);
        if (netComponent != null) {
            return EntityData.Value.newBuilder().addInteger(netComponent.networkId).build();
        }
        return null;
    }

    @Override
    public EntityRef deserialize(EntityData.Value value) {
        if (value.getIntegerCount() > 0) {
            return new NetEntityRef(value.getInteger(0), networkSystem);
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
            if (netComponent == null) {
                result.addInteger(0);
            } else {
                result.addInteger(netComponent.networkId);
            }
        }
        return result.build();
    }

    @Override
    public List<EntityRef> deserializeList(EntityData.Value value) {
        List<EntityRef> result = Lists.newArrayListWithCapacity(value.getIntegerCount());
        for (Integer item : value.getIntegerList()) {
            result.add(new NetEntityRef(item, networkSystem));
        }
        return result;
    }
}
