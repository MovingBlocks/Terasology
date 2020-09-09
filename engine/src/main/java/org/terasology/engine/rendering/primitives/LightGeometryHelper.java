// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;


import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glNewList;

public final class LightGeometryHelper {

    private static int displayListSphere = -1;

    private LightGeometryHelper() {
    }

    public static void renderSphereGeometry() {
        if (displayListSphere == -1) {
            displayListSphere = glGenLists(1);

            Sphere sphere = new Sphere();

            glNewList(displayListSphere, GL11.GL_COMPILE);

            sphere.draw(1, 8, 8);

            glEndList();
        }

        glCallList(displayListSphere);
    }
}
