// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.gestalt.entitysystem.component.Component;

public class RelevanceRegionComponent implements Component<RelevanceRegionComponent> {

    public Vector3i distance = new Vector3i(1, 1, 1);

    public RelevanceRegionComponent() { }

    public RelevanceRegionComponent(Vector3ic distance) {
        this.distance.set(distance);
    }

    @Override
    public void copyFrom(RelevanceRegionComponent other) {
        this.distance.set(other.distance);
    }
}
