package org.terasology.entitySystem.persistence;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.metadata.ComponentMetadata;

/**
 * Interface for checks as whether a component should be serialized
 * @author Immortius
 */
public interface ComponentSerializeCheck {

    boolean serialize(ComponentMetadata<? extends Component> metadata);

    public static class NullCheck implements ComponentSerializeCheck {
        private static NullCheck instance = new NullCheck();

        public static ComponentSerializeCheck create() {
            return instance;
        }

        private NullCheck() {
        }

        @Override
        public boolean serialize(ComponentMetadata<? extends Component> metadata) {
            return metadata.getType() != EntityInfoComponent.class;
        }
    }
}
