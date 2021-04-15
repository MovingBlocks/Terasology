// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag.stateChanges;

import org.terasology.engine.rendering.dag.StateChange;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;

/**
 * Enables OpenGL's Face Culling.
 *
 * Face Culling is used to discard triangles facing a particular direction, reducing the total number of triangles
 * to be processed. In normal circumstances the GL_BACK triangles are culled, but in some circumstances, i.e. inside
 * a mesh such as the skysphere, the triangles facing the camera are the GL_BACK ones and the GL_FRONT triangles need
 * to be culled instead. Use SetFacesToCull to set which triangles gets culled - by default GL_BACK.
 *
 * Notice that Terasology by default enables face culling. However, the rendering engine disables it again
 * every frame to be consistent with OpenGL's defaults. This is debatable and might change in the future.
 *
 * See StateChange implementation SetFacesToCull to change from OpenGL's default of culling only the GL_BACK faces.
 */
public final class EnableFaceCulling extends EnableStateParameter {
    private static StateChange defaultInstance = new DisableFaceCulling();

    /**
     * The constructor, to be used in the initialise method of a node.
     *
     * Sample use:
     *      addDesiredStateChange(new EnableFaceCulling());
     */
    public EnableFaceCulling() {
        super(GL_CULL_FACE);
    }

    @Override
    public StateChange getDefaultInstance() {
        return defaultInstance;
    }

    private static final class DisableFaceCulling extends DisableStateParameter {
        DisableFaceCulling() {
            super(GL_CULL_FACE);
        }

        @Override
        public StateChange getDefaultInstance() {
            return this;
        }
    }
}
