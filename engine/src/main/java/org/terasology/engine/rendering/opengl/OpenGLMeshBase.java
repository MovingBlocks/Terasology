// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.opengl;

import org.lwjgl.opengl.GL30;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;

import java.nio.ByteBuffer;
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
                VertexResource resource = state.entries[x].resource;

                int offset = state.entries[x].offset;
                resource.writeBuffer((buffer) -> {
                    GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, offset, buffer);
                });
//                ByteBuffer buffer = resource.buffer();
//                buffer.limit(resource.inSize());
//                GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, state.entries[x].offset, buffer);
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

            int currentOffset = offset;
            resource.writeBuffer((buffer) -> {
                GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, currentOffset, buffer);
            });
//            ByteBuffer vertexBuffer = resource.buffer();
//            vertexBuffer.rewind();
//            vertexBuffer.limit(resource.inSize());
//            GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, offset, vertexBuffer);
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
}
