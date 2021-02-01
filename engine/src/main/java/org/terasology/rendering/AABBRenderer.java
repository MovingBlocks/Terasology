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

package org.terasology.rendering;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.module.sandbox.API;
import org.terasology.registry.CoreRegistry;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDeleteLists;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslated;
import static org.lwjgl.opengl.GL11.glVertex3f;

/**
 * Renderer for an AABB.
 */
@API
public class AABBRenderer implements BlockOverlayRenderer {
    private int displayListWire = -1;
    private int displayListSolid = -1;
    private Vector4f solidColor = new Vector4f(1f, 1f, 1f, 1f);

    private AABBf aabb = new AABBf();

    public AABBRenderer(AABBfc aabb) {
        this.aabb.set(aabb);
    }

    @Override
    public void setAABB(AABBfc from) {
        if (from != null && !from.equals(this.aabb)) {
            this.aabb.set(from);
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

    public void setSolidColor(Vector4f color) {
        solidColor = color;
    }

    /**
     * Renders this AABB.
     * <br><br>
     */
    @Override
    public void render() {
        CoreRegistry.get(ShaderManager.class).enableDefault();

        glPushMatrix();
        Vector3f cameraPosition = CoreRegistry.get(LocalPlayer.class).getViewPosition(new Vector3f());
        Vector3f center = aabb.center(new Vector3f());
        glTranslated(center.x - cameraPosition.x, center.y - cameraPosition.y, center.z - cameraPosition.z);

        renderLocally();

        glPopMatrix();
    }

    public void renderSolid() {
        CoreRegistry.get(ShaderManager.class).enableDefault();

        glPushMatrix();
        Vector3f cameraPosition = CoreRegistry.get(LocalPlayer.class).getViewPosition(new Vector3f());
        Vector3f center = aabb.center(new Vector3f());
        glTranslated(center.x - cameraPosition.x, -cameraPosition.y, center.z - cameraPosition.z);

        renderSolidLocally();

        glPopMatrix();
    }

    /**
     * Maintained for API compatibility.
     */
    public void renderLocally(float ignored) {
        renderLocally();
    }

    public void renderLocally() {
        CoreRegistry.get(ShaderManager.class).enableDefault();

        if (displayListWire == -1) {
            generateDisplayListWire();
        }

        glCallList(displayListWire);
    }

    public void renderSolidLocally() {
        CoreRegistry.get(ShaderManager.class).enableDefault();

        if (displayListSolid == -1) {
            generateDisplayListSolid();
        }
        glEnable(GL_BLEND);
        glPushMatrix();

        Vector3f center = aabb.center(new Vector3f());
        glTranslated(0f, center.y, 0f);
        glScalef(1.5f, 1.5f, 1.5f);

        glCallList(displayListSolid);

        glPopMatrix();
        glDisable(GL_BLEND);
    }

    private void generateDisplayListSolid() {
        displayListSolid = glGenLists(1);

        glNewList(displayListSolid, GL11.GL_COMPILE);
        glBegin(GL_QUADS);
        glColor4f(solidColor.x, solidColor.y, solidColor.z, solidColor.w);

        Vector3f dimensions = aabb.extent(new Vector3f());

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

        Vector3f dimensions = aabb.extent(new Vector3f());

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

    public AABBfc getAABB() {
        return aabb;
    }
}
