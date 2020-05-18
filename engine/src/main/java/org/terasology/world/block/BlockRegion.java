/*
 * Copyright 2020 MovingBlocks
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

import org.joml.AABBi;
import org.joml.Math;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;

public class BlockRegion extends AABBi {

    public BlockRegion unionBlock(EntityRef blockRef, BlockRegion dest) {
        BlockComponent component = blockRef.getComponent(BlockComponent.class);
        if (component != null) {
            return this.unionBlock(component.position.x, component.position.y, component.position.z, dest);
        }
        return dest;
    }

    public BlockRegion unionBlock(int x, int y, int z, BlockRegion dest) {
        dest.minX = this.minX < x ? this.minX : x;
        dest.minY = this.minY < y ? this.minY : y;
        dest.minZ = this.minZ < z ? this.minZ : z;
        // a block is (x,y,z) and (x + 1, y + 1, z + 1)
        dest.maxX = this.maxX > (x + 1) ? this.maxX : (x + 1);
        dest.maxY = this.maxY > (y + 1) ? this.maxY : (y + 1);
        dest.maxZ = this.maxZ > (z + 1) ? this.maxZ : (z + 1);
        return dest;
    }

    public boolean testBlock(Vector3ic pos) {
        return testBlock(pos.x(), pos.y(), pos.z());
    }

    public boolean testBlock(int x, int y, int z) {
        return x >= minX && y >= minY && z >= minZ && x < maxX && y < maxY && z < maxZ;
    }

    public BlockRegion centerExtents(Vector3ic center, Vector3ic extents) {
        this.minX = center.x() - extents.x();
        this.minY = center.y() - extents.y();
        this.minZ = center.z() - extents.z();
        this.maxX = center.x() + extents.x();
        this.maxY = center.y() + extents.y();
        this.maxZ = center.z() + extents.z();
        return this;
    }

    public BlockRegion centerExtents(Vector3fc center, Vector3fc extents) {
        this.minX = Math.roundUsing(center.x() - extents.x(), RoundingMode.FLOOR);
        this.minY = Math.roundUsing(center.y() - extents.y(), RoundingMode.FLOOR);
        this.minZ = Math.roundUsing(center.z() - extents.z(), RoundingMode.FLOOR);

        this.maxX = Math.roundUsing(center.x() + extents.x(), RoundingMode.CEILING);
        this.maxY = Math.roundUsing(center.y() + extents.y(), RoundingMode.CEILING);
        this.maxZ = Math.roundUsing(center.z() + extents.z(), RoundingMode.CEILING);
        return this;
    }
}
