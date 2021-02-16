// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world;

import org.joml.Vector3ic;
import org.terasology.world.block.Block;

/**
 */
public interface WorldChangeListener {

    void onBlockChanged(Vector3ic pos, Block newBlock, Block originalBlock);

    void onExtraDataChanged(int i, Vector3ic pos, int newData, int oldData);
}
