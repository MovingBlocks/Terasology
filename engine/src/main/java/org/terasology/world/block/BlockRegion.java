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
import org.joml.Matrix4fc;
import org.joml.Rectanglei;
import org.joml.RoundingMode;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;

public class BlockRegion extends AABBi {

    public BlockRegion() {
    }

    public BlockRegion(AABBi source) {
        super(source);
    }

    public BlockRegion(Vector3ic min, Vector3ic max) {
        super(min, max);
    }

    public BlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        super(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public BlockRegion unionBlock(EntityRef blockRef, BlockRegion dest) {
        BlockComponent component = blockRef.getComponent(BlockComponent.class);
        if (component != null) {
            return this.unionBlock(component.position.x(), component.position.y(), component.position.z(), dest);
        }
        return dest;
    }

    public Vector3i getMin(Vector3i dest) {
        return dest.set(minX, minY, minZ);
    }


    public Vector3i getMax(Vector3i dest) {
        return dest.set(maxX, maxY, maxZ);
    }

    @Override
    public BlockRegion setMin(Vector3ic min) {
        return (BlockRegion) super.setMin(min);
    }

    @Override
    public BlockRegion setMax(Vector3ic max) {
        return (BlockRegion) super.setMax(max);
    }

    @Override
    public BlockRegion setMax(int maxX, int maxY, int maxZ) {
        return (BlockRegion) super.setMax(maxX, maxY, maxZ);
    }

    @Override
    public BlockRegion setMin(int minX, int minY, int minZ) {
        return (BlockRegion) super.setMin(minX, minY, minZ);
    }

    @Override
    public BlockRegion union(AABBi other) {
        return (BlockRegion) super.union(other);
    }

    @Override
    public BlockRegion union(Vector3ic p) {
        return (BlockRegion) super.union(p);
    }

    public BlockRegion union(Vector3ic p, BlockRegion dest) {
        return (BlockRegion) super.union(p, dest);
    }

    public BlockRegion union(int x, int y, int z, BlockRegion dest) {
        return (BlockRegion) super.union(x, y, z, dest);
    }

    public BlockRegion union(BlockRegion other) {
        return (BlockRegion) super.union(other);
    }

    public BlockRegion union(AABBi other, BlockRegion dest) {
        return (BlockRegion) super.union(other, dest);
    }

    @Override
    public BlockRegion correctBounds() {
        return (BlockRegion) super.correctBounds();
    }

    @Override
    public BlockRegion union(int x, int y, int z) {
        return (BlockRegion) super.union(x, y, z);
    }

    public BlockRegion setSize(int x, int y, int z) {
        this.maxX = this.minX + x;
        this.maxY = this.minY + y;
        this.maxZ = this.minZ + z;
        return this;
    }

    public BlockRegion setSize(Vector3ic size) {
        return setSize(size.x(), size.y(), size.z());
    }

    public Vector3i getSize(Vector3i dest) {
        return dest.set(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ);
    }

    public int getSizeX() {
        return this.maxX - this.minX;
    }

    public int getSizeY() {
        return this.maxY - this.minY;
    }

    public int getSizeZ() {
        return this.maxZ - this.minZ;
    }

    public BlockRegion unionBlock(Vector3ic p) {
        return unionBlock(p.x(), p.y(), p.z(), this);
    }

    public BlockRegion unionBlock(Vector3ic p, BlockRegion dest) {
        return unionBlock(p.x(), p.y(), p.z(), dest);
    }

    @Override
    public BlockRegion translate(int x, int y, int z) {
        return (BlockRegion) super.translate(x, y, z);
    }

    public BlockRegion translate(Vector3ic xyz, BlockRegion dest) {
        return (BlockRegion) super.translate(xyz, dest);
    }

    @Override
    public BlockRegion translate(Vector3ic xyz) {
        return (BlockRegion) super.translate(xyz);
    }

    @Override
    public BlockRegion transform(Matrix4fc m) {
        return (BlockRegion) super.transform(m);
    }

    public BlockRegion transform(Matrix4fc m, BlockRegion dest) {
        return (BlockRegion) super.transform(m, dest);
    }

    public BlockRegion unionBlock(int x, int y, int z, BlockRegion dest) {
        // a block is (x,y,z) and (x + 1, y + 1, z + 1)
        dest.minX = this.minX < x ? this.minX : x;
        dest.minY = this.minY < y ? this.minY : y;
        dest.minZ = this.minZ < z ? this.minZ : z;
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

    public boolean isValid() {
        return this.minX < this.maxX && this.minY < this.maxY && this.minZ < this.maxZ;
    }

    private BlockRegion validate() {
        if (!isValid()) {
            minX = Integer.MAX_VALUE;
            minY = Integer.MAX_VALUE;
            minZ = Integer.MAX_VALUE;

            maxX = Integer.MIN_VALUE;
            maxY = Integer.MIN_VALUE;
            maxZ = Integer.MIN_VALUE;
        }
        return this;
    }

    public BlockRegion intersection(BlockRegion other, BlockRegion dest) {
        dest.minX = Math.max(minX, other.minX);
        dest.minY = Math.max(minY, other.minY);
        dest.minZ = Math.max(minZ, other.minZ);

        dest.maxX = Math.min(maxX, other.maxX);
        dest.maxY = Math.min(maxY, other.maxY);
        dest.maxZ = Math.min(maxZ, other.maxZ);
        return dest;
    }

    public BlockRegion inflate(int extentX, int extentY, int extentZ, BlockRegion dest) {
        dest.minX = this.minX - extentX;
        dest.minY = this.minY - extentY;
        dest.minZ = this.minZ - extentZ;

        dest.maxX = this.maxX + extentX;
        dest.maxY = this.maxY + extentY;
        dest.maxZ = this.maxZ + extentZ;
        return dest;
    }

    public BlockRegion inflate(float extentX, float extentY, float extentZ, BlockRegion dest) {
        dest.minX = Math.roundUsing(this.minX - extentX, RoundingMode.FLOOR);
        dest.minY = Math.roundUsing(this.minY - extentY, RoundingMode.FLOOR);
        dest.minZ = Math.roundUsing(this.minZ - extentZ, RoundingMode.FLOOR);

        dest.maxX = Math.roundUsing(this.maxX + extentX, RoundingMode.CEILING);
        dest.maxY = Math.roundUsing(this.maxY + extentY, RoundingMode.CEILING);
        dest.maxZ = Math.roundUsing(this.maxZ + extentZ, RoundingMode.CEILING);
        return dest;
    }
}
