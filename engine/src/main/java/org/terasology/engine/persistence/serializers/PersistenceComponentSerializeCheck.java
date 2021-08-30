// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.serializers;

import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.gestalt.entitysystem.component.Component;

public class PersistenceComponentSerializeCheck implements ComponentSerializeCheck {
    @Override
    public boolean serialize(ComponentMetadata<? extends Component> metadata) {
        return metadata.isPersisted();
    }
}
