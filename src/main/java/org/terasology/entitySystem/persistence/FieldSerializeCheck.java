package org.terasology.entitySystem.persistence;

import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.protobuf.EntityData;

/**
 * Interface for providing serializers with a method to check whether a given field should be serialized.
 * @author Immortius
 */
public interface FieldSerializeCheck<T> {

    /**
     * @param field The field to check
     * @param object The object it belongs to
     * @return Whether the field should be serialized
     */
    boolean shouldSerializeField(FieldMetadata field, T object);

    boolean shouldDeserializeField(FieldMetadata fieldInfo);

    /**
     * Null implementation, returns true for all fields
     */
    public static class NullCheck<T> implements FieldSerializeCheck<T> {

        private static NullCheck instance = new NullCheck();

        @SuppressWarnings("unchecked")
        public static <T> NullCheck<T> newInstance() {
            return instance;
        }

        private NullCheck() {
        }

        @Override
        public boolean shouldSerializeField(FieldMetadata field, T object) {
            return true;
        }

        @Override
        public boolean shouldDeserializeField(FieldMetadata fieldInfo) {
            return true;
        }
    }
}
