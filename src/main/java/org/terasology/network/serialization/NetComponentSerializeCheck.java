package org.terasology.network.serialization;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.persistence.ComponentSerializeCheck;

/**
 * @author Immortius
 */
public class NetComponentSerializeCheck implements ComponentSerializeCheck {

    @Override
    public boolean serialize(ComponentMetadata<? extends Component> metadata) {
        return metadata.isReplicated() && metadata.getType() != EntityInfoComponent.class;
    }
}
