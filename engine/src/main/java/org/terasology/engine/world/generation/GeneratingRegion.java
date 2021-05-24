// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.world.block.BlockRegion;

public interface GeneratingRegion {

    BlockRegion getRegion();

    <T extends WorldFacet> T getRegionFacet(Class<T> type);

    <T extends WorldFacet> void setRegionFacet(Class<T> type, T facet);

    Border3D getBorderForFacet(Class<? extends WorldFacet> type);
}
