package com.github.begla.blockmania.player;

import com.github.begla.blockmania.blocks.Block;
import com.github.begla.blockmania.player.Intersection;
import com.github.begla.blockmania.utilities.VectorPool;
import com.github.begla.blockmania.world.World;
import javolution.util.FastList;
import org.lwjgl.util.vector.Vector3f;

import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: bg
 * Date: 27.08.11
 * Time: 02:21
 * To change this template use File | Settings | File Templates.
 */
public class RayBoxIntersectionHelper {
     /**
     * Returns the vertices of a block at the given position.
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    private static Vector3f[] verticesForBlockAt(int x, int y, int z) {
        Vector3f[] vertices = new Vector3f[8];

        vertices[0] = VectorPool.getVector(x - .5f, y - .5f, z - .5f);
        vertices[1] = VectorPool.getVector(x + .5f, y - .5f, z - .5f);
        vertices[2] = VectorPool.getVector(x + .5f, y + .5f, z - .5f);
        vertices[3] = VectorPool.getVector(x - .5f, y + .5f, z - .5f);

        vertices[4] = VectorPool.getVector(x - .5f, y - .5f, z + .5f);
        vertices[5] = VectorPool.getVector(x + .5f, y - .5f, z + .5f);
        vertices[6] = VectorPool.getVector(x + .5f, y + .5f, z + .5f);
        vertices[7] = VectorPool.getVector(x - .5f, y + .5f, z + .5f);

        return vertices;
    }

    /**
     * Calculates the intersection of a given ray originating from a specified point with
     * a block. Returns a list of intersections ordered by the distance to the player.
     *
     * @param x
     * @param y
     * @param z
     * @param origin
     * @param ray
     * @return Distance-ordered list of ray-face-intersections
     */
    public static FastList<Intersection> rayBlockIntersection(World w, int x, int y, int z, Vector3f origin, Vector3f ray) {
        /*
         * Ignore invisible blocks.
         */
        if (Block.getBlockForType(w.getBlock(x, y, z)).isBlockInvisible()) {
            return null;
        }

        FastList<Intersection> result = new FastList<Intersection>();

        /*
         * Fetch all vertices of the specified block.
         */
        Vector3f[] vertices = verticesForBlockAt(x, y, z);
        Vector3f blockPos = VectorPool.getVector(x, y, z);

        /*
         * Generate a new intersection for each side of the block.
         */

        // Front
        Intersection is = rayFaceIntersection(blockPos, vertices[0], vertices[3], vertices[2], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Back
        is = rayFaceIntersection(blockPos, vertices[4], vertices[5], vertices[6], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Left
        is = rayFaceIntersection(blockPos, vertices[0], vertices[4], vertices[7], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Right
        is = rayFaceIntersection(blockPos, vertices[1], vertices[2], vertices[6], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Top
        is = rayFaceIntersection(blockPos, vertices[3], vertices[7], vertices[6], origin, ray);
        if (is != null) {
            result.add(is);
        }

        // Bottom
        is = rayFaceIntersection(blockPos, vertices[0], vertices[1], vertices[5], origin, ray);
        if (is != null) {
            result.add(is);
        }

        /*
         * Sort the intersections by distance.
         */
        Collections.sort(result);
        return result;
    }

    /**
     * Calculates an intersection with the face of a block defined by 3 points.
     *
     * @param blockPos The position of the block to intersect with
     * @param v0       Point 1
     * @param v1       Point 2
     * @param v2       Point 3
     * @param origin   Origin of the intersection ray
     * @param ray      Direction of the intersection ray
     * @return Ray-face-intersection
     */
    private static Intersection rayFaceIntersection(Vector3f blockPos, Vector3f v0, Vector3f v1, Vector3f v2, Vector3f origin, Vector3f ray) {

        // Calculate the plane to intersect with
        Vector3f a = Vector3f.sub(v1, v0, null);
        Vector3f b = Vector3f.sub(v2, v0, null);
        Vector3f norm = Vector3f.cross(a, b, null);


        float d = -(norm.x * v0.x + norm.y * v0.y + norm.z * v0.z);

        /**
         * Calculate the distance on the ray, where the intersection occurs.
         */
        float t = -(norm.x * origin.x + norm.y * origin.y + norm.z * origin.z + d) / (Vector3f.dot(ray, norm));

        if (t < 0) {
            return null;
        }

        /**
         * Calc. the point of intersection.
         */
        Vector3f intersectPoint = VectorPool.getVector(ray.x * t, ray.y * t, ray.z * t);
        Vector3f.add(intersectPoint, origin, intersectPoint);

        if (intersectPoint.x >= v0.x && intersectPoint.x <= v2.x && intersectPoint.y >= v0.y && intersectPoint.y <= v2.y && intersectPoint.z >= v0.z && intersectPoint.z <= v2.z) {
            return new Intersection(blockPos, v0, v1, v2, d, t, origin, ray, intersectPoint);
        }

        return null;
    }
}
