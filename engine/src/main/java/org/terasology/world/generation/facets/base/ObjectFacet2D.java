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

import org.terasology.math.geom.BaseVector2i;
import org.terasology.world.generation.WorldFacet2D;

/**
 */
public interface ObjectFacet2D<T> extends WorldFacet2D {

    T get(int x, int y);

    T get(BaseVector2i pos);

    T getWorld(int x, int y);

    T getWorld(BaseVector2i pos);

    void set(int x, int y, T value);

    void set(BaseVector2i pos, T value);

    void setWorld(int x, int y, T value);

    void setWorld(BaseVector2i pos, T value);
}
