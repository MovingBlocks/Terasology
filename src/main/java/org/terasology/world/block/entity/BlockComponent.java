/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.world.block.entity;

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
    private Vector3i position = new Vector3i();

    // Does this block component exist only for excavation (and should be removed when back at full heath)
    public boolean temporary = false;

    public BlockComponent() {
    }

    public BlockComponent(Tuple3i pos, boolean temporary) {
        this.position.set(pos);
        this.temporary = temporary;
    }

    public Vector3i getPosition() {
        return position;
    }

    public void setPosition(Tuple3i pos) {
        position.set(pos);
    }
}
