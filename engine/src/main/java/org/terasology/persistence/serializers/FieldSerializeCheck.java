// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.serializers;

import org.terasology.engine.entitySystem.metadata.ReplicatedFieldMetadata;
import org.terasology.persistence.serializers.DeserializeFieldCheck;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;

/**
 * Interface for providing serializers with a method to check whether a given field should be serialized.
 *
 */
public interface FieldSerializeCheck<T> extends DeserializeFieldCheck {

    /**
     * @param field  The field to check
     * @param object The object it belongs to
     * @return Whether the field should be serialized
     */
    boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, T object);

    /**
     * @param field            The field to check
     * @param object           The object it belongs to
     * @param componentInitial In a network situation, whether the component is newly added or not
     * @return Whether the field should be serialized
     */
    boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, T object, boolean componentInitial);

    /**
     * Null implementation, returns true for all fields
     */
     final class NullCheck<T> implements FieldSerializeCheck<T> {

        private static final FieldSerializeCheck.NullCheck INSTANCE = new FieldSerializeCheck.NullCheck();

        private NullCheck() {
        }

        @SuppressWarnings("unchecked")
        public static <T> FieldSerializeCheck.NullCheck<T> newInstance() {
            return INSTANCE;
        }

        @Override
        public boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, T object) {
            return true;
        }

        @Override
        public boolean shouldSerializeField(ReplicatedFieldMetadata<?, ?> field, T object, boolean componentInitial) {
            return true;
        }

        @Override
        public boolean shouldDeserialize(ClassMetadata<?, ?> classMetadata, FieldMetadata<?, ?> fieldMetadata) {
            return true;
        }
    }
}
