// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.terasology.engine.utilities.concurrency.Task;
import org.terasology.math.geom.Vector3i;

/**
 *
 */
public interface ChunkTask extends Task {

    Vector3i getPosition();

}
