// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector2ic;
import org.terasology.engine.world.generation.WorldFacet2D;

public interface FieldFacet2D extends WorldFacet2D {

    float get(int x, int y);

    float get(Vector2ic pos);

    float getWorld(int x, int y);

    float getWorld(Vector2ic pos);

    void set(int x, int y, float value);

    void set(Vector2ic pos, float value);

    void setWorld(int x, int y, float value);

    void setWorld(Vector2ic pos, float value);
}
