// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.engine.world.generation.WorldFacet2D;

/**
 */
public interface ObjectFacet2D<T> extends WorldFacet2D {

    T get(int x, int y);

    T get(BaseVector2i pos);

    T getWorld(int x, int y);

    T getWorld(BaseVector2i pos);

    void set(int x, int y, T value);

    void set(BaseVector2i pos, T value);

    void setWorld(int x, int y, T value);

    void setWorld(BaseVector2i pos, T value);
}
