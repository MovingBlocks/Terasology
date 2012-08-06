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
package org.terasology.math;

import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.google.common.base.Objects;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * An axis-aligned bounding box. Provides basic support for inclusion
 * and intersection tests.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AABB {

    private final Vector3f min;
    private final Vector3f max;

    private Vector3f[] vertices;

    public static AABB createMinMax(Vector3f min, Vector3f max) {
        return new AABB(new Vector3f(min), new Vector3f(max));
    }

    public static AABB createCenterExtent(Vector3f center, Vector3f extent) {
        Vector3f min = new Vector3f(center);
        min.sub(extent);
        Vector3f max = new Vector3f(center);
        max.add(extent);
        return new AABB(min, max);
    }

    public static AABB createEmpty() {
        return new AABB(new Vector3f(), new Vector3f());
    }

    /**
     * Creates a new AABB that encapsulates a set of AABBs
     *
     * @param others
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

    private AABB(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    public Vector3f getExtents() {
        Vector3f dimensions = new Vector3f(max);
        dimensions.sub(min);
        dimensions.scale(0.5f);
        return dimensions;
    }

    public Vector3f getCenter() {
        Vector3f dimensions = new Vector3f(max);
        dimensions.add(min);
        dimensions.scale(0.5f);
        return dimensions;
    }

    public Vector3f getMin() {
        return new Vector3f(min);
    }

    public Vector3f getMax() {
        return new Vector3f(max);
    }

    public AABB move(Vector3f offset) {
        Vector3f newMin = new Vector3f(min);
        newMin.add(offset);
        Vector3f newMax = new Vector3f(max);
        newMax.add(offset);
        return new AABB(newMin, newMax);
    }

    public AABB transform(Quat4f rotation, Vector3f offset, float scale) {
        Transform transform = new Transform(new Matrix4f(rotation, offset, scale));
        Vector3f newMin = new Vector3f();
        Vector3f newMax = new Vector3f();
        AabbUtil2.transformAabb(min, max, 0.01f, transform, newMin, newMax);
        return new AABB(newMin, newMax);
    }

    public AABB transform(Transform transform) {
        Vector3f newMin = new Vector3f();
        Vector3f newMax = new Vector3f();
        AabbUtil2.transformAabb(min, max, 0.01f, transform, newMin, newMax);
        return new AABB(newMin, newMax);
    }

    /**
     * Returns true if this AABB overlaps the given AABB.
     *
     * @param aabb2 The AABB to check for overlapping
     * @return True if overlapping
     */
    public boolean overlaps(AABB aabb2) {
        return !(max.x < aabb2.min.x || min.x > aabb2.max.x) &&
                !(max.y < aabb2.min.y || min.y > aabb2.max.y) &&
                !(max.z < aabb2.min.z || min.z > aabb2.max.z);
    }

    /**
     * Returns true if the AABB contains the given point.
     *
     * @param point The point to check for inclusion
     * @return True if containing
     */
    public boolean contains(Vector3d point) {
        return !(max.x < point.x || min.x > point.x) &&
                !(max.y < point.y || min.y > point.y) &&
                !(max.z < point.z || min.z > point.z);
    }

    /**
     * Returns true if the AABB contains the given point.
     *
     * @param point The point to check for inclusion
     * @return True if containing
     */
    public boolean contains(Vector3f point) {
        return !(max.x < point.x || min.x > point.x) &&
                !(max.y < point.y || min.y > point.y) &&
                !(max.z < point.z || min.z > point.z);
    }

    /**
     * Returns the closest point on the AABB to a given point.
     *
     * @param p The point
     * @return The point on the AABB closest to the given point
     */
    public Vector3f closestPointOnAABBToPoint(Vector3f p) {
        Vector3f r = new Vector3f(p);

        if (p.x < min.x) r.x = min.x;
        if (p.x > max.x) r.x = max.x;
        if (p.y < min.y) r.y = min.y;
        if (p.y > max.y) r.y = max.y;
        if (p.z < min.z) r.z = min.z;
        if (p.z > max.z) r.z = max.z;

        return r;
    }

    public Vector3f getFirstHitPlane(Vector3f direction, Vector3f pos, Vector3f dimensions, boolean testX, boolean testY, boolean testZ) {
        Vector3f hitNormal = new Vector3f();

        float dist = Float.POSITIVE_INFINITY;

        if (testX) {
            float distX;
            if (direction.x > 0) {
                distX = (min.x - pos.x - dimensions.x) / direction.x;
            } else {
                distX = (max.x - pos.x + dimensions.x) / direction.x;
            }
            if (distX >= 0 && distX < dist) {
                hitNormal.set(Math.copySign(1, direction.x), 0, 0);
            }
        }
        if (testY) {
            float distY;
            if (direction.y > 0) {
                distY = (min.y - pos.y - dimensions.y) / direction.y;
            } else {
                distY = (max.y - pos.y + dimensions.y) / direction.y;
            }
            if (distY >= 0 && distY < dist) {
                hitNormal.set(0, Math.copySign(1, direction.y), 0);
            }
        }
        if (testZ) {
            float distZ;
            if (direction.z > 0) {
                distZ = (min.z - pos.z - dimensions.z) / direction.z;
            } else {
                distZ = (max.z - pos.z + dimensions.z) / direction.z;
            }
            if (distZ >= 0 && distZ < dist) {
                hitNormal.set(0, 0, Math.copySign(1, direction.z));
            }
        }
        return hitNormal;

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
        ArrayList<Vector3f> normals = new ArrayList<Vector3f>();

        if (pointOnAABB.z == min.z && testZ) normals.add(new Vector3f(0, 0, -1));
        if (pointOnAABB.z == max.z && testZ) normals.add(new Vector3f(0, 0, 1));
        if (pointOnAABB.x == min.x && testX) normals.add(new Vector3f(-1, 0, 0));
        if (pointOnAABB.x == max.x && testX) normals.add(new Vector3f(1, 0, 0));
        if (pointOnAABB.y == min.y && testY) normals.add(new Vector3f(0, -1, 0));
        if (pointOnAABB.y == max.y && testY) normals.add(new Vector3f(0, 1, 0));

        float minDistance = Float.MAX_VALUE;
        Vector3f closestNormal = new Vector3f();

        for (int i = 0; i < normals.size(); i++) {
            Vector3f n = normals.get(i);

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
        if (normal.x == 1 && normal.y == 0 && normal.z == 0)
            return new Vector3f(max.x, getCenter().y, getCenter().z);
        if (normal.x == -1 && normal.y == 0 && normal.z == 0)
            return new Vector3f(min.x, getCenter().y, getCenter().z);
        if (normal.x == 0 && normal.y == 0 && normal.z == 1)
            return new Vector3f(getCenter().x, getCenter().y, max.z);
        if (normal.x == 0 && normal.y == 0 && normal.z == -1)
            return new Vector3f(getCenter().x, getCenter().y, min.z);
        if (normal.x == 0 && normal.y == 1 && normal.z == 0)
            return new Vector3f(getCenter().x, max.y, getCenter().z);
        if (normal.x == 0 && normal.y == -1 && normal.z == 0)
            return new Vector3f(getCenter().x, min.y, getCenter().z);

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

    public int hashCode() {
        return Objects.hashCode(min, max);
    }
}
