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

import org.joml.Vector3i;
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
