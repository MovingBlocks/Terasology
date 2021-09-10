// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector2ic;
import org.terasology.engine.world.generation.WorldFacet2D;

/**
 * A {@link WorldFacet2D}-based facet that provides boolean values
 * for rectangular area (see {@link #getWorldArea()}  and {@link #getRelativeArea()}.
 * Its entries are accessible through both relative and world coordinates (in blocks).
 * <br><br>
 * All methods throw {@link IllegalArgumentException} if coordinates are not inside the respective region.
 */
public interface BooleanFieldFacet2D extends WorldFacet2D {

    boolean get(int x, int y);

    boolean get(Vector2ic pos);

    boolean getWorld(int x, int y);

    boolean getWorld(Vector2ic pos);

    void set(int x, int y, boolean value);

    void set(Vector2ic pos, boolean value);

    void setWorld(int x, int y, boolean value);

    void setWorld(Vector2ic pos, boolean value);
}
