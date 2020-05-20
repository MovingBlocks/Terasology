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

import org.joml.AABBf;
import org.joml.AABBi;
import org.joml.Intersectionf;
import org.joml.LineSegmentf;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Planef;
import org.joml.Rayf;
import org.joml.RoundingMode;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.entitySystem.entity.EntityRef;

import java.util.Objects;

public class BlockRegion {

    public final AABBi aabb = new AABBi();

    public BlockRegion() {
    }

    public BlockRegion(BlockRegion source) {
        aabb.set(source.aabb);
    }

    public BlockRegion(AABBi source) {
        aabb.set(source);
    }

    public BlockRegion(Vector3ic min, Vector3ic max) {
        this(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    public BlockRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.setMin(minX, minY, minZ).setMax(maxX, maxY, maxZ);
    }

    /**
     * Get the minimum corner coordinate of the given component.
     *
     * @param component the component, within <code>[0..2]</code>
     * @return the maximum coordinate
     * @throws IllegalArgumentException if <code>component</code> is not within <code>[0..2]</code>
     */
    public int getMin(int component) throws IllegalArgumentException {
        return aabb.getMin(component);
    }

    public Vector3i getMin(Vector3i dest) {
        return dest.set(aabb.minX, aabb.minY, aabb.minZ);
    }

    public int getMax(int component) throws IllegalArgumentException {
        return aabb.getMax(component) - 1;
    }

    public Vector3i getMax(Vector3i dest) {
        return dest.set(aabb.maxX - 1, aabb.maxY - 1, aabb.maxZ - 1);
    }

    public int getMaxX() {
        return this.aabb.maxX - 1;
    }

    public int getMaxY() {
        return this.aabb.maxY - 1;
    }

    public int getMaxZ() {
        return this.aabb.maxZ - 1;
    }

    public int getMinX() {
        return this.aabb.minX;
    }

    public int getMinY() {
        return this.aabb.minY;
    }

    public int getMinZ() {
        return this.aabb.minZ;
    }


    public BlockRegion setMin(Vector3ic min) {
        this.aabb.setMin(min);
        return this;
    }

    public BlockRegion setMax(Vector3ic max) {
        this.setMax(max.x(), max.y(), max.z());
        return this;
    }

    public BlockRegion setMax(int maxX, int maxY, int maxZ) {
        this.aabb.setMax(maxX + 1, maxY + 1, maxZ + 1);
        return this;
    }

    public BlockRegion setMin(int minX, int minY, int minZ) {
        aabb.setMin(minX, minY, minZ);
        return this;
    }

    public BlockRegion union(EntityRef blockRef, BlockRegion dest) {
        BlockComponent component = blockRef.getComponent(BlockComponent.class);
        if (component != null) {
            return this.union(component.position.x(), component.position.y(), component.position.z(), dest);
        }
        return dest;
    }

    public BlockRegion union(Vector3ic p) {
        return union(p.x(), p.y(), p.z(), this);
    }

    public BlockRegion union(int x, int y, int z, BlockRegion dest) {
        // a block is (x,y,z) and (x + 1, y + 1, z + 1)
        dest.aabb.minX = this.aabb.minX < x ? this.aabb.minX : x;
        dest.aabb.minY = this.aabb.minY < y ? this.aabb.minY : y;
        dest.aabb.minZ = this.aabb.minZ < z ? this.aabb.minZ : z;
        dest.aabb.maxX = this.aabb.maxX > (x + 1) ? this.aabb.maxX : (x + 1);
        dest.aabb.maxY = this.aabb.maxY > (y + 1) ? this.aabb.maxY : (y + 1);
        dest.aabb.maxZ = this.aabb.maxZ > (z + 1) ? this.aabb.maxZ : (z + 1);
        return dest;
    }


    public BlockRegion union(Vector3ic p, BlockRegion dest) {
        return this.union(p.x(), p.y(), p.z(), dest);
    }

    public BlockRegion union(BlockRegion other) {
        return this.union(other.aabb);
    }

    public BlockRegion union(AABBi other, BlockRegion dest) {
        dest.union(other);
        return dest;
    }

    public BlockRegion union(AABBi other) {
        this.aabb.union(other);
        return this;
    }

    public BlockRegion correctBounds() {
        this.aabb.correctBounds();
        return this;
    }

    public BlockRegion setSize(int x, int y, int z) {
        this.aabb.maxX = this.aabb.minX + x;
        this.aabb.maxY = this.aabb.minY + y;
        this.aabb.maxZ = this.aabb.minZ + z;
        return this;
    }

    public BlockRegion setSize(Vector3ic size) {
        return setSize(size.x(), size.y(), size.z());
    }

    public Vector3i getSize(Vector3i dest) {
        return dest.set(this.aabb.maxX - this.aabb.minX, this.aabb.maxY - this.aabb.minY, this.aabb.maxZ - this.aabb.minZ);
    }

    public int getSizeX() {
        return this.aabb.maxX - this.aabb.minX;
    }

    public int getSizeY() {
        return this.aabb.maxY - this.aabb.minY;
    }

    public int getSizeZ() {
        return this.aabb.maxZ - this.aabb.minZ;
    }

    public BlockRegion translate(int x, int y, int z) {
        aabb.translate(x, y, z);
        return this;
    }

    public BlockRegion translate(Vector3ic xyz, BlockRegion dest) {
        aabb.translate(xyz, dest.aabb);
        return dest;
    }

    public BlockRegion translate(Vector3ic xyz) {
        this.aabb.translate(xyz);
        return this;
    }

    public BlockRegion transform(Matrix4fc m) {
        this.aabb.transform(m);
        return this;
    }

    public BlockRegion transform(Matrix4fc m, BlockRegion dest) {
        this.aabb.transform(m, dest.aabb);
        return dest;
    }

    public boolean containsBlock(Vector3ic pos) {
        return containsBlock(pos.x(), pos.y(), pos.z());
    }

    public boolean containsBlock(int x, int y, int z) {
        return x >= aabb.minX && y >= aabb.minY && z >= aabb.minZ && x < aabb.maxX && y < aabb.maxY && z < aabb.maxZ;
    }

    public boolean containsPoint(int x, int y, int z) {
        return this.aabb.containsPoint(x, y, z);
    }

    public boolean containsPoint(Vector3ic point) {
        return this.aabb.containsPoint(point);
    }

    public boolean intersectsPlane(Planef plane) {
        return this.aabb.intersectsPlane(plane);
    }

    public boolean intersectsPlane(float a, float b, float c, float d) {
        return this.aabb.intersectsPlane(a, b, c, d);
    }

    public boolean intersectsAABB(AABBi other) {
        return this.aabb.maxX >= other.minX && this.aabb.maxY >= other.minY && this.aabb.maxZ >= other.minZ &&
            this.aabb.minX <= other.maxX && this.aabb.minY <= other.maxY && this.aabb.minZ <= other.maxZ;
    }

    public boolean intersectsAABB(AABBf other) {
        return this.aabb.maxX >= other.minX && this.aabb.maxY >= other.minY && this.aabb.maxZ >= other.minZ &&
            this.aabb.minX <= other.maxX && this.aabb.minY <= other.maxY && this.aabb.minZ <= other.maxZ;
    }

    public boolean intersectsSphere(float centerX, float centerY, float centerZ, float radiusSquared) {
        return Intersectionf.testAabSphere(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, centerX, centerY, centerZ, radiusSquared);
    }

    public boolean intersectsSphere(Spheref sphere) {
        return Intersectionf.testAabSphere(aabb, sphere);
    }

    public boolean intersectsRay(float originX, float originY, float originZ, float dirX, float dirY, float dirZ) {
        return Intersectionf.testRayAab(originX, originY, originZ, dirX, dirY, dirZ, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
    }

    public boolean intersectsRay(Rayf ray) {
        return Intersectionf.testRayAab(ray, aabb);
    }

    public int intersectLineSegment(float p0X, float p0Y, float p0Z, float p1X, float p1Y, float p1Z, Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(p0X, p0Y, p0Z, p1X, p1Y, p1Z, aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, result);
    }

    public int intersectLineSegment(LineSegmentf lineSegment, Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(lineSegment, aabb, result);
    }

    public boolean isValid() {
        return aabb.isValid();
    }

    private BlockRegion validate() {
        if (!isValid()) {
            aabb.minX = Integer.MAX_VALUE;
            aabb.minY = Integer.MAX_VALUE;
            aabb.minZ = Integer.MAX_VALUE;

            aabb.maxX = Integer.MIN_VALUE;
            aabb.maxY = Integer.MIN_VALUE;
            aabb.maxZ = Integer.MIN_VALUE;
        }
        return this;
    }

    public BlockRegion intersection(BlockRegion other, BlockRegion dest) {
        this.aabb.intersection(other.aabb, dest.aabb);
        return dest;
    }

    public BlockRegion addExtents(int extentX, int extentY, int extentZ, BlockRegion dest) {
        dest.aabb.minX = this.aabb.minX - extentX;
        dest.aabb.minY = this.aabb.minY - extentY;
        dest.aabb.minZ = this.aabb.minZ - extentZ;

        dest.aabb.maxX = this.aabb.maxX + extentX;
        dest.aabb.maxY = this.aabb.maxY + extentY;
        dest.aabb.maxZ = this.aabb.maxZ + extentZ;
        return dest.validate();
    }

    public BlockRegion addExtents(float extentX, float extentY, float extentZ, BlockRegion dest) {
        dest.aabb.minX = Math.roundUsing(this.aabb.minX - extentX, RoundingMode.FLOOR);
        dest.aabb.minY = Math.roundUsing(this.aabb.minY - extentY, RoundingMode.FLOOR);
        dest.aabb.minZ = Math.roundUsing(this.aabb.minZ - extentZ, RoundingMode.FLOOR);

        dest.aabb.maxX = Math.roundUsing(this.aabb.maxX + extentX, RoundingMode.CEILING);
        dest.aabb.maxY = Math.roundUsing(this.aabb.maxY + extentY, RoundingMode.CEILING);
        dest.aabb.maxZ = Math.roundUsing(this.aabb.maxZ + extentZ, RoundingMode.CEILING);
        return dest.validate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockRegion region = (BlockRegion) o;
        return aabb.equals(region.aabb);
    }

    @Override
    public int hashCode() {
        return aabb.hashCode();
    }

    @Override
    public String toString() {
        return "(" + this.aabb.minX + " " + this.aabb.minY + " " + this.aabb.minZ + ") < " +
            "(" + (this.aabb.maxX - 1) + " " + (this.aabb.maxY - 1) + " " + (this.aabb.maxZ - 1) + ")";
    }
}
