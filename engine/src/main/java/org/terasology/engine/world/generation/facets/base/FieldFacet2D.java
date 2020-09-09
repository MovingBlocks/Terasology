// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.math.geom.BaseVector2i;
import org.terasology.engine.world.generation.WorldFacet2D;

/**
 */
public interface FieldFacet2D extends WorldFacet2D {

    float get(int x, int y);

    float get(BaseVector2i pos);

    float getWorld(int x, int y);

    float getWorld(BaseVector2i pos);

    void set(int x, int y, float value);

    void set(BaseVector2i pos, float value);

    void setWorld(int x, int y, float value);

    void setWorld(BaseVector2i pos, float value);
}
