// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.block;

import org.joml.AABBf;
import org.joml.Intersectionf;
import org.joml.LineSegmentf;
import org.joml.Math;
import org.joml.Matrix4fc;
import org.joml.Planef;
import org.joml.Rayf;
import org.joml.RoundingMode;
import org.joml.Spheref;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.Iterator;
import java.util.Optional;

public interface BlockRegionc extends Iterable<Vector3ic> {

    // -- ITERABLE -------------------------------------------------------------------------------------------------//
    Iterator<Vector3ic> iterator();

    // -- GETTERS & SETTERS -----------------------------------------------------------------------------------------//
    BlockRegion set(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockRegion dest);

    default BlockRegion set(Vector3ic min, Vector3ic max, BlockRegion dest) {
        return this.set(min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), dest);
    }

    default BlockRegion set(BlockRegion source, BlockRegion dest) {
        return this.set(source.minX(), source.minY(), source.minZ(), source.maxX(), source.maxY(), source.maxZ(), dest);
    }

    // -- min -------------------------------------------------------------------------------------------------------//
    int minX();

    BlockRegion minX(int x, BlockRegion dest);

    int minY();

    BlockRegion minY(int y, BlockRegion dest);

    int minZ();

    BlockRegion minZ(int z, BlockRegion dest);

    default Vector3i getMin(Vector3i dest) {
        return dest.set(minX(), minY(), minZ());
    }

    BlockRegion setMin(int x, int y, int z, BlockRegion dest);

    default BlockRegion setMin(Vector3ic min, BlockRegion dest) {
        return this.setMin(min.x(), min.y(), min.z(), dest);
    }

    BlockRegion addMin(int dx, int dy, int dz, BlockRegion dest);

    default BlockRegion addMin(Vector3ic dmin, BlockRegion dest) {
        return this.addMin(dmin.x(), dmin.y(), dmin.z(), dest);
    }

    // -- max -------------------------------------------------------------------------------------------------------//
    int maxX();

    BlockRegion maxX(int x, BlockRegion dest);

    int maxY();

    BlockRegion maxY(int y, BlockRegion dest);

    int maxZ();

    BlockRegion maxZ(int z, BlockRegion dest);

    default Vector3i getMax(Vector3i dest) {
        return dest.set(maxX(), maxY(), maxZ());
    }

    BlockRegion setMax(int x, int y, int z, BlockRegion dest);

    default BlockRegion setMax(Vector3ic max, BlockRegion dest) {
        return this.setMax(max.x(), max.y(), max.z(), dest);
    }

    BlockRegion addMax(int dx, int dy, int dz, BlockRegion dest);

    default BlockRegion addMax(Vector3ic dmax, BlockRegion dest) {
        return this.addMax(dmax.x(), dmax.y(), dmax.z(), dest);
    }

    // -- size ------------------------------------------------------------------------------------------------------//
    default int getSizeX() {
        return maxX() - minX() + 1;
    }

    default int getSizeY() {
        return maxY() - minY() + 1;
    }

    default int getSizeZ() {
        return maxZ() - minZ() + 1;
    }

    default Vector3i getSize(Vector3i dest) {
        return dest.set(getSizeX(), getSizeY(), getSizeZ());
    }

    BlockRegion setSize(int sizeX, int sizeY, int sizeZ, BlockRegion dest);

    default BlockRegion setSize(Vector3ic size, BlockRegion dest) {
        return this.setSize(size.x(), size.y(), size.z(), dest);
    }

    default int volume() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    // -- world -----------------------------------------------------------------------------------------------------//
    default AABBf getBounds(AABBf dest) {
        dest.minX = minX() - .5f;
        dest.minY = minY() - .5f;
        dest.minZ = minZ() - .5f;

        dest.maxX = maxX() + .5f;
        dest.maxY = maxY() + .5f;
        dest.maxZ = maxZ() + .5f;

        return dest;
    }

    default Vector3f center(Vector3f dest) {
        if (!this.isValid()) {
            return dest.set(Float.NaN);
        }
        return dest.set(
                (this.minX() - .5f) + ((this.maxX() - this.minX() + 1.0f) / 2.0f),
                (this.minY() - .5f) + ((this.maxY() - this.minY() + 1.0f) / 2.0f),
                (this.minZ() - .5f) + ((this.maxZ() - this.minZ() + 1.0f) / 2.0f)
        );
    }

    // -- IN-PLACE MUTATION -----------------------------------------------------------------------------------------//
    // -- union -----------------------------------------------------------------------------------------------------//
    BlockRegion union(int x, int y, int z, BlockRegion dest);

    default BlockRegion union(Vector3ic pos, BlockRegion dest) {
        return this.union(pos.x(), pos.y(), pos.z(), dest);
    }

    BlockRegion union(BlockRegion other, BlockRegion dest);

    // -- intersect -------------------------------------------------------------------------------------------------//
    Optional<BlockRegion> intersect(BlockRegion other, BlockRegion dest);      // this can leave the region in
    // invalid state!

    // ---------------------------------------------------------------------------------------------------------------//
    BlockRegion translate(int dx, int dy, int dz, BlockRegion dest);

    default BlockRegion translate(Vector3ic vec, BlockRegion dest) {
        return this.translate(vec.x(), vec.y(), vec.z(), dest);
    }

    // -- expand -----------------------------------------------------------------------------------------------------//
    BlockRegion expand(int dx, int dy, int dz, BlockRegion dest);

    default BlockRegion expand(Vector3ic vec, BlockRegion dest) {
        return this.expand(vec.x(), vec.y(), vec.z(), dest);
    }

    //TODO: why do we offer float variants here?
    default BlockRegion expand(float dx, float dy, float dz, BlockRegion dest) {
        return this.expand(
                Math.roundUsing(dx, RoundingMode.FLOOR),
                Math.roundUsing(dy, RoundingMode.FLOOR),
                Math.roundUsing(dz, RoundingMode.FLOOR),
                dest);
    }

    default BlockRegion expand(Vector3fc vec, BlockRegion dest) {
        return this.expand(vec.x(), vec.y(), vec.z(), dest);
    }

    // -- transform --------------------------------------------------------------------------------------------------//
    BlockRegion transform(Matrix4fc m, BlockRegion dest);

    // -- CHECKS -----------------------------------------------------------------------------------------------------//
    default boolean isValid() {
        return minX() <= maxX() && minY() <= maxY() && minZ() <= maxZ();
    }

    // -- contains ---------------------------------------------------------------------------------------------------//
    default boolean contains(int x, int y, int z) {
        return x >= minX() && y >= minY() && z >= minZ() && x <= maxX() && y <= maxY() && z <= maxZ();
    }

    default boolean contains(Vector3ic pos) {
        return this.contains(pos.x(), pos.y(), pos.z());
    }

    default boolean contains(float x, float y, float z) {
        return x >= (this.minX() - .5f)
                && y >= (this.minY() - .5f)
                && z >= (this.minZ() - .5f)
                && x <= (this.maxX() + .5f)
                && y <= (this.maxY() + .5f)
                && z <= (this.maxZ() + .5f);
    }

    default boolean contains(Vector3fc point) {
        return this.contains(point.x(), point.y(), point.z());
    }

    default boolean contains(BlockRegion other) {
        return this.contains(other.minX(), other.minY(), other.minZ())
                && this.contains(other.maxX(), other.maxY(), other.maxZ());
    }

    // -- intersects -------------------------------------------------------------------------------------------------//

    default boolean intersectsAABB(AABBf other) {
        return Intersectionf.testAabAab(
                this.minX() - .5f, this.minY() - .5f, this.minZ() - .5f,
                this.maxX() + .5f, this.maxY() + .5f, this.maxZ() + .5f,
                other.minX, other.minY, other.minZ,
                other.maxX, other.maxY, other.maxZ
        );
    }

    default boolean intersectsBlockRegion(BlockRegion other) {
        return this.maxX() >= other.minX() && this.maxY() >= other.minY() && this.maxZ() >= other.minZ()
                && this.minX() <= other.maxX() && this.minY() <= other.maxY() && this.minZ() <= other.maxZ();
    }

    default boolean intersectsPlane(float a, float b, float c, float d) {
        return Intersectionf.testAabPlane(
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f, a, b, c, d);
    }

    default boolean intersectsPlane(Planef plane) {
        return this.intersectsPlane(plane.a, plane.b, plane.c, plane.d);
    }

    default boolean intersectsRay(float originX, float originY, float originZ, float dirX, float dirY, float dirZ) {
        return Intersectionf.testRayAab(
                originX, originY, originZ, dirX, dirY, dirZ,
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f
        );
    }

    default boolean intersectsRay(Rayf ray) {
        return this.intersectsRay(ray.oX, ray.oY, ray.oZ, ray.dX, ray.dY, ray.dZ);
    }

    default boolean intersectsSphere(float centerX, float centerY, float centerZ, float radiusSquared) {
        return Intersectionf.testAabSphere(
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f,
                centerX,
                centerY,
                centerZ,
                radiusSquared
        );
    }

    default boolean intersectsSphere(Spheref sphere) {
        return this.intersectsSphere(sphere.x, sphere.y, sphere.z, sphere.r * sphere.r);
    }

    default int intersectLineSegment(float p0X, float p0Y, float p0Z, float p1X, float p1Y, float p1Z,
                                     Vector2f result) {
        return Intersectionf.intersectLineSegmentAab(p0X, p0Y, p0Z, p1X, p1Y, p1Z,
                minX() - .5f,
                minY() - .5f,
                minZ() - .5f,
                maxX() + .5f,
                maxY() + .5f,
                maxZ() + .5f, result);
    }

    default int intersectLineSegment(LineSegmentf lineSegment, Vector2f result) {
        return this.intersectLineSegment(lineSegment.aX, lineSegment.aY, lineSegment.aZ,
                lineSegment.bX, lineSegment.bY, lineSegment.bZ,
                result);
    }

    // ---------------------------------------------------------------------------------------------------------------//
    public boolean equals(Object obj);

    public int hashCode();

    public String toString();
}
