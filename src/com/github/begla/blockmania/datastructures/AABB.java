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

import com.github.begla.blockmania.rendering.RenderableObject;
import javolution.util.FastList;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * An axis-aligned bounding box. Provides basic support for inclusion
 * and intersection tests.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AABB implements RenderableObject {

    private final Vector3f _position = new Vector3f();
    private final Vector3f _dimensions;
    private Vector3f[] _vertices;

    public AABB(Vector3f position, Vector3f dimensions) {
        setPosition(position);
        this._dimensions = dimensions;
    }

    public double minX() {
        return (getPosition().x - _dimensions.x);
    }

    public double minY() {
        return (getPosition().y - _dimensions.y);
    }

    public double minZ() {
        return (getPosition().z - _dimensions.z);
    }

    public double maxX() {
        return (getPosition().x + _dimensions.x);
    }

    public double maxY() {
        return (getPosition().y + _dimensions.y);
    }

    public double maxZ() {
        return (getPosition().z + _dimensions.z);
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
        Vector3f r = new Vector3f(p);

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

        if (pointOnAABB.z == minZ() && testZ) normals.add(new Vector3f(0, 0, -1));
        if (pointOnAABB.z == maxZ() && testZ) normals.add(new Vector3f(0, 0, 1));
        if (pointOnAABB.x == minX() && testX) normals.add(new Vector3f(-1, 0, 0));
        if (pointOnAABB.x == maxX() && testX) normals.add(new Vector3f(1, 0, 0));
        if (pointOnAABB.y == minY() && testY) normals.add(new Vector3f(0, -1, 0));
        if (pointOnAABB.y == maxY() && testY) normals.add(new Vector3f(0, 1, 0));

        double minDistance = Double.MAX_VALUE;
        Vector3f closestNormal = new Vector3f();

        for (FastList.Node<Vector3f> n = normals.head(), end = normals.tail(); (n = n.getNext()) != end; ) {
            double distance = Vector3f.sub(centerPointForNormal(n.getValue()), origin, null).length();

            if (distance < minDistance) {
                minDistance = distance;
                closestNormal = n.getValue();
            }
        }

        return closestNormal;
    }

    public Vector3f centerPointForNormal(Vector3f normal) {
        if (normal.x == 1 && normal.y == 0 && normal.z == 0)
            return new Vector3f(getPosition().x + _dimensions.x, getPosition().y, getPosition().z);
        if (normal.x == -1 && normal.y == 0 && normal.z == 0)
            return new Vector3f(getPosition().x - _dimensions.x, getPosition().y, getPosition().z);
        if (normal.x == 0 && normal.y == 0 && normal.z == 1)
            return new Vector3f(getPosition().x, getPosition().y, getPosition().z + _dimensions.z);
        if (normal.x == 0 && normal.y == 0 && normal.z == -1)
            return new Vector3f(getPosition().x, getPosition().y, getPosition().z - _dimensions.z);
        if (normal.x == 0 && normal.y == 1 && normal.z == 0)
            return new Vector3f(getPosition().x, getPosition().y + _dimensions.y, getPosition().z);
        if (normal.x == 0 && normal.y == -1 && normal.z == 0)
            return new Vector3f(getPosition().x, getPosition().y - _dimensions.y, getPosition().z);

        return new Vector3f();
    }

    public Vector3f[] getVertices() {

        if (_vertices == null) {
            _vertices = new Vector3f[8];

            // Front
            _vertices[0] = new Vector3f((float) minX(), (float) minY(), (float) maxZ());
            _vertices[1] = new Vector3f((float) maxX(), (float) minY(), (float) maxZ());
            _vertices[2] = new Vector3f((float) maxX(), (float) maxY(), (float) maxZ());
            _vertices[3] = new Vector3f((float) minX(), (float) maxY(), (float) maxZ());
            // Back
            _vertices[4] = new Vector3f((float) minX(), (float) minY(), (float) minZ());
            _vertices[5] = new Vector3f((float) maxX(), (float) minY(), (float) minZ());
            _vertices[6] = new Vector3f((float) maxX(), (float) maxY(), (float) minZ());
            _vertices[7] = new Vector3f((float) minX(), (float) maxY(), (float) minZ());
        }

        return _vertices;
    }

    public void render() {
        double offset = 0.01;

        glPushMatrix();
        glTranslatef(getPosition().x, getPosition().y, getPosition().z);

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

    public void update() {
        // Do nothing. Really.
    }

    public Vector3f getPosition() {
        return _position;
    }

    public void setPosition(Vector3f position) {
        _position.set(position);
    }
}
