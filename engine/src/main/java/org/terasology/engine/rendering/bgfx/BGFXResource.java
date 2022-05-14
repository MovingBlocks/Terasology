// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.bgfx;

import org.lwjgl.bgfx.BGFXMemory;
import org.lwjgl.bgfx.BGFXVertexLayout;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.gestalt.assets.DisposableResource;

import java.nio.ByteBuffer;

public class BGFXResource implements DisposableResource {

    public BGFXVertexItem[] vertex;

    @Override
    public void close() {

    }

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
