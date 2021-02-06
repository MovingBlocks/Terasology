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

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.Component;
import org.terasology.math.JomlUtil;
import org.terasology.network.Replicate;

/**
 * Used for entities representing a block in the world
 */
public final class BlockComponent implements Component {
    @Replicate
    Vector3i position = new Vector3i();
    @Replicate
    public Block block;

    public BlockComponent() {
    }
    /**
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #BlockComponent(Block, Vector3ic)}.
     */
    @Deprecated
    public BlockComponent(Block block, org.terasology.math.geom.Vector3i pos) {
        this(block, JomlUtil.from(pos));
    }

    public BlockComponent(Block block, Vector3ic pos) {
        this.block = block;
        this.position.set(pos);
    }

    /**
     * Get an immutable view on the current position.
     *
     * Note: the vector may change when the position on this component is updated. If the position information is to be
     * stored you should use {@link #getPosition(Vector3i)} instead.
     */
    public Vector3ic getPosition() {
        return position;
    }

    /**
     * get the position
     *
     * @param dest will hold the result
     * @return dest
     */
    public Vector3i getPosition(Vector3i dest) {
        dest.set(position);
        return dest;
    }

    public void setPosition(Vector3ic pos) {
        position.set(pos);
    }

    /**
     * set the position of the {@link BlockComponent}
     *
     * @param pos position to set
     */
    public void setPosition(org.terasology.math.geom.Vector3i pos) {
        position.set(JomlUtil.from(pos));
    }
}
