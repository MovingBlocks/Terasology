/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.generation.facets.base;

import org.joml.Vector2ic;
import org.terasology.world.generation.WorldFacet2D;

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
