// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.serializers;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;

public class PersistenceComponentSerializeCheck implements ComponentSerializeCheck {
    @Override
    public boolean serialize(ComponentMetadata<? extends Component> metadata) {
        return metadata.isPersisted();
    }
}
