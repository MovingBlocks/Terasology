/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.stateChanges;

import org.terasology.rendering.dag.StateChange;

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
