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

package org.terasology.math;

import com.bulletphysics.linearmath.AabbUtil2;
import com.bulletphysics.linearmath.Transform;
import com.google.common.base.Objects;
import gnu.trove.list.TFloatList;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3d;
import org.terasology.math.geom.Vector3f;

import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An axis-aligned bounding box. Provides basic support for inclusion
 * and intersection tests.
 *
 */
public final class AABB {

    private final Vector3f min;
    private final Vector3f max;

    private Vector3f[] vertices;

    private AABB(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

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
        Transform transform = new Transform(new Matrix4f(VecMath.to(rotation), VecMath.to(offset), scale));
        return transform(transform);
    }

    public AABB transform(Transform transform) {
        javax.vecmath.Vector3f newMin = new javax.vecmath.Vector3f();
        javax.vecmath.Vector3f newMax = new javax.vecmath.Vector3f();
        AabbUtil2.transformAabb(VecMath.to(min), VecMath.to(max), 0.01f, transform, newMin, newMax);
        return new AABB(VecMath.from(newMin), VecMath.from(newMax));
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

    public boolean intersectRectangle(Vector3f from, Vector3f direction) {
        Vector3f dirfrac = new Vector3f();

        dirfrac.x = 1.0f / direction.x;
        dirfrac.y = 1.0f / direction.y;
        dirfrac.z = 1.0f / direction.z;

        float t1 = (min.x - from.x) * dirfrac.x;
        float t2 = (max.x - from.x) * dirfrac.x;
        float t3 = (min.y - from.y) * dirfrac.y;
        float t4 = (max.y - from.y) * dirfrac.y;
        float t5 = (min.z - from.z) * dirfrac.z;
        float t6 = (max.z - from.z) * dirfrac.z;

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
