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

import com.github.begla.blockmania.rendering.VectorPool;
import com.github.begla.blockmania.world.RenderableObject;
import javolution.util.FastList;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AABB extends RenderableObject {

    private final Vector3f _dimensions;
    private Vector3f[] _vertices;

    public AABB(Vector3f position, Vector3f dimensions) {
        this._position = position;
        this._dimensions = dimensions;
    }

    public double minX() {
        return (_position.x - _dimensions.x);
    }

    public double minY() {
        return (_position.y - _dimensions.y);
    }

    public double minZ() {
        return (_position.z - _dimensions.z);
    }

    public double maxX() {
        return (_position.x + _dimensions.x);
    }

    public double maxY() {
        return (_position.y + _dimensions.y);
    }

    public double maxZ() {
        return (_position.z + _dimensions.z);
    }

    public boolean overlaps(AABB aabb2) {
        if (maxX() <= aabb2.minX() || minX() >= aabb2.maxX()) return false;
        if (maxY() <= aabb2.minY() || minY() >= aabb2.maxY()) return false;
        if (maxZ() <= aabb2.minZ() || minZ() >= aabb2.maxZ()) return false;
        return true;
    }

    public boolean contains(Vector3f point) {
        if (maxX() <= point.x || minX() >= point.x) return false;
        if (maxY() <= point.y || minY() >= point.y) return false;
        if (maxZ() <= point.z || minZ() >= point.z) return false;

        return true;
    }

    public Vector3f getDimensions() {
        return _dimensions;
    }

    public Vector3f closestPointOnAABBToPoint(Vector3f p) {
        Vector3f r = VectorPool.getVector(p);

        if (p.x < minX()) r.x = (float) minX();
        if (p.x > maxX()) r.x = (float) maxX();
        if (p.y < minY()) r.y = (float) minY();
        if (p.y > maxY()) r.y = (float) maxY();
        if (p.z < minZ()) r.z = (float) minZ();
        if (p.z > maxZ()) r.z = (float) maxZ();

        return r;
    }

    public Vector3f normalForPlaneClosestToOrigin(Vector3f pointOnAABB, Vector3f origin, boolean testX, boolean testY, boolean testZ) {
        FastList<Vector3f> normals = new FastList<Vector3f>();

        if (pointOnAABB.z == minZ() && testZ) normals.add(VectorPool.getVector(0, 0, -1));
        if (pointOnAABB.z == maxZ() && testZ) normals.add(VectorPool.getVector(0, 0, 1));
        if (pointOnAABB.x == minX() && testX) normals.add(VectorPool.getVector(-1, 0, 0));
        if (pointOnAABB.x == maxX() && testX) normals.add(VectorPool.getVector(1, 0, 0));
        if (pointOnAABB.y == minY() && testY) normals.add(VectorPool.getVector(0, -1, 0));
        if (pointOnAABB.y == maxY() && testY) normals.add(VectorPool.getVector(0, 1, 0));

        double minDistance = Double.MAX_VALUE;
        Vector3f closestNormal = VectorPool.getVector();

        for (Vector3f v : normals) {
            double distance = Vector3f.sub(centerPointForNormal(v), origin, null).length();

            if (distance < minDistance) {
                minDistance = distance;
                closestNormal = v;
            }
        }

        return closestNormal;
    }

    public Vector3f centerPointForNormal(Vector3f normal) {
        if (normal.x == 1 && normal.y == 0 && normal.z == 0)
            return VectorPool.getVector(_position.x + _dimensions.x, _position.y, _position.z);
        if (normal.x == -1 && normal.y == 0 && normal.z == 0)
            return VectorPool.getVector(_position.x - _dimensions.x, _position.y, _position.z);
        if (normal.x == 0 && normal.y == 0 && normal.z == 1)
            return VectorPool.getVector(_position.x, _position.y, _position.z + _dimensions.z);
        if (normal.x == 0 && normal.y == 0 && normal.z == -1)
            return VectorPool.getVector(_position.x, _position.y, _position.z - _dimensions.z);
        if (normal.x == 0 && normal.y == 1 && normal.z == 0)
            return VectorPool.getVector(_position.x, _position.y + _dimensions.y, _position.z);
        if (normal.x == 0 && normal.y == -1 && normal.z == 0)
            return VectorPool.getVector(_position.x, _position.y - _dimensions.y, _position.z);

        return VectorPool.getVector();
    }

    public Vector3f[] getVertices() {

        if (_vertices == null) {
            _vertices = new Vector3f[8];

            // Front
            _vertices[0] = VectorPool.getVector(minX(), minY(), maxZ());
            _vertices[1] = VectorPool.getVector(maxX(), minY(), maxZ());
            _vertices[2] = VectorPool.getVector(maxX(), maxY(), maxZ());
            _vertices[3] = VectorPool.getVector(minX(), maxY(), maxZ());
            // Back
            _vertices[4] = VectorPool.getVector(minX(), minY(), minZ());
            _vertices[5] = VectorPool.getVector(maxX(), minY(), minZ());
            _vertices[6] = VectorPool.getVector(maxX(), maxY(), minZ());
            _vertices[7] = VectorPool.getVector(minX(), maxY(), minZ());
        }

        return _vertices;
    }

    @Override
    public void render() {
        double offset = 0.01;

        glPushMatrix();
        glTranslatef(_position.x, _position.y, _position.z);

        glLineWidth(6f);
        glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

        // FRONT
        glBegin(GL_LINE_LOOP);
        glVertex3d(-_dimensions.x - offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3d(+_dimensions.x + offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3d(+_dimensions.x + offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glVertex3d(-_dimensions.x - offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glEnd();

        // BACK
        glBegin(GL_LINE_LOOP);
        glVertex3d(-_dimensions.x - offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3d(+_dimensions.x + offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3d(+_dimensions.x + offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3d(-_dimensions.x - offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glEnd();

        // TOP
        glBegin(GL_LINE_LOOP);
        glVertex3d(-_dimensions.x - offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3d(+_dimensions.x + offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3d(+_dimensions.x + offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3d(-_dimensions.x - offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glEnd();

        // BOTTOM
        glBegin(GL_LINE_LOOP);
        glVertex3d(-_dimensions.x - offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glVertex3d(+_dimensions.x + offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glVertex3d(+_dimensions.x + offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3d(-_dimensions.x - offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glEnd();

        // LEFT
        glBegin(GL_LINE_LOOP);
        glVertex3d(-_dimensions.x - offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3d(-_dimensions.x - offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3d(-_dimensions.x - offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3d(-_dimensions.x - offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glEnd();

        // RIGHT
        glBegin(GL_LINE_LOOP);
        glVertex3d(+_dimensions.x + offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3d(+_dimensions.x + offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3d(+_dimensions.x + offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3d(+_dimensions.x + offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glEnd();
        glPopMatrix();
    }

    @Override
    public void update() {
        // Do nothing. Really.
    }
}
