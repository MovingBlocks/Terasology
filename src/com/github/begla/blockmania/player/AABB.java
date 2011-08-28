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
package com.github.begla.blockmania.player;

import com.github.begla.blockmania.utilities.VectorPool;
import com.github.begla.blockmania.world.RenderableObject;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class AABB extends RenderableObject {

    private final Vector3f _dimensions;

    /**
     * @param position
     * @param dimensions
     */
    public AABB(Vector3f position, Vector3f dimensions) {
        this._position = position;
        this._dimensions = dimensions;
    }

    /**
     * @param aabb2
     * @return
     */
    public boolean overlaps(AABB aabb2) {
        Vector3f t = Vector3f.sub(aabb2.getPosition(), getPosition(), null);
        return Math.abs(t.x) <= (getDimensions().x + aabb2.getDimensions().x) && Math.abs(t.y) <= (getDimensions().y + aabb2.getDimensions().y) && Math.abs(t.z) <= (getDimensions().z + aabb2.getDimensions().z);
    }

    /**
     * @return
     */
    public Vector3f getDimensions() {
        return _dimensions;
    }

    @Override
    public void render() {
        float offset = 0.001f;

        glPushMatrix();
        glTranslatef(_position.x, _position.y, _position.z);

        glLineWidth(2f);
        glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

        // FRONT
        glBegin(GL_LINE_LOOP);
        glVertex3f(-_dimensions.x - offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3f(+_dimensions.x + offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3f(+_dimensions.x + offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glVertex3f(-_dimensions.x - offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glEnd();

        // BACK
        glBegin(GL_LINE_LOOP);
        glVertex3f(-_dimensions.x - offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3f(+_dimensions.x + offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3f(+_dimensions.x + offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3f(-_dimensions.x - offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glEnd();

        // TOP
        glBegin(GL_LINE_LOOP);
        glVertex3f(-_dimensions.x - offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3f(+_dimensions.x + offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3f(+_dimensions.x + offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3f(-_dimensions.x - offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glEnd();

        // BOTTOM
        glBegin(GL_LINE_LOOP);
        glVertex3f(-_dimensions.x - offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glVertex3f(+_dimensions.x + offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glVertex3f(+_dimensions.x + offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3f(-_dimensions.x - offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glEnd();

        // LEFT
        glBegin(GL_LINE_LOOP);
        glVertex3f(-_dimensions.x - offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3f(-_dimensions.x - offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3f(-_dimensions.x - offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3f(-_dimensions.x - offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glEnd();

        // RIGHT
        glBegin(GL_LINE_LOOP);
        glVertex3f(+_dimensions.x + offset, -_dimensions.y - offset, -_dimensions.z - offset);
        glVertex3f(+_dimensions.x + offset, -_dimensions.y - offset, +_dimensions.z + offset);
        glVertex3f(+_dimensions.x + offset, +_dimensions.y + offset, +_dimensions.z + offset);
        glVertex3f(+_dimensions.x + offset, +_dimensions.y + offset, -_dimensions.z - offset);
        glEnd();
        glPopMatrix();
    }

    @Override
    public void update() {
        // Do nothing. Really.
    }

    /**
     * @param point
     * @return
     */
    public Vector3f closestNormalToPoint(Vector3f point) {

        Vector3f[] sides = new Vector3f[6];

        // Calculate the center points of each of the six sides
        // Top side
        sides[0] = VectorPool.getVector(_position.x, _position.y + _dimensions.y, _position.z);
        // Left side
        sides[1] = VectorPool.getVector(_position.x - _dimensions.x, _position.y, _position.z);
        // Right side
        sides[2] = VectorPool.getVector(_position.x + _dimensions.x, _position.y, _position.z);
        // Bottom side
        sides[3] = VectorPool.getVector(_position.x, _position.y - _dimensions.y, _position.z);
        // Front side
        sides[4] = VectorPool.getVector(_position.x, _position.y, _position.z + _dimensions.z);
        // Back side
        sides[5] = VectorPool.getVector(_position.x, _position.y, _position.z - _dimensions.z);

        int closestSideIndex = -1;
        float closestSideDistance = Integer.MAX_VALUE;

        // Calculate the distance of each center point to the given point
        for (int i = sides.length - 1; i >= 0; i--) {
            Vector3f sideToPoint = Vector3f.sub(point, sides[i], null);
            float distance = sideToPoint.length();

            if (distance < closestSideDistance) {
                closestSideDistance = distance;
                closestSideIndex = i;
            }
        }

        switch (closestSideIndex) {
            case 0:
                return VectorPool.getVector(0, 1, 0);
            case 1:
                return VectorPool.getVector(1, 0, 0);
            case 2:
                return VectorPool.getVector(-1, 0, 0);
            case 3:
                return VectorPool.getVector(0, -1, 0);
            case 4:
                return VectorPool.getVector(0, 0, 1);
            case 5:
                return VectorPool.getVector(0, 0, -1);
        }

        return VectorPool.getVector();
    }
}
