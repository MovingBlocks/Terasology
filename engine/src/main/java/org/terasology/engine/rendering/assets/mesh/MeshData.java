// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL44;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.engine.rendering.assets.mesh.resouce.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;

/**
 */
public abstract class MeshData implements AssetData {
    public enum DrawingMode {
        POINTS(GL30.GL_POINT),
        LINES(GL30.GL_LINES),
        LINE_LOOP(GL30.GL_LINE_LOOP),
        LINE_STRIP(GL30.GL_LINE_STRIP),
        TRIANGLES(GL30.GL_TRIANGLES),
        TRIANGLE_STRIP(GL30.GL_TRIANGLE_STRIP),
        TRIANGLE_FAN(GL30.GL_TRIANGLE_FAN),
        LINES_ADJACENCY(GL33.GL_LINES_ADJACENCY),
        LINE_STRIP_ADJACENCY(GL44.GL_LINE_STRIP_ADJACENCY),
        TRIANGLES_ADJACENCY(GL44.GL_TRIANGLES_ADJACENCY),
        TRIANGLE_STRIP_ADJACENCY(GL44.GL_TRIANGLE_STRIP_ADJACENCY);

        public final int glCall;
        DrawingMode(int gl) {
            this.glCall = gl;
        }

    };

    private final DrawingMode mode;

    public MeshData() {
        this(DrawingMode.TRIANGLES);
    }

    public MeshData(DrawingMode mode) {
        this.mode = mode;
    }

    public DrawingMode getMode() {
        return mode;
    }

    public abstract float[] verts();

    public abstract VertexResource[] vertexResources();
    public abstract IndexResource indexResource();
}
