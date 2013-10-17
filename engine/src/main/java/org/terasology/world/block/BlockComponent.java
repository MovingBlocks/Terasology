/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.block;

import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;
import org.terasology.network.Replicate;

import javax.vecmath.Tuple3i;

/**
 * Used for entities representing a block in the world
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class BlockComponent implements Component {
    @Replicate
    Vector3i position = new Vector3i();

    public BlockComponent() {
    }

    public BlockComponent(Tuple3i pos) {
        this.position.set(pos);
    }

    public Vector3i getPosition() {
        return position;
    }

    public void setPosition(Tuple3i pos) {
        position.set(pos);
    }
}
