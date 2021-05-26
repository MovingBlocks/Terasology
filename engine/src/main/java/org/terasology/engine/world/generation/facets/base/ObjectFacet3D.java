// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector3ic;
import org.terasology.engine.world.generation.WorldFacet3D;

public interface ObjectFacet3D<T> extends WorldFacet3D {

    T get(int x, int y, int z);

    T get(Vector3ic pos);

    T getWorld(int x, int y, int z);

    T getWorld(Vector3ic pos);

    void set(int x, int y, int z, T value);

    void set(Vector3ic pos, T value);

    void setWorld(int x, int y, int z, T value);

    void setWorld(Vector3ic pos, T value);
}
