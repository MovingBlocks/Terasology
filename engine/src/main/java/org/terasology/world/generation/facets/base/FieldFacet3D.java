// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector3ic;
import org.terasology.engine.world.generation.WorldFacet3D;

/**
 */
public interface FieldFacet3D extends WorldFacet3D {

    float get(int x, int y, int z);

    float get(Vector3ic pos);

    float getWorld(int x, int y, int z);

    float getWorld(Vector3ic pos);

    void set(int x, int y, int z, float value);

    void set(Vector3ic pos, float value);

    void setWorld(int x, int y, int z, float value);

    void setWorld(Vector3ic pos, float value);
}
