// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.serializers;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;

/**
 * Interface for checks as whether a component should be serialized
 */
@FunctionalInterface
public interface ComponentSerializeCheck {

    boolean serialize(ComponentMetadata<? extends Component> metadata);

     final class NullCheck implements ComponentSerializeCheck {
        private static final NullCheck INSTANCE = new NullCheck();

        private NullCheck() {
        }

        public static ComponentSerializeCheck create() {
            return INSTANCE;
        }

        @Override
        public boolean serialize(ComponentMetadata<? extends Component> metadata) {
            return metadata.getType() != EntityInfoComponent.class;
        }
    }
}
