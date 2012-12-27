package org.terasology.entitySystem.persistence;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.FieldMetadata;

/**
 * Interface for providing serializers with a method to check whether a given field should be serialized.
 * @author Immortius
 */
public interface FieldSerializeCheck {

    /**
     * @param field The field to check
     * @param component The component it belongs to
     * @return Whether the field should be serialized
     */
    boolean shouldSerializeField(FieldMetadata field, Component component);

    /**
     * Null implementation, returns true for all fields
     */
    public static class NullCheck implements FieldSerializeCheck {

        private static NullCheck instance = new NullCheck();

        public static NullCheck newInstance() {
            return instance;
        }

        private NullCheck() {
        }

        @Override
        public boolean shouldSerializeField(FieldMetadata field, Component component) {
            return true;
        }
    }
}
