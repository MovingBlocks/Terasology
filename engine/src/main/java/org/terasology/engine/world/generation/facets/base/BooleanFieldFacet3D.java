// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 */
public interface BooleanFieldFacet3D extends WorldFacet3D {

    boolean get(int x, int y, int z);

    boolean get(Vector3i pos);

    boolean getWorld(int x, int y, int z);

    boolean getWorld(Vector3i pos);

    void set(int x, int y, int z, boolean value);

    void set(Vector3i pos, boolean value);

    void setWorld(int x, int y, int z, boolean value);

    void setWorld(Vector3i pos, boolean value);
}
