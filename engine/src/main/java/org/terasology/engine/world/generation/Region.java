// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation;

import org.terasology.engine.math.Region3i;

/**
 */
public interface Region {

    <T extends WorldFacet> T getFacet(Class<T> dataType);

    Region3i getRegion();
}
