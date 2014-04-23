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
package org.terasology.world.generation2.facets;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.iterator.TIterator;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public interface Boolean3DIterator extends TIterator {
    Vector3i currentPosition();

    boolean next();

    void setLast(boolean newValue);
}
