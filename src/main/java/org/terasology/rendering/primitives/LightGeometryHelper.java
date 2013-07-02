package org.terasology.rendering.primitives;


import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

import static org.lwjgl.opengl.GL11.*;

public class LightGeometryHelper {

    static int displayListSphere = -1;

    public static void renderSphereGeometry() {
        if (displayListSphere == -1) {
            displayListSphere = glGenLists(1);

            Sphere sphere = new Sphere();

            glNewList(displayListSphere, GL11.GL_COMPILE);

            sphere.draw(1, 4, 4);

            glEndList();
        }

        glCallList(displayListSphere);
    }
}
