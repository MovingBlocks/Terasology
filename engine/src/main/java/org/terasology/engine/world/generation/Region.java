// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.world.block.BlockRegion;

public interface Region {

    <T extends WorldFacet> T getFacet(Class<T> dataType);

    BlockRegion getRegion();
}
