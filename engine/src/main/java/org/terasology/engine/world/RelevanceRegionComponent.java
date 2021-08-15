// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.Component;

public class RelevanceRegionComponent implements Component {

    public Vector3i distance = new Vector3i(1, 1, 1);
}
