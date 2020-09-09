// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world;

import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.block.Block;

/**
 */
public interface WorldChangeListener {

    void onBlockChanged(Vector3i pos, Block newBlock, Block originalBlock);
    
    void onExtraDataChanged(int i, Vector3i pos, int newData, int oldData);
}
