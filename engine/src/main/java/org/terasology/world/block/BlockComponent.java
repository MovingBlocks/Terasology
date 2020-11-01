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
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Replicate;

/**
 * Used for entities representing a block in the world
 */
public final class BlockComponent implements Component {
    @Replicate
    public Vector3i position = new Vector3i();
    @Replicate
    public Block block;

    public BlockComponent() {
    }
    /**
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #BlockComponent(Block, org.joml.Vector3i)}.
     */
    @Deprecated
    public BlockComponent(Block block, Vector3i pos) {
        this.block = block;
        this.position.set(pos);
    }

    public BlockComponent(Block block, org.joml.Vector3i pos) {
        this.block = block;
        this.position.set(JomlUtil.from(pos));
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is error prone (no defensive copy) and needlessly verbose.
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #getPosition(org.joml.Vector3i)}.
     */
    @Deprecated
    public Vector3i getPosition() {
        return position;
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is needlessly verbose.
     * @deprecated This is scheduled for removal in an upcoming version method will be replaced with JOML implementation
     *     {@link #setPosition(org.joml.Vector3i)}.
     */
    @Deprecated
    public void setPosition(Vector3i pos) {
        position.set(pos);
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is error prone (no defensive copy) and needlessly verbose.
     */
    @Deprecated
    public void setBlock(Block block) {
        this.block = block;
    }

    /**
     * @deprecated Deprecated on 21/Sep/2018, because it is error prone (no defensive copy) and needlessly verbose.
     */
    @Deprecated
    public Block getBlock() {
        return block;
    }

    /**
     * get the position
     *
     * @param dest will hold the result
     * @return dest
     */
    public org.joml.Vector3i getPosition(org.joml.Vector3i dest) {
        dest.set(JomlUtil.from(position));
        return dest;
    }

    /**
     * set the position of the {@link BlockComponent}
     *
     * @param pos position to set
     */
    public void setPosition(org.joml.Vector3i pos) {
        position.set(JomlUtil.from(pos));
    }
}
