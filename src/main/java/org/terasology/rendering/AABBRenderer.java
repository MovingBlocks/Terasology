/*
 * Copyright 2012
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

package org.terasology.rendering;

import org.lwjgl.opengl.GL11;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.AABB;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Renderer for an AABB.
 * @author Immortius
 */
public class AABBRenderer
{
    private int displayListWire = -1;
    private int displayListSolid = -1;

    private AABB aabb;

    public AABBRenderer(AABB aabb) {
        this.aabb = aabb;
    }

    public void setAABB(AABB aabb) {
        if (aabb != null && !aabb.equals(this.aabb)) {
            this.aabb = aabb;
            dispose();
        }
    }

    public void dispose() {
        if (displayListWire != -1) {
            glDeleteLists(displayListWire, 1);
            displayListWire = -1;
        }
        if (displayListSolid != -1) {
            glDeleteLists(displayListSolid, 1);
            displayListSolid = -1;
        }
    }

    /**
     * Renders this AABB.
     * <p/>
     *
     * @param lineThickness The thickness of the line
     */
    public void render(float lineThickness) {
        ShaderManager.getInstance().enableDefault();

        glPushMatrix();
        Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
        glTranslated(aabb.getCenter().x - cameraPosition.x, -cameraPosition.y, aabb.getCenter().z - cameraPosition.z);

        renderLocally(lineThickness);

        glPopMatrix();
    }

    public void renderLocally(float lineThickness) {
        ShaderManager.getInstance().enableDefault();

        if (displayListWire == -1) {
            generateDisplayListWire();
        }

        glPushMatrix();
        glTranslated(0f, aabb.getCenter().y, 0f);

        glLineWidth(lineThickness);
        glCallList(displayListWire);

        glPopMatrix();
    }

    public void renderSolidLocally() {
        ShaderManager.getInstance().enableDefault();

        if (displayListSolid == -1) {
            generateDisplayListSolid();
        }

        glPushMatrix();

        glTranslated(0f, aabb.getCenter().y, 0f);
        glScalef(1.5f, 1.5f, 1.5f);

        glCallList(displayListSolid);

        glPopMatrix();
    }

    private void generateDisplayListSolid() {
        displayListSolid = glGenLists(1);

        glNewList(displayListSolid, GL11.GL_COMPILE);
        glBegin(GL_QUADS);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        Vector3f dimensions = aabb.getExtents();

        GL11.glVertex3f(-dimensions.x, dimensions.y, dimensions.z);
        GL11.glVertex3f(dimensions.x, dimensions.y, dimensions.z);
        GL11.glVertex3f(dimensions.x, dimensions.y, -dimensions.z);
        GL11.glVertex3f(-dimensions.x, dimensions.y, -dimensions.z);

        GL11.glVertex3f(-dimensions.x, -dimensions.y, -dimensions.z);
        GL11.glVertex3f(-dimensions.x, -dimensions.y, dimensions.z);
        GL11.glVertex3f(-dimensions.x, dimensions.y, dimensions.z);
        GL11.glVertex3f(-dimensions.x, dimensions.y, -dimensions.z);

        GL11.glVertex3f(-dimensions.x, -dimensions.y, dimensions.z);
        GL11.glVertex3f(dimensions.x, -dimensions.y, dimensions.z);
        GL11.glVertex3f(dimensions.x, dimensions.y, dimensions.z);
        GL11.glVertex3f(-dimensions.x, dimensions.y, dimensions.z);

        GL11.glVertex3f(dimensions.x, dimensions.y, -dimensions.z);
        GL11.glVertex3f(dimensions.x, dimensions.y, dimensions.z);
        GL11.glVertex3f(dimensions.x, -dimensions.y, dimensions.z);
        GL11.glVertex3f(dimensions.x, -dimensions.y, -dimensions.z);

        GL11.glVertex3f(-dimensions.x, dimensions.y, -dimensions.z);
        GL11.glVertex3f(dimensions.x, dimensions.y, -dimensions.z);
        GL11.glVertex3f(dimensions.x, -dimensions.y, -dimensions.z);
        GL11.glVertex3f(-dimensions.x, -dimensions.y, -dimensions.z);

        GL11.glVertex3f(-dimensions.x, -dimensions.y, -dimensions.z);
        GL11.glVertex3f(dimensions.x, -dimensions.y, -dimensions.z);
        GL11.glVertex3f(dimensions.x, -dimensions.y, dimensions.z);
        GL11.glVertex3f(-dimensions.x, -dimensions.y, dimensions.z);
        glEnd();
        glEndList();

    }

