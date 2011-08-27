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
package com.github.begla.blockmania.player;

import com.github.begla.blockmania.utilities.VectorPool;
import org.lwjgl.util.vector.Vector3f;

/**
 * Represents an intersection of a ray with the face of a block.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class RayBoxIntersection implements Comparable<RayBoxIntersection> {

    /**
     *
     */
    public enum SIDE {

    }

    private final Vector3f v0;
    private final Vector3f v1;
    private final Vector3f v2;
    private final float d;
    private final float t;
    private final Vector3f origin;
    private final Vector3f ray;
    private final Vector3f intersectPoint;
    private final Vector3f blockPos;

    /**
     * @param blockPos
     * @param v0
     * @param v1
     * @param v2
     * @param d
     * @param t
     * @param origin
     * @param ray
     * @param intersectPoint
     */
    public RayBoxIntersection(Vector3f blockPos, Vector3f v0, Vector3f v1, Vector3f v2, float d, float t, Vector3f origin, Vector3f ray, Vector3f intersectPoint) {
        this.d = d;
        this.t = t;
        this.origin = origin;
        this.ray = ray;
        this.intersectPoint = intersectPoint;
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.blockPos = blockPos;
    }

    /**
     * @param o
     * @return
     */
    public int compareTo(RayBoxIntersection o) {
        return new Float(Math.abs(getT())).compareTo(Math.abs(o.getT()));
    }

    /**
     * @return the t
     */
    float getT() {
        return t;
    }

    /**
     * @return
     */
    Vector3f calcSurfaceNormal() {
        Vector3f a = Vector3f.sub(v1, v0, null);
        Vector3f b = Vector3f.sub(v2, v0, null);
        Vector3f norm = Vector3f.cross(a, b, null);

        VectorPool.putVector(a);
        VectorPool.putVector(b);

        return norm;
    }

    /**
     * @return
     */
    public Vector3f calcAdjacentBlockPos() {
        return Vector3f.add(getBlockPos(), calcSurfaceNormal(), null);
    }

    /**
     * @return the blockPos
     */
    public Vector3f getBlockPos() {
        return blockPos;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        return String.format("x: %.2f y: %.2f z: %.2f", blockPos.x, blockPos.y, blockPos.z);
    }
}
