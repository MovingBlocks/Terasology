// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.opengl;

import org.lwjgl.opengl.GL30;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;

import java.util.List;

public interface OpenGLMeshBase {
    class VBOContext {
        private VBOSubBuffer[] entries;
        private int vbo;

        public static class VBOSubBuffer {
            int version;
            int offset;
            VertexResource resource;
        }
    }

    default void updateState(VBOContext state) {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, state.vbo);
        for (int x = 0; x < state.entries.length; x++) {
            if (state.entries[x].version != state.entries[x].resource.getVersion()) {
                GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, state.entries[x].offset, state.entries[x].resource.buffer);
            }
        }
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    default VBOContext buildVBO(int vbo, int drawType, List<VertexResource> resources) {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
        int bufferSize = 0;
        for (VertexResource vertexResource : resources) {
            bufferSize += vertexResource.inSize();
        }
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, bufferSize, drawType);

        VBOContext state = new VBOContext();
        state.entries = new VBOContext.VBOSubBuffer[resources.size()];

        int offset = 0;
        for (int i = 0; i < resources.size(); i++) {
            VertexResource resource = resources.get(i);

            state.entries[i] = new VBOContext.VBOSubBuffer();
            state.entries[i].resource = resource;
            state.entries[i].version = resource.getVersion();
            state.entries[i].offset = offset;

            resource.buffer.rewind();
            GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, offset, resource.buffer);
            for (VertexResource.VertexDefinition attribute : resource.attributes) {
                GL30.glEnableVertexAttribArray(attribute.location);
                GL30.glVertexAttribPointer(attribute.location, attribute.attribute.count,
                        attribute.attribute.mapping.glType, false, resource.getInStride(), offset + attribute.stride);
            }
            offset += resource.inSize();
        }
        state.vbo = vbo;

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        return state;
    }
}
