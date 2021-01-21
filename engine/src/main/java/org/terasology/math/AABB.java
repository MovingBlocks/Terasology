/*
 * Copyright 2018 MovingBlocks
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

package org.terasology.math;

import com.google.common.base.Objects;
import gnu.trove.list.TFloatList;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3d;
import org.terasology.math.geom.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An axis-aligned bounding box. Provides basic support for inclusion and intersection tests.
 * @deprecated this class is schedules for removal in an upcoming version
 *             Use the JOML implementation instead: {@link org.terasology.joml.geom.AABBf}
 **/
@Deprecated
public final class AABB {

    public static final float DEFAULT_MARGIN = 0.01f;
    public static final float HALVING_FACTOR = 0.5f;
    private final Vector3f min;
    private final Vector3f max;

    private Vector3f[] vertices;

    private AABB(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Creates a new AABB from the given minimum and maximum points, both inclusive.
     *
     * @param min The minimum of the AABB.
     * @param max The maximum of the AABB.
     * @return The created AABB.
     */
    public static AABB createMinMax(Vector3f min, Vector3f max) {
        return new AABB(new Vector3f(min), new Vector3f(max));
    }

    /**
     * Creates a new AABB with the given center and extents.
     *
     * @param center The center of the AABB.
     * @param extent The extent of the AABB.
     * @return The created AABB.
     */
    public static AABB createCenterExtent(Vector3f center, Vector3f extent) {
        Vector3f min = new Vector3f(center);
        min.sub(extent);
        Vector3f max = new Vector3f(center);
        max.add(extent);
        return new AABB(min, max);
    }

    /**
     * Creates an empty AABB that does not contain any points.
     *
     * @return The created AABB.
     */
    public static AABB createEmpty() {
        return new AABB(new Vector3f(), new Vector3f());
    }

    /**
     * Creates a new AABB that encapsulates a set of AABBs.
     *
     * @param others The other AABBs that'll define the extents of the new one.
     */
    public static AABB createEncompassing(Iterable<AABB> others) {
        Vector3f min;
        Vector3f max;
        Iterator<AABB> i = others.iterator();
        if (i.hasNext()) {
            AABB next = i.next();
            min = next.getMin();
            max = next.getMax();
        } else {
            return createEmpty();
        }
        while (i.hasNext()) {
            AABB next = i.next();
            Vector3f otherMin = next.getMin();
            Vector3f otherMax = next.getMax();
            Vector3fUtil.min(min, otherMin, min);
            Vector3fUtil.max(max, otherMax, max);
        }
        return new AABB(min, max);
    }

    /**
     * Creates a new AABB that contains the vertices as represented by a {@link TFloatList}.
     *
     * @param vertices The vertices to encompass. It is assumed that the X, Y, Z components of each
     *                 vertex are stored consecutively in the {@link TFloatList}.
     *
     *                 For the {@code i}th vertex in the list, the X, Y, and Z components
     *                 are stored at indices {@code 3 * i}, {@code 3 * i + 1}, and
     *                 {@code 3 * i + 2} respectively.
     *
     * @return The created AABB.
     */
    public static AABB createEncompasing(TFloatList vertices) {
        int vertexCount = vertices.size() / 3;
        if (vertexCount == 0) {
            return AABB.createEmpty();
        }

        Vector3f min = new Vector3f(vertices.get(0), vertices.get(1), vertices.get(2));
        Vector3f max = new Vector3f(vertices.get(0), vertices.get(1), vertices.get(2));
        for (int index = 1; index < vertexCount; ++index) {
            min.x = Math.min(min.x, vertices.get(3 * index));
            max.x = Math.max(max.x, vertices.get(3 * index));
            min.y = Math.min(min.y, vertices.get(3 * index + 1));
            max.y = Math.max(max.y, vertices.get(3 * index + 1));
            min.z = Math.min(min.z, vertices.get(3 * index + 2));
            max.z = Math.max(max.z, vertices.get(3 * index + 2));
        }
        return AABB.createMinMax(min, max);
    }

    /**
     * @return The distance from the center to the max node
     */
    public Vector3f getExtents() {
        Vector3f dimensions = new Vector3f(max);
        dimensions.sub(min);
        dimensions.scale(HALVING_FACTOR);
        return dimensions;
    }

    public Vector3f getCenter() {
        Vector3f dimensions = new Vector3f(max);
        dimensions.add(min);
        dimensions.scale(HALVING_FACTOR);
        return dimensions;
    }

    public Vector3f getMin() {
        return new Vector3f(min);
    }

    public Vector3f getMax() {
        return new Vector3f(max);
    }

    /**
     * Get a new AABB which have a new location base on the offset
     * @param offset The offset between the current AABB and the new AABB
     * @return the new AABB
     */
    public AABB move(Vector3f offset) {
        Vector3f newMin = new Vector3f(min);
        newMin.add(offset);
        Vector3f newMax = new Vector3f(max);
        newMax.add(offset);
        return new AABB(newMin, newMax);
    }

    /**
     * Transform this AABB into a new AABB with the given rotation, offset and scale.
     *
     * @param rotation The rotation from the current AABB to the new AABB.
     * @param offset The offset between the current AABB and the new AABB.
     * @param scale The scale of the new AABB with respect to the old AABB.
     * @return The new transformed AABB.
     */
    public AABB transform(Quat4f rotation, Vector3f offset, float scale) {
        Transform transform = new Transform(offset, rotation, scale);
        return transform(transform);
    }

    /**
     * Transform this AABB into a new AABB with the given rotation, offset and scale as represented by the {@link Transform}.
     *
     * @param transform The {@link Transform} representing the offset, rotation, and scale transformation from this AABB to the new AABB.
     * @return the new transformed AABB.
     */
    public AABB transform(Transform transform) {
        return transform(transform, DEFAULT_MARGIN);
    }

    public AABB transform(Transform transform, float margin) {
        // Adaptation of method AabbUtil2.transformAabb from the TeraBullet library.
        Vector3f localHalfExtents = new Vector3f();
        localHalfExtents.sub(max, min);
        localHalfExtents.mul(HALVING_FACTOR);

        localHalfExtents.x += margin;
        localHalfExtents.y += margin;
        localHalfExtents.z += margin;

        Vector3f localCenter = new Vector3f(max);
        localCenter.add(min);
        localCenter.mul(HALVING_FACTOR);

        Matrix3f absBasis = transform.getBasis();

        absBasis.m00 = Math.abs(absBasis.m00);
        absBasis.m01 = Math.abs(absBasis.m01);
        absBasis.m02 = Math.abs(absBasis.m02);
        absBasis.m10 = Math.abs(absBasis.m10);
        absBasis.m11 = Math.abs(absBasis.m11);
        absBasis.m12 = Math.abs(absBasis.m12);
        absBasis.m20 = Math.abs(absBasis.m20);
        absBasis.m21 = Math.abs(absBasis.m21);
        absBasis.m22 = Math.abs(absBasis.m22);

        Vector3f center = new Vector3f(localCenter);
        absBasis.transform(center);
        center.add(transform.origin);

        Vector3f extent = new Vector3f();

        extent.x = absBasis.getRow(0).dot(localHalfExtents);
        extent.y = absBasis.getRow(1).dot(localHalfExtents);
        extent.z = absBasis.getRow(2).dot(localHalfExtents);

        Vector3f worldMin = new Vector3f();
        worldMin.sub(center, extent);

        Vector3f worldMax = new Vector3f(center).add(extent);

        return AABB.createMinMax(worldMin, worldMax);
    }

    /**
     * Returns true if this AABB overlaps the given AABB.
     *
     * @param aabb2 The AABB to check for overlapping
     * @return True if overlapping
     */
    public boolean overlaps(AABB aabb2) {
        return !(max.x < aabb2.min.x || min.x > aabb2.max.x)
                && !(max.y < aabb2.min.y || min.y > aabb2.max.y)
                && !(max.z < aabb2.min.z || min.z > aabb2.max.z);
    }

    /**
     * Returns true if the AABB contains the given point.
     *
     * @param point The point to check for inclusion
     * @return True if containing
     */
    public boolean contains(Vector3d point) {
        return !(max.x < point.x || min.x >= point.x)
                && !(max.y < point.y || min.y >= point.y)
                && !(max.z < point.z || min.z >= point.z);
    }

    /**
     * Returns true if the AABB contains the given point.
     *
     * @param point The point to check for inclusion
     * @return True if containing
     */
    public boolean contains(Vector3f point) {
        return !(max.x < point.x || min.x >= point.x)
                && !(max.y < point.y || min.y >= point.y)
                && !(max.z < point.z || min.z >= point.z);
    }

    /**
     * Returns the closest point on the AABB to a given point.
     *
     * @param p The point
     * @return The point on the AABB closest to the given point
     */
    public Vector3f closestPointOnAABBToPoint(Vector3f p) {
        Vector3f r = new Vector3f(p);

        if (p.x < min.x) {
            r.x = min.x;
        }
        if (p.x > max.x) {
            r.x = max.x;
        }
        if (p.y < min.y) {
            r.y = min.y;
        }
        if (p.y > max.y) {
            r.y = max.y;
        }
        if (p.z < min.z) {
            r.z = min.z;
        }
        if (p.z > max.z) {
            r.z = max.z;
        }

        return r;
    }

    /**
     * Returns the normal of the plane closest to the given origin.
     *
     * @param pointOnAABB A point on the AABB
     * @param origin      The origin
     * @param testX       True if the x-axis should be tested
     * @param testY       True if the y-axis should be tested
     * @param testZ       True if the z-axis should be tested
     * @return The normal
     */
    public Vector3f normalForPlaneClosestToOrigin(Vector3f pointOnAABB, Vector3f origin, boolean testX, boolean testY, boolean testZ) {
        List<Vector3f> normals = new ArrayList<>();

        if (pointOnAABB.z == min.z && testZ) {
            normals.add(new Vector3f(0, 0, -1));
        }
        if (pointOnAABB.z == max.z && testZ) {
            normals.add(new Vector3f(0, 0, 1));
        }
        if (pointOnAABB.x == min.x && testX) {
            normals.add(new Vector3f(-1, 0, 0));
        }
        if (pointOnAABB.x == max.x && testX) {
            normals.add(new Vector3f(1, 0, 0));
        }
        if (pointOnAABB.y == min.y && testY) {
            normals.add(new Vector3f(0, -1, 0));
        }
        if (pointOnAABB.y == max.y && testY) {
            normals.add(new Vector3f(0, 1, 0));
        }

        float minDistance = Float.MAX_VALUE;
        Vector3f closestNormal = new Vector3f();

        for (Vector3f n : normals) {
            Vector3f diff = new Vector3f(centerPointForNormal(n));
            diff.sub(origin);

            float distance = diff.length();

            if (distance < minDistance) {
                minDistance = distance;
                closestNormal = n;
            }
        }

        return closestNormal;
    }

    /**
     * Returns the center point of one of the six planes for the given normal.
     *
     * @param normal The normal
     * @return The center point
     */
    public Vector3f centerPointForNormal(Vector3f normal) {
        if (normal.x == 1 && normal.y == 0 && normal.z == 0) {
            return new Vector3f(max.x, getCenter().y, getCenter().z);
        }
        if (normal.x == -1 && normal.y == 0 && normal.z == 0) {
            return new Vector3f(min.x, getCenter().y, getCenter().z);
        }
        if (normal.x == 0 && normal.y == 0 && normal.z == 1) {
            return new Vector3f(getCenter().x, getCenter().y, max.z);
        }
        if (normal.x == 0 && normal.y == 0 && normal.z == -1) {
            return new Vector3f(getCenter().x, getCenter().y, min.z);
        }
        if (normal.x == 0 && normal.y == 1 && normal.z == 0) {
            return new Vector3f(getCenter().x, max.y, getCenter().z);
        }
        if (normal.x == 0 && normal.y == -1 && normal.z == 0) {
            return new Vector3f(getCenter().x, min.y, getCenter().z);
        }

        return new Vector3f();
    }

    public float minX() {
        return min.x;
    }

    public float minY() {
        return min.y;
    }

    public float minZ() {
        return min.z;
    }

    public float maxX() {
        return max.x;
    }

    public float maxY() {
        return max.y;
    }

    public float maxZ() {
        return max.z;
    }

    /**
     * Returns the vertices of this AABB.
     *
     * @return The vertices
     */
    public Vector3f[] getVertices() {
        if (vertices == null) {
            vertices = new Vector3f[8];

            // Front
            vertices[0] = new Vector3f(min.x, min.y, max.z);
            vertices[1] = new Vector3f(max.x, min.y, max.z);
            vertices[2] = new Vector3f(max.x, max.y, max.z);
            vertices[3] = new Vector3f(min.x, max.y, max.z);
            // Back
            vertices[4] = new Vector3f(min.x, min.y, min.z);
            vertices[5] = new Vector3f(max.x, min.y, min.z);
            vertices[6] = new Vector3f(max.x, max.y, min.z);
            vertices[7] = new Vector3f(min.x, max.y, min.z);
        }

        return vertices;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof AABB) {
            AABB other = (AABB) obj;
            return Objects.equal(min, other.min) && Objects.equal(max, other.max);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(min, max);
    }

    /**
     * Checks whether a given ray intersects the AABB.
     *
     * @param from The origin of the ray.
     * @param direction The direction of the ray.
     * @return True if the ray intersects the AABB, else false.
     */
    public boolean intersectRectangle(Vector3f from, Vector3f direction) {
        Vector3f dirFrac = new Vector3f(
                1.0f / direction.x,
                1.0f / direction.y,
                1.0f / direction.z
        );

        float t1 = (min.x - from.x) * dirFrac.x;
        float t2 = (max.x - from.x) * dirFrac.x;
        float t3 = (min.y - from.y) * dirFrac.y;
        float t4 = (max.y - from.y) * dirFrac.y;
        float t5 = (min.z - from.z) * dirFrac.z;
        float t6 = (max.z - from.z) * dirFrac.z;

        float tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        float tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        if (tmax < 0) {
            return false;
        }

        if (tmin > tmax) {
            return false;
        }

        return true;
    }
}
