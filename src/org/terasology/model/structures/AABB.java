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
package org.terasology.model.structures;

import org.lwjgl.opengl.GL11;
import org.terasology.game.Terasology;

import javax.vecmath.Vector3d;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

/**
 * An axis-aligned bounding box. Provides basic support for inclusion
 * and intersection tests.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AABB {

    private final Vector3d _position = new Vector3d();
    private final Vector3d _dimensions;
    private Vector3d[] _vertices;

    private int _displayListWire = -1;
    private int _displayListSolid = -1;

    /**
     * Creates a new AABB at the given position with the given dimensions.
     *
     * @param position   The position
     * @param dimensions The dimensions
     */
    public AABB(Vector3d position, Vector3d dimensions) {
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
    public boolean contains(Vector3d point) {
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
    public Vector3d closestPointOnAABBToPoint(Vector3d p) {
        Vector3d r = new Vector3d(p);

        if (p.x < minX()) r.x = minX();
        if (p.x > maxX()) r.x = maxX();
        if (p.y < minY()) r.y = minY();
        if (p.y > maxY()) r.y = maxY();
        if (p.z < minZ()) r.z = minZ();
        if (p.z > maxZ()) r.z = maxZ();

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
    public Vector3d normalForPlaneClosestToOrigin(Vector3d pointOnAABB, Vector3d origin, boolean testX, boolean testY, boolean testZ) {
        ArrayList<Vector3d> normals = new ArrayList<Vector3d>();

        if (pointOnAABB.z == minZ() && testZ) normals.add(new Vector3d(0, 0, -1));
        if (pointOnAABB.z == maxZ() && testZ) normals.add(new Vector3d(0, 0, 1));
        if (pointOnAABB.x == minX() && testX) normals.add(new Vector3d(-1, 0, 0));
        if (pointOnAABB.x == maxX() && testX) normals.add(new Vector3d(1, 0, 0));
        if (pointOnAABB.y == minY() && testY) normals.add(new Vector3d(0, -1, 0));
        if (pointOnAABB.y == maxY() && testY) normals.add(new Vector3d(0, 1, 0));

        double minDistance = Double.MAX_VALUE;
        Vector3d closestNormal = new Vector3d();

        for (int i = 0; i < normals.size(); i++) {
            Vector3d n = normals.get(i);

            Vector3d diff = new Vector3d(centerPointForNormal(n));
            diff.sub(origin);

            double distance = diff.length();

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
    public Vector3d centerPointForNormal(Vector3d normal) {
        if (normal.x == 1 && normal.y == 0 && normal.z == 0)
            return new Vector3d(getPosition().x + _dimensions.x, getPosition().y, getPosition().z);
        if (normal.x == -1 && normal.y == 0 && normal.z == 0)
            return new Vector3d(getPosition().x - _dimensions.x, getPosition().y, getPosition().z);
        if (normal.x == 0 && normal.y == 0 && normal.z == 1)
            return new Vector3d(getPosition().x, getPosition().y, getPosition().z + _dimensions.z);
        if (normal.x == 0 && normal.y == 0 && normal.z == -1)
            return new Vector3d(getPosition().x, getPosition().y, getPosition().z - _dimensions.z);
        if (normal.x == 0 && normal.y == 1 && normal.z == 0)
            return new Vector3d(getPosition().x, getPosition().y + _dimensions.y, getPosition().z);
        if (normal.x == 0 && normal.y == -1 && normal.z == 0)
            return new Vector3d(getPosition().x, getPosition().y - _dimensions.y, getPosition().z);

        return new Vector3d();
    }

    /**
     * Returns the vertices of this AABB.
     *
     * @return The vertices
     */
    public Vector3d[] getVertices() {
        if (_vertices == null) {
            Vector3d[] vertices = new Vector3d[8];

            // Front
            vertices[0] = new Vector3d(minX(), minY(), maxZ());
            vertices[1] = new Vector3d(maxX(), minY(), maxZ());
            vertices[2] = new Vector3d(maxX(), maxY(), maxZ());
            vertices[3] = new Vector3d(minX(), maxY(), maxZ());
            // Back
            vertices[4] = new Vector3d(minX(), minY(), minZ());
            vertices[5] = new Vector3d(maxX(), minY(), minZ());
            vertices[6] = new Vector3d(maxX(), maxY(), minZ());
            vertices[7] = new Vector3d(minX(), maxY(), minZ());

            _vertices = vertices;
        }

        return _vertices;
    }

    /**
     * Renders this AABB.
     * <p/>
     *
     * @param lineThickness The thickness of the line
     */
    public void render(float lineThickness) {
        glPushMatrix();
        Vector3d playerPosition = Terasology.getInstance().getActivePlayer().getPosition();
        glTranslated(getPosition().x - playerPosition.x, -playerPosition.y, getPosition().z - playerPosition.z);

        renderLocally(lineThickness);

        glPopMatrix();
    }

    public void renderLocally(float lineThickness) {
        if (_displayListWire == -1) {
            generateDisplayListWire();
        }

        glPushMatrix();
        glTranslated(0f, getPosition().y, 0f);

        glLineWidth(lineThickness);
        glCallList(_displayListWire);

        glPopMatrix();
    }

    public void renderSolidLocally() {
        if (_displayListSolid == -1) {
            generateDisplayListSolid();
        }

        glPushMatrix();

        glTranslated(0f, getPosition().y, 0f);
        glScalef(1.5f, 1.5f, 1.5f);

        glCallList(_displayListSolid);

        glPopMatrix();
    }

    private void generateDisplayListSolid() {
        _displayListSolid = glGenLists(1);

        glNewList(_displayListSolid, GL11.GL_COMPILE);
        glBegin(GL_QUADS);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        GL11.glVertex3d(-_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3d(_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3d(_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3d(-_dimensions.x, _dimensions.y, -_dimensions.z);

        GL11.glVertex3d(-_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3d(-_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3d(-_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3d(-_dimensions.x, _dimensions.y, -_dimensions.z);

        GL11.glVertex3d(-_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3d(_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3d(_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3d(-_dimensions.x, _dimensions.y, _dimensions.z);

        GL11.glVertex3d(_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3d(_dimensions.x, _dimensions.y, _dimensions.z);
        GL11.glVertex3d(_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3d(_dimensions.x, -_dimensions.y, -_dimensions.z);

        GL11.glVertex3d(-_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3d(_dimensions.x, _dimensions.y, -_dimensions.z);
        GL11.glVertex3d(_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3d(-_dimensions.x, -_dimensions.y, -_dimensions.z);

        GL11.glVertex3d(-_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3d(_dimensions.x, -_dimensions.y, -_dimensions.z);
        GL11.glVertex3d(_dimensions.x, -_dimensions.y, _dimensions.z);
        GL11.glVertex3d(-_dimensions.x, -_dimensions.y, _dimensions.z);
        glEnd();
        glEndList();

    }

    private void generateDisplayListWire() {
        double offset = 0.001;

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

    public Vector3d getDimensions() {
        return _dimensions;
    }

    public Vector3d getPosition() {
        return _position;
    }

    public void setPosition(Vector3d position) {
        _position.set(position);
    }
}
