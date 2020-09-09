// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 */
public interface FieldFacet3D extends WorldFacet3D {

    float get(int x, int y, int z);

    float get(Vector3i pos);

    float getWorld(int x, int y, int z);

    float getWorld(Vector3i pos);

    void set(int x, int y, int z, float value);

    void set(Vector3i pos, float value);

    void setWorld(int x, int y, int z, float value);

    void setWorld(Vector3i pos, float value);
}
