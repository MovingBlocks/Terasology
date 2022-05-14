// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.bgfx;

import org.lwjgl.bgfx.BGFX;
import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.bgfx.BGFXVertexLayout;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;

import java.nio.ByteBuffer;



public interface BGFXMeshBase {

    default BGFXResource buildBGFXResource(VertexResource[] resources) {
        BGFXResource state = new BGFXResource();
        state.vertex = new BGFXResource.BGFXVertexItem[resources.length];
        for (int i = 0; i < resources.length; i++) {
            if (resources[i].inSize() == 0) {
                continue;
            }
            var bgfxResource = new BGFXResource.BGFXVertexItem();
            resources[i].writeBuffer((buffer) -> {
                bgfxResource.buffer = buffer;
                bgfxResource.memory = BGFX.bgfx_make_ref(buffer);
            });
            BGFXVertexLayout layout = BGFXVertexLayout.calloc();
            BGFX.bgfx_vertex_layout_begin(layout, -1);
            for (VertexResource.VertexDefinition attribute : resources[i].definitions()) {
                BGFX.bgfx_vertex_layout_add(layout, attribute.location.bgfxLocation,
                        attribute.attribute.count,
                        attribute.attribute.mapping.size, false, false);
            }
            BGFX.bgfx_vertex_layout_end(layout);
            bgfxResource.bufferId = BGFX.bgfx_create_vertex_buffer(bgfxResource.memory, layout, BGFX.BGFX_BUFFER_NONE);
            bgfxResource.layout = layout;
            bgfxResource.resource = resources[i];
            state.vertex[i] = bgfxResource;
        }
        return state;
    }

    class BGFXResource {
        public BGFXVertexItem[] vertex;

        public static class BGFXVertexItem {
            public ByteBuffer buffer = null;
            public BGFXMemory memory = null;
            public BGFXVertexLayout layout = null;
            public VertexResource resource = null;
            public short bufferId = -1;
        }

        public void free() {
            for (var en : vertex) {
                if (en != null) {
                    en.layout.free();
                    en.memory.free();
                    en.layout.free();
                }
            }
        }
    }
}
