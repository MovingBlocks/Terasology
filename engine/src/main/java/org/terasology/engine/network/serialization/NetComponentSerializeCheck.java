// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network.serialization;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.metadata.ComponentMetadata;
import org.terasology.engine.persistence.serializers.ComponentSerializeCheck;

/**
 * Determines which components should be serialized over the network - only replicated components.
 *
 */
public class NetComponentSerializeCheck implements ComponentSerializeCheck {

    @Override
    public boolean serialize(ComponentMetadata<? extends Component> metadata) {
        return metadata.isReplicated();
    }
}
