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

import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.rendering.interfaces.RenderableObject;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3f;
import java.util.ArrayList;

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

    private int _displayListWire = -1;
    private int _displayListSolid = -1;

    /**
     * Creates a new AABB at the given position with the given dimensions.
     *
     * @param position   The position
     * @param dimensions The dimensions
     */
    public AABB(Vector3f position, Vector3f dimensions) {
        setPosition(position);
        this._dimensions = dimensions;
    }

    /**
     * Returns true if this AABB overlaps the given AABB.
     *
     * @param aabb2 The AABB to check for overlapping
     * @return True if overlapping
     */
    public boolean overlaps(AABB aabb2) {
        if (maxX() < aabb2.minX() || minX() > aabb2.maxX()) return false;
        if (maxY() < aabb2.minY() || minY() > aabb2.maxY()) return false;
        if (maxZ() < aabb2.minZ() || minZ() > aabb2.maxZ()) return false;
        return true;
    }

    /**
     * Returns true if the AABB contains the given point.
     *
     * @param point The point to check for inclusion
     * @return True if containing
     */
    public boolean contains(Vector3f point) {
        if (maxX() < point.x || minX() > point.x) return false;
        if (maxY() < point.y || minY() > point.y) return false;
        if (maxZ() < point.z || minZ() > point.z) return false;

        return true;
    }

    /**
     * Returns the closest point on the AABB to a given point.
     *
     * @param p The point
     * @return The point on the AABB closest to the given point
     */
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

    /**
     * Returns the normal of the plane closest to the given origin.
     *
     * @param pointOnAABB A point on the AABB
     * @param origin      The origin
     * @param testX       True if the x-axis should be tested
     * @param testY       True if the y-axis should be tested
     * @param testZ       True if the z-axis should be tested
     * @return
     */
    public Vector3f normalForPlaneClosestToOrigin(Vector3f pointOnAABB, Vector3f origin, boolean testX, boolean testY, boolean testZ) {
        ArrayList<Vector3f> normals = new ArrayList<Vector3f>();

        if (pointOnAABB.z == minZ() && testZ) normals.add(new Vector3f(0, 0, -1));
        if (pointOnAABB.z == maxZ() && testZ) normals.add(new Vector3f(0, 0, 1));
        if (pointOnAABB.x == minX() && testX) normals.add(new Vector3f(-1, 0, 0));
        if (pointOnAABB.x == maxX() && testX) normals.add(new Vector3f(1, 0, 0));
        if (pointOnAABB.y == minY() && testY) normals.add(new Vector3f(0, -1, 0));
        if (pointOnAABB.y == maxY() && testY) normals.add(new Vector3f(0, 1, 0));

        double minDistance = Double.MAX_VALUE;
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

    /**
     * Returns the vertices of this AABB.
     *
     * @return The vertices
     */
    public Vector3f[] getVertices() {
        if (_vertices == null) {
            Vector3f[] vertices = new Vector3f[8];

            // Front
            vertices[0] = new Vector3f((float) minX(), (float) minY(), (float) maxZ());
            vertices[1] = new Vector3f((float) maxX(), (float) minY(), (float) maxZ());
            vertices[2] = new Vector3f((float) maxX(), (float) maxY(), (float) maxZ());
            vertices[3] = new Vector3f((float) minX(), (float) maxY(), (float) maxZ());
            // Back
            vertices[4] = new Vector3f((float) minX(), (float) minY(), (float) minZ());
            vertices[5] = new Vector3f((float) maxX(), (float) minY(), (float) minZ());
            vertices[6] = new Vector3f((float) maxX(), (float) maxY(), (float) minZ());
            vertices[7] = new Vector3f((float) minX(), (float) maxY(), (float) minZ());

            _vertices = vertices;
        }

        return _vertices;
    }

    /**
     * Renders this AABB.
     * <p/>
     */
    public void render() {
        glPushMatrix();
        Vector3f rp = Blockmania.getInstance().getActiveWorldProvider().getRenderingReferencePoint();
        glTranslatef(getPosition().x - rp.x, getPosition().y - rp.y, getPosition().z - rp.z);

        if (_displayListWire == -1) {
            generateDisplayListWire();
        }

        glCallList(_displayListWire);

        glPopMatrix();
    }

    public void renderSolid() {
        glPushMatrix();
        Vector3f rp = Blockmania.getInstance().getActiveWorldProvider().getRenderingReferencePoint();
        glTranslatef(getPosition().x - rp.x, getPosition().y - rp.y, getPosition().z - rp.z);

        if (_displayListWire == -1) {
            generateDisplayListSolid();
        }

        glCallList(_displayListSolid);

        glPopMatrix();
    }

    private void generateDisplayListSolid() {

        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        _displayListSolid = glGenLists(1);

        glNewList(_displayListSolid, GL11.GL_COMPILE);
        glBegin(GL_QUADS);
        GL11.glVertex3f(-_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3f(_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3f(_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3f(-_dimensions.x, _dimensions.y, -_dimensions.z);

        GL11.glVertex3f(-_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3f(-_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3f(-_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3f(-_dimensions.x, _dimensions.y, -_dimensions.z);

        GL11.glVertex3f(-_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3f(_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3f(_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3f(-_dimensions.x, _dimensions.y, _dimensions.z);

        GL11.glVertex3f(_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3f(_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3f(_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3f(_dimensions.x, -_dimensions.y, -_dimensions.z);

        GL11.glVertex3f(-_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3f(_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3f(_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3f(-_dimensions.x, -_dimensions.y, -_dimensions.z);

        GL11.glVertex3f(-_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3f(_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3f(_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3f(-_dimensions.x, -_dimensions.y, _dimensions.z);
        glEnd();
        glEndList();

    }

    private void generateDisplayListWire() {
        double offset = 0.01;

        _displayListWire = glGenLists(1);

        glNewList(_displayListWire, GL11.GL_COMPILE);
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
        glEndList();
    }

    public void update() {
        // Do nothing. Really.
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

    public Vector3f getDimensions() {
        return _dimensions;
    }

    public Vector3f getPosition() {
        return _position;
    }

    public void setPosition(Vector3f position) {
        _position.set(position);
    }
}
