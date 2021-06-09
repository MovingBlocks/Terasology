// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world;

import org.joml.Vector3i;
import org.terasology.gestalt.entitysystem.component.Component;

public class RelevanceRegionComponent implements Component<RelevanceRegionComponent> {

    public Vector3i distance = new Vector3i(1, 1, 1);

    @Override
    public void copy(RelevanceRegionComponent other) {
        this.distance = new Vector3i(other.distance);
    }
}
