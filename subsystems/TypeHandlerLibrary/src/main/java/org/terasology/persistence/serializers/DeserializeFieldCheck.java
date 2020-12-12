// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers;

import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;

/**
 * Interface for checks as whether a component should be deserialized
 */
@FunctionalInterface
public interface DeserializeFieldCheck {

    boolean shouldDeserialize(ClassMetadata<?, ?> classMetadata, FieldMetadata<?, ?> fieldMetadata);

    /**
     * Null implementation, returns true for all fields
     */
     final class NullCheck implements DeserializeFieldCheck {

        private static final NullCheck INSTANCE = new NullCheck();

        private NullCheck() {
        }

        public static NullCheck newInstance() {
            return INSTANCE;
        }

        @Override
        public boolean shouldDeserialize(ClassMetadata<?, ?> classMetadata, FieldMetadata<?, ?> fieldMetadata) {
            return true;
        }
    }

}
