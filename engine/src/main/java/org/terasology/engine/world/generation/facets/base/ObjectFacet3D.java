// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.math.geom.BaseVector3i;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 */
public interface ObjectFacet3D<T> extends WorldFacet3D {

    T get(int x, int y, int z);

    T get(BaseVector3i pos);

    T getWorld(int x, int y, int z);

    T getWorld(BaseVector3i pos);

    void set(int x, int y, int z, T value);

    void set(BaseVector3i pos, T value);

    void setWorld(int x, int y, int z, T value);

    void setWorld(BaseVector3i pos, T value);
}
