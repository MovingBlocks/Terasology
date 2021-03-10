// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector2ic;
import org.terasology.engine.world.generation.WorldFacet2D;

/**
 */
public interface ObjectFacet2D<T> extends WorldFacet2D {

    T get(int x, int y);

    T get(Vector2ic pos);

    T getWorld(int x, int y);

    T getWorld(Vector2ic pos);

    void set(int x, int y, T value);

    void set(Vector2ic pos, T value);

    void setWorld(int x, int y, T value);

    void setWorld(Vector2ic pos, T value);
}
