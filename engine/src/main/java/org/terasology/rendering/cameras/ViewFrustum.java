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
package org.terasology.rendering.cameras;

import org.lwjgl.BufferUtils;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.AABB;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;

import java.nio.FloatBuffer;

/**
 * View frustum usable for frustum culling.
 *
 */
public class ViewFrustum {

    private final FrustumPlane[] planes = new FrustumPlane[6];
    private final FloatBuffer clip = BufferUtils.createFloatBuffer(16);

    /**
     * Init. a new view frustum.
     */
    public ViewFrustum() {
        for (int i = 0; i < 6; i++) {
            planes[i] = new FrustumPlane();
        }
    }

    /**
     * Updates the view frustum using the currently active modelview and projection matrices.
     */
    public void updateFrustum(FloatBuffer modelViewMatrix, FloatBuffer projectionMatrix) {
        clip.put(0, modelViewMatrix.get(0) * projectionMatrix.get(0) + modelViewMatrix.get(1) * projectionMatrix.get(4)
                + modelViewMatrix.get(2) * projectionMatrix.get(8) + modelViewMatrix.get(3) * projectionMatrix.get(12));
        clip.put(1, modelViewMatrix.get(0) * projectionMatrix.get(1) + modelViewMatrix.get(1) * projectionMatrix.get(5)
                + modelViewMatrix.get(2) * projectionMatrix.get(9) + modelViewMatrix.get(3) * projectionMatrix.get(13));
        clip.put(2, modelViewMatrix.get(0) * projectionMatrix.get(2) + modelViewMatrix.get(1) * projectionMatrix.get(6)
                + modelViewMatrix.get(2) * projectionMatrix.get(10) + modelViewMatrix.get(3) * projectionMatrix.get(14));
        clip.put(3, modelViewMatrix.get(0) * projectionMatrix.get(3) + modelViewMatrix.get(1) * projectionMatrix.get(7)
                + modelViewMatrix.get(2) * projectionMatrix.get(11) + modelViewMatrix.get(3) * projectionMatrix.get(15));

        clip.put(4, modelViewMatrix.get(4) * projectionMatrix.get(0) + modelViewMatrix.get(5) * projectionMatrix.get(4)
                + modelViewMatrix.get(6) * projectionMatrix.get(8) + modelViewMatrix.get(7) * projectionMatrix.get(12));
        clip.put(5, modelViewMatrix.get(4) * projectionMatrix.get(1) + modelViewMatrix.get(5) * projectionMatrix.get(5)
                + modelViewMatrix.get(6) * projectionMatrix.get(9) + modelViewMatrix.get(7) * projectionMatrix.get(13));
        clip.put(6, modelViewMatrix.get(4) * projectionMatrix.get(2) + modelViewMatrix.get(5) * projectionMatrix.get(6)
                + modelViewMatrix.get(6) * projectionMatrix.get(10) + modelViewMatrix.get(7) * projectionMatrix.get(14));
        clip.put(7, modelViewMatrix.get(4) * projectionMatrix.get(3) + modelViewMatrix.get(5) * projectionMatrix.get(7)
                + modelViewMatrix.get(6) * projectionMatrix.get(11) + modelViewMatrix.get(7) * projectionMatrix.get(15));

        clip.put(8, modelViewMatrix.get(8) * projectionMatrix.get(0) + modelViewMatrix.get(9) * projectionMatrix.get(4)
                + modelViewMatrix.get(10) * projectionMatrix.get(8) + modelViewMatrix.get(11) * projectionMatrix.get(12));
        clip.put(9, modelViewMatrix.get(8) * projectionMatrix.get(1) + modelViewMatrix.get(9) * projectionMatrix.get(5)
                + modelViewMatrix.get(10) * projectionMatrix.get(9) + modelViewMatrix.get(11) * projectionMatrix.get(13));
        clip.put(10, modelViewMatrix.get(8) * projectionMatrix.get(2) + modelViewMatrix.get(9) * projectionMatrix.get(6)
                + modelViewMatrix.get(10) * projectionMatrix.get(10) + modelViewMatrix.get(11) * projectionMatrix.get(14));
        clip.put(11, modelViewMatrix.get(8) * projectionMatrix.get(3) + modelViewMatrix.get(9) * projectionMatrix.get(7)
                + modelViewMatrix.get(10) * projectionMatrix.get(11) + modelViewMatrix.get(11) * projectionMatrix.get(15));

        clip.put(12, modelViewMatrix.get(12) * projectionMatrix.get(0) + modelViewMatrix.get(13) * projectionMatrix.get(4)
                + modelViewMatrix.get(14) * projectionMatrix.get(8) + modelViewMatrix.get(15) * projectionMatrix.get(12));
        clip.put(13, modelViewMatrix.get(12) * projectionMatrix.get(1) + modelViewMatrix.get(13) * projectionMatrix.get(5)
                + modelViewMatrix.get(14) * projectionMatrix.get(9) + modelViewMatrix.get(15) * projectionMatrix.get(13));
        clip.put(14, modelViewMatrix.get(12) * projectionMatrix.get(2) + modelViewMatrix.get(13) * projectionMatrix.get(6)
                + modelViewMatrix.get(14) * projectionMatrix.get(10) + modelViewMatrix.get(15) * projectionMatrix.get(14));
        clip.put(15, modelViewMatrix.get(12) * projectionMatrix.get(3) + modelViewMatrix.get(13) * projectionMatrix.get(7)
                + modelViewMatrix.get(14) * projectionMatrix.get(11) + modelViewMatrix.get(15) * projectionMatrix.get(15));

        // RIGHT
        planes[0].setA(clip.get(3) - clip.get(0));
        planes[0].setB(clip.get(7) - clip.get(4));
        planes[0].setC(clip.get(11) - clip.get(8));
        planes[0].setD(clip.get(15) - clip.get(12));
        planes[0].normalize();

        // LEFT
        planes[1].setA(clip.get(3) + clip.get(0));
        planes[1].setB(clip.get(7) + clip.get(4));
        planes[1].setC(clip.get(11) + clip.get(8));
        planes[1].setD(clip.get(15) + clip.get(12));
        planes[1].normalize();

        // BOTTOM
        planes[2].setA(clip.get(3) + clip.get(1));
        planes[2].setB(clip.get(7) + clip.get(5));
        planes[2].setC(clip.get(11) + clip.get(9));
        planes[2].setD(clip.get(15) + clip.get(13));
        planes[2].normalize();

        // TOP
        planes[3].setA(clip.get(3) - clip.get(1));
        planes[3].setB(clip.get(7) - clip.get(5));
        planes[3].setC(clip.get(11) - clip.get(9));
        planes[3].setD(clip.get(15) - clip.get(13));
        planes[3].normalize();

        // FAR
        planes[4].setA(clip.get(3) - clip.get(2));
        planes[4].setB(clip.get(7) - clip.get(6));
        planes[4].setC(clip.get(11) - clip.get(10));
        planes[4].setD(clip.get(15) - clip.get(14));
        planes[4].normalize();

        // NEAR
        planes[5].setA(clip.get(3) + clip.get(2));
        planes[5].setB(clip.get(7) + clip.get(6));
        planes[5].setC(clip.get(11) + clip.get(10));
        planes[5].setD(clip.get(15) + clip.get(14));
        planes[5].normalize();
    }

