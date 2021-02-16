// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.generation.facets.base;

import org.joml.Vector3ic;
import org.terasology.world.generation.WorldFacet3D;

/**
 */
public interface BooleanFieldFacet3D extends WorldFacet3D {

    boolean get(int x, int y, int z);

    boolean get(Vector3ic pos);

    boolean getWorld(int x, int y, int z);

    boolean getWorld(Vector3ic pos);

    void set(int x, int y, int z, boolean value);

    void set(Vector3ic pos, boolean value);

    void setWorld(int x, int y, int z, boolean value);

    void setWorld(Vector3ic pos, boolean value);
}