    private void generateDisplayListWire() {
        float offset = 0.001f;

        displayListWire = glGenLists(1);

        glNewList(displayListWire, GL11.GL_COMPILE);
        glColor4f(0.0f, 0.0f, 0.0f, 1.0f);

        Vector3f dimensions = aabb.getExtents();

        // FRONT
        glBegin(GL_LINE_LOOP);
        glVertex3f(-dimensions.x - offset, -dimensions.y - offset, -dimensions.z - offset);
        glVertex3f(+dimensions.x + offset, -dimensions.y - offset, -dimensions.z - offset);
        glVertex3f(+dimensions.x + offset, +dimensions.y + offset, -dimensions.z - offset);
        glVertex3f(-dimensions.x - offset, +dimensions.y + offset, -dimensions.z - offset);
        glEnd();

        // BACK
        glBegin(GL_LINE_LOOP);
        glVertex3f(-dimensions.x - offset, -dimensions.y - offset, +dimensions.z + offset);
        glVertex3f(+dimensions.x + offset, -dimensions.y - offset, +dimensions.z + offset);
        glVertex3f(+dimensions.x + offset, +dimensions.y + offset, +dimensions.z + offset);
        glVertex3f(-dimensions.x - offset, +dimensions.y + offset, +dimensions.z + offset);
        glEnd();

        // TOP
        glBegin(GL_LINE_LOOP);
        glVertex3f(-dimensions.x - offset, -dimensions.y - offset, -dimensions.z - offset);
        glVertex3f(+dimensions.x + offset, -dimensions.y - offset, -dimensions.z - offset);
        glVertex3f(+dimensions.x + offset, -dimensions.y - offset, +dimensions.z + offset);
        glVertex3f(-dimensions.x - offset, -dimensions.y - offset, +dimensions.z + offset);
        glEnd();

        // BOTTOM
        glBegin(GL_LINE_LOOP);
        glVertex3f(-dimensions.x - offset, +dimensions.y + offset, -dimensions.z - offset);
        glVertex3f(+dimensions.x + offset, +dimensions.y + offset, -dimensions.z - offset);
        glVertex3f(+dimensions.x + offset, +dimensions.y + offset, +dimensions.z + offset);
        glVertex3f(-dimensions.x - offset, +dimensions.y + offset, +dimensions.z + offset);
        glEnd();

        // LEFT
        glBegin(GL_LINE_LOOP);
        glVertex3f(-dimensions.x - offset, -dimensions.y - offset, -dimensions.z - offset);
        glVertex3f(-dimensions.x - offset, -dimensions.y - offset, +dimensions.z + offset);
        glVertex3f(-dimensions.x - offset, +dimensions.y + offset, +dimensions.z + offset);
        glVertex3f(-dimensions.x - offset, +dimensions.y + offset, -dimensions.z - offset);
        glEnd();

        // RIGHT
        glBegin(GL_LINE_LOOP);
        glVertex3f(+dimensions.x + offset, -dimensions.y - offset, -dimensions.z - offset);
        glVertex3f(+dimensions.x + offset, -dimensions.y - offset, +dimensions.z + offset);
        glVertex3f(+dimensions.x + offset, +dimensions.y + offset, +dimensions.z + offset);
        glVertex3f(+dimensions.x + offset, +dimensions.y + offset, -dimensions.z - offset);
        glEnd();
        glEndList();
    }
}
