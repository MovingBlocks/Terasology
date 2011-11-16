/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.datastructures;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;

/**
 * View frustum usable for frustum culling.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ViewFrustum {

    private final FrustumPlane[] _planes = new FrustumPlane[6];

    private final FloatBuffer _proj = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer _model = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer _clip = BufferUtils.createFloatBuffer(16);

    /**
     * Init. a new view frustum.
     */
    public ViewFrustum() {
        for (int i = 0; i < 6; i++)
            _planes[i] = new FrustumPlane();
    }

    /**
     * Updates the view frustum using the currently active modelview and projection matrices.
     */
    public void updateFrustum() {
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, _proj);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, _model);

        _clip.put(0, _model.get(0) * _proj.get(0) + _model.get(1) * _proj.get(4) + _model.get(2) * _proj.get(8) + _model.get(3) * _proj.get(12));
        _clip.put(1, _model.get(0) * _proj.get(1) + _model.get(1) * _proj.get(5) + _model.get(2) * _proj.get(9) + _model.get(3) * _proj.get(13));
        _clip.put(2, _model.get(0) * _proj.get(2) + _model.get(1) * _proj.get(6) + _model.get(2) * _proj.get(10) + _model.get(3) * _proj.get(14));
        _clip.put(3, _model.get(0) * _proj.get(3) + _model.get(1) * _proj.get(7) + _model.get(2) * _proj.get(11) + _model.get(3) * _proj.get(15));

        _clip.put(4, _model.get(4) * _proj.get(0) + _model.get(5) * _proj.get(4) + _model.get(6) * _proj.get(8) + _model.get(7) * _proj.get(12));
        _clip.put(5, _model.get(4) * _proj.get(1) + _model.get(5) * _proj.get(5) + _model.get(6) * _proj.get(9) + _model.get(7) * _proj.get(13));
        _clip.put(6, _model.get(4) * _proj.get(2) + _model.get(5) * _proj.get(6) + _model.get(6) * _proj.get(10) + _model.get(7) * _proj.get(14));
        _clip.put(7, _model.get(4) * _proj.get(3) + _model.get(5) * _proj.get(7) + _model.get(6) * _proj.get(11) + _model.get(7) * _proj.get(15));

        _clip.put(8, _model.get(8) * _proj.get(0) + _model.get(9) * _proj.get(4) + _model.get(10) * _proj.get(8) + _model.get(11) * _proj.get(12));
        _clip.put(9, _model.get(8) * _proj.get(1) + _model.get(9) * _proj.get(5) + _model.get(10) * _proj.get(9) + _model.get(11) * _proj.get(13));
        _clip.put(10, _model.get(8) * _proj.get(2) + _model.get(9) * _proj.get(6) + _model.get(10) * _proj.get(10) + _model.get(11) * _proj.get(14));
        _clip.put(11, _model.get(8) * _proj.get(3) + _model.get(9) * _proj.get(7) + _model.get(10) * _proj.get(11) + _model.get(11) * _proj.get(15));

        _clip.put(12, _model.get(12) * _proj.get(0) + _model.get(13) * _proj.get(4) + _model.get(14) * _proj.get(8) + _model.get(15) * _proj.get(12));
        _clip.put(13, _model.get(12) * _proj.get(1) + _model.get(13) * _proj.get(5) + _model.get(14) * _proj.get(9) + _model.get(15) * _proj.get(13));
        _clip.put(14, _model.get(12) * _proj.get(2) + _model.get(13) * _proj.get(6) + _model.get(14) * _proj.get(10) + _model.get(15) * _proj.get(14));
        _clip.put(15, _model.get(12) * _proj.get(3) + _model.get(13) * _proj.get(7) + _model.get(14) * _proj.get(11) + _model.get(15) * _proj.get(15));

        // RIGHT
        _planes[0].setA(_clip.get(3) - _clip.get(0));
        _planes[0].setB(_clip.get(7) - _clip.get(4));
        _planes[0].setC(_clip.get(11) - _clip.get(8));
        _planes[0].setD(_clip.get(15) - _clip.get(12));
        _planes[0].normalize();

        // LEFT
        _planes[1].setA(_clip.get(3) + _clip.get(0));
        _planes[1].setB(_clip.get(7) + _clip.get(4));
        _planes[1].setC(_clip.get(11) + _clip.get(8));
        _planes[1].setD(_clip.get(15) + _clip.get(12));
        _planes[1].normalize();

        // BOTTOM
        _planes[2].setA(_clip.get(3) + _clip.get(1));
        _planes[2].setB(_clip.get(7) + _clip.get(5));
        _planes[2].setC(_clip.get(11) + _clip.get(9));
        _planes[2].setD(_clip.get(15) + _clip.get(13));
        _planes[2].normalize();

        // TOP
        _planes[3].setA(_clip.get(3) - _clip.get(1));
        _planes[3].setB(_clip.get(7) - _clip.get(5));
        _planes[3].setC(_clip.get(11) - _clip.get(9));
        _planes[3].setD(_clip.get(15) - _clip.get(13));
        _planes[3].normalize();

        // FAR
        _planes[4].setA(_clip.get(3) - _clip.get(2));
        _planes[4].setB(_clip.get(7) - _clip.get(6));
        _planes[4].setC(_clip.get(11) - _clip.get(10));
        _planes[4].setD(_clip.get(15) - _clip.get(14));
        _planes[4].normalize();

        // NEAR
        _planes[5].setA(_clip.get(3) + _clip.get(2));
        _planes[5].setB(_clip.get(7) + _clip.get(6));
        _planes[5].setC(_clip.get(11) + _clip.get(10));
        _planes[5].setD(_clip.get(15) + _clip.get(14));
        _planes[5].normalize();
    }

    /**
     * Returns true if the given point intersects the view frustum.
     */
    public boolean intersects(double x, double y, double z) {
        for (int i = 0; i < 6; i++) {
            if (_planes[i].getA() * x + _planes[i].getB() * y + _planes[i].getC() * z + _planes[i].getD() <= 0) {
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

        for (int i = 0; i < 6; i++) {
            if (_planes[i].getA() * aabbVertices[0].x + _planes[i].getB() * aabbVertices[0].y + _planes[i].getC() * aabbVertices[0].z + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * aabbVertices[1].x + _planes[i].getB() * aabbVertices[1].y + _planes[i].getC() * aabbVertices[1].z + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * aabbVertices[2].x + _planes[i].getB() * aabbVertices[2].y + _planes[i].getC() * aabbVertices[2].z + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * aabbVertices[3].x + _planes[i].getB() * aabbVertices[3].y + _planes[i].getC() * aabbVertices[3].z + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * aabbVertices[4].x + _planes[i].getB() * aabbVertices[4].y + _planes[i].getC() * aabbVertices[4].z + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * aabbVertices[5].x + _planes[i].getB() * aabbVertices[5].y + _planes[i].getC() * aabbVertices[5].z + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * aabbVertices[6].x + _planes[i].getB() * aabbVertices[6].y + _planes[i].getC() * aabbVertices[6].z + _planes[i].getD() > 0)
                continue;
            if (_planes[i].getA() * aabbVertices[7].x + _planes[i].getB() * aabbVertices[7].y + _planes[i].getC() * aabbVertices[7].z + _planes[i].getD() > 0)
                continue;
            return false;
        }

        return true;
    }
}
