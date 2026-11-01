// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.opengl;

import org.lwjgl.opengl.GL30;
import org.terasology.engine.rendering.assets.mesh.resource.AllocationType;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;

public interface OpenGLMeshBase {

    default boolean updateState(VBOContext state) {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, state.vbo);
        for (VBOContext.VBOSubBuffer subBuffer : state.entries) {
            if (subBuffer.version != subBuffer.resource.getVersion()) {
                if (subBuffer.size != subBuffer.resource.inSize()) {
                    return false;
                }
                VertexResource resource = subBuffer.resource;
                int offset = subBuffer.offset;
                resource.writeBuffer((buffer) -> {
                    GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, offset, buffer);
                });
            }
        }
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        return true;
    }

    default VBOContext buildVBO(int vbo, AllocationType allocation, VertexResource[] resources) {
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo);
        int bufferSize = 0;
        for (VertexResource vertexResource : resources) {
            bufferSize += vertexResource.inSize();
        }
        GL30.glBufferData(GL30.GL_ARRAY_BUFFER, bufferSize, allocation.glCall);

        VBOContext state = new VBOContext();
        state.entries = new VBOContext.VBOSubBuffer[resources.length];

        int offset = 0;
        for (int i = 0; i < resources.length; i++) {
            VertexResource resource = resources[i];

            state.entries[i] = new VBOContext.VBOSubBuffer();
            state.entries[i].resource = resource;
            state.entries[i].version = resource.getVersion();
            state.entries[i].offset = offset;
            state.entries[i].size = resource.inSize();
            if (state.entries[i].size == 0) {
                continue;
            }

            int currentOffset = offset;
            resource.writeBuffer((buffer) -> {
                GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, currentOffset, buffer);
            });
            for (VertexResource.VertexDefinition attribute : resource.definitions()) {
                GL30.glEnableVertexAttribArray(attribute.location);
                GL30.glVertexAttribPointer(attribute.location, attribute.attribute.count,
                        attribute.attribute.mapping.glType, false, resource.inStride(), offset + attribute.offset);
            }
            offset += resource.inSize();
        }
        state.vbo = vbo;

        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        return state;
    }

    class VBOContext {
        private VBOSubBuffer[] entries;
        private int vbo;

        public static class VBOSubBuffer {
            int version;
            int offset;
            int size;
            VertexResource resource;
        }
    }
}
