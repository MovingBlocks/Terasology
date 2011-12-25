/*
*  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*/
package com.github.begla.blockmania.model.structures;

import com.github.begla.blockmania.logic.world.WorldProvider;
import com.github.begla.blockmania.model.blocks.Block;
import com.github.begla.blockmania.model.blocks.BlockManager;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Provides support for ray-box intersection tests.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class RayBlockIntersection {

    /**
     * Represents an intersection of a ray with the face of a block.
     *
     * @author Benjamin Glatzel <benjamin.glatzel@me.com>
     */
    public static class Intersection implements Comparable<Intersection> {

        private final double _d;
        private final double _t;
        private final Vector3d _rayOrigin, _intersectionPoint, _surfaceNormal, _rayDirection;
        private final BlockPosition _blockPosition;

        public Intersection(BlockPosition blockPosition, Vector3d normal, double d, double t, Vector3d rayOrigin, Vector3d rayDirection, Vector3d intersectionPoint) {
            this._d = d;
            this._t = t;
            this._rayOrigin = rayOrigin;
            this._rayDirection = rayDirection;
            this._intersectionPoint = intersectionPoint;
            this._surfaceNormal = normal;
            this._blockPosition = blockPosition;
        }

        public int compareTo(Intersection o) {
            if (o == null) {
                return 0;
            }

            double distance = _t;
            double distance2 = o._t;

            if (distance == distance2)
                return 0;

            return distance2 > distance ? -1 : 1;
        }

        public boolean equals(Object o) {
            if (o.getClass() != Intersection.class)
                return false;

            Intersection i = (Intersection) o;
            return this._blockPosition.equals(i.getBlockPosition());
        }

        Vector3d getSurfaceNormal() {
            return _surfaceNormal;
        }

        public BlockPosition calcAdjacentBlockPos() {
            Vector3d pos = getBlockPosition().toVector3d();
            pos.add(getSurfaceNormal());

            return new BlockPosition(pos);
        }

        public BlockPosition getBlockPosition() {
            return _blockPosition;
        }

        @Override
        public String toString() {
            return String.format("x: %.2d y: %.2d z: %.2d", _blockPosition.x, _blockPosition.y, _blockPosition.z);
        }
    }

    /**
     * Calculates the intersection of a given ray originating from a specified point with
     * a block. Returns a list of intersections ordered by the distance to the player.
     *
     * @param w            The world provider
     * @param x            The block's position on the x-axis
     * @param y            The block's position on the y-axis
     * @param z            The block's position on the z-axis
     * @param rayOrigin    The origin of the ray
     * @param rayDirection The direction of the ray
     */
    public static ArrayList<Intersection> executeIntersection(WorldProvider w, int x, int y, int z, Vector3d rayOrigin, Vector3d rayDirection) {
        /*
         * Ignore invisible blocks.
         */
        if (BlockManager.getInstance().getBlock(w.getBlock(x, y, z)).isInvisible()) {
            return null;
        }

        ArrayList<Intersection> result = new ArrayList<Intersection>();

        /*
         * Fetch all vertices of the specified block.
         */
        Vector3d[] vertices = Block.AABBForBlockAt(x, y, z).getVertices();
        BlockPosition blockPos = new BlockPosition(x, y, z);

        /*
         * Generate a new intersection for each side of the block.
         */

        // Front
        Intersection is = executeBlockFaceIntersection(blockPos, vertices[0], vertices[1], vertices[3], rayOrigin, rayDirection);
        if (is != null) {
            result.add(is);
        }

        // Back
        is = executeBlockFaceIntersection(blockPos, vertices[4], vertices[7], vertices[5], rayOrigin, rayDirection);
        if (is != null) {
            result.add(is);
        }

        // Left
        is = executeBlockFaceIntersection(blockPos, vertices[4], vertices[0], vertices[7], rayOrigin, rayDirection);
        if (is != null) {
            result.add(is);
        }

        // Right
        is = executeBlockFaceIntersection(blockPos, vertices[5], vertices[6], vertices[1], rayOrigin, rayDirection);
        if (is != null) {
            result.add(is);
        }

        // Top
        is = executeBlockFaceIntersection(blockPos, vertices[7], vertices[3], vertices[6], rayOrigin, rayDirection);
        if (is != null) {
            result.add(is);
        }

        // Bottom
        is = executeBlockFaceIntersection(blockPos, vertices[4], vertices[5], vertices[0], rayOrigin, rayDirection);
        if (is != null) {
            result.add(is);
        }

        // Sort the intersections by distance to the player
        Collections.sort(result);
        return result;
    }

    /**
     * Calculates an intersection with the face of a block defined by three points.
     *
     * @param blockPos The position of the block to intersect with
     * @param v0       Point 1
     * @param v1       Point 2
     * @param v2       Point 3
     * @param origin   Origin of the intersection ray
     * @param ray      Direction of the intersection ray
     * @return Ray-face-intersection
     */
    private static Intersection executeBlockFaceIntersection(BlockPosition blockPos, Vector3d v0, Vector3d v1, Vector3d v2, Vector3d origin, Vector3d ray) {

        // Calculate the plane to intersect with
        Vector3d a = new Vector3d();
        a.sub(v1, v0);
        Vector3d b = new Vector3d();
        b.sub(v2, v0);
        Vector3d norm = new Vector3d();
        norm.cross(a, b);

        double d = -(norm.x * v0.x + norm.y * v0.y + norm.z * v0.z);

        /**
         * Calculate the distance on the ray, where the intersection occurs.
         */
        double t = -(norm.x * origin.x + norm.y * origin.y + norm.z * origin.z + d) / ray.dot(norm);

        // No intersection possible
        if (t < 0)
            return null;

        /**
         * Calc. the point of intersection.
         */
        Vector3d intersectPoint = new Vector3d(ray.x * t, ray.y * t, ray.z * t);
        intersectPoint.add(intersectPoint, origin);

        /**
         * Check if the point lies on block's face.
         */
        if (intersectPoint.x >= v0.x && intersectPoint.x <= Math.max(v1.x, v2.x) && intersectPoint.y >= v0.y && intersectPoint.y <= Math.max(v1.y, v2.y) && intersectPoint.z >= v0.z && intersectPoint.z <= Math.max(v1.z, v2.z)) {
            return new Intersection(blockPos, norm, d, t, origin, ray, intersectPoint);
        }

        // Point of intersection was NOT lying on the block's face
        return null;
    }
}
