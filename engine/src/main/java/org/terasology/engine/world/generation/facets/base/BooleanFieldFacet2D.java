// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.math.geom.Vector2i;
import org.terasology.engine.world.generation.WorldFacet2D;

/**
 * A {@link WorldFacet2D}-based facet that provides boolean values
 * for rectangular area (see {@link #getWorldRegion()} and {@link #getRelativeRegion()}).
 * Its entries are accessible through both relative and world coordinates (in blocks).
 * <br><br>
 * All methods throw {@link IllegalArgumentException} if coordinates are not inside the respective region.
 */
public interface BooleanFieldFacet2D extends WorldFacet2D {

    boolean get(int x, int y);

    boolean get(Vector2i pos);

    boolean getWorld(int x, int y);

    boolean getWorld(Vector2i pos);

    void set(int x, int y, boolean value);

    void set(Vector2i pos, boolean value);

    void setWorld(int x, int y, boolean value);

    void setWorld(Vector2i pos, boolean value);
}
