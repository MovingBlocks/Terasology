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

package org.terasology.persistence.serializers;

import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.ReplicatedFieldMetadata;

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
    public static final class NullCheck<T> implements FieldSerializeCheck<T> {

        private static final NullCheck INSTANCE = new NullCheck();

        private NullCheck() {
        }

        @SuppressWarnings("unchecked")
        public static <T> NullCheck<T> newInstance() {
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
