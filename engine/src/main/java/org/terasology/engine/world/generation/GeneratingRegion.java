// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.math.Region3i;

/**
 */
public interface GeneratingRegion {

    Region3i getRegion();

    <T extends WorldFacet> T getRegionFacet(Class<T> type);

    <T extends WorldFacet> void setRegionFacet(Class<T> type, T facet);

    Border3D getBorderForFacet(Class<? extends WorldFacet> type);
}