    /**
     * Returns true if the given point intersects the view frustum.
     */
    public boolean intersects(double x, double y, double z) {
        for (int i = 0; i < 6; i++) {
            if (planes[i].getA() * x + planes[i].getB() * y + planes[i].getC() * z + planes[i].getD() <= 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if this view frustum intersects the given AABB.
     */
    public boolean intersects(AABB aabb) {

        Vector3f[] aabbVertices = aabb.getVertices();

        Vector3f cp = CoreRegistry.get(LocalPlayer.class).getViewPosition();

        for (int i = 0; i < 6; i++) {
            if (planes[i].getA() * (aabbVertices[0].x - cp.x) + planes[i].getB() * (aabbVertices[0].y - cp.y)
                    + planes[i].getC() * (aabbVertices[0].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            if (planes[i].getA() * (aabbVertices[1].x - cp.x) + planes[i].getB() * (aabbVertices[1].y - cp.y)
                    + planes[i].getC() * (aabbVertices[1].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            if (planes[i].getA() * (aabbVertices[2].x - cp.x) + planes[i].getB() * (aabbVertices[2].y - cp.y)
                    + planes[i].getC() * (aabbVertices[2].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            if (planes[i].getA() * (aabbVertices[3].x - cp.x) + planes[i].getB() * (aabbVertices[3].y - cp.y)
                    + planes[i].getC() * (aabbVertices[3].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            if (planes[i].getA() * (aabbVertices[4].x - cp.x) + planes[i].getB() * (aabbVertices[4].y - cp.y)
                    + planes[i].getC() * (aabbVertices[4].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            if (planes[i].getA() * (aabbVertices[5].x - cp.x) + planes[i].getB() * (aabbVertices[5].y - cp.y)
                    + planes[i].getC() * (aabbVertices[5].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            if (planes[i].getA() * (aabbVertices[6].x - cp.x) + planes[i].getB() * (aabbVertices[6].y - cp.y)
                    + planes[i].getC() * (aabbVertices[6].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            if (planes[i].getA() * (aabbVertices[7].x - cp.x) + planes[i].getB() * (aabbVertices[7].y - cp.y)
                    + planes[i].getC() * (aabbVertices[7].z - cp.z) + planes[i].getD() > 0) {
                continue;
            }
            return false;
        }

        return true;
    }

    /**
     * Returns true if the given sphere intersects the given AABB.
     */
    public boolean intersects(Vector3f position, float radius) {
        for (int i = 0; i < 6; i++) {
            if (planes[i].getA() * position.x + planes[i].getB() * position.y + planes[i].getC() * position.z + planes[i].getD() <= -radius) {
                return false;
            }
        }
        return true;
    }
}
