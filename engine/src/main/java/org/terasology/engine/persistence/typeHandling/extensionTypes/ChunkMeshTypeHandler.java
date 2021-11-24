// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.lwjgl.BufferUtils;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.ChunkMeshImpl;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChunkMeshTypeHandler extends TypeHandler<ChunkMesh> {
    @Override
    protected PersistedData serializeNonNull(ChunkMesh value, PersistedDataSerializer serializer) {
        if (!value.hasVertexElements()) {
            throw new IllegalStateException("Attempting to serialize a ChunkMesh whose data has already been discarded.");
        }
        List<PersistedData> data = new ArrayList<>();
        for (ChunkMesh.RenderType renderType : ChunkMesh.RenderType.values()) {

            value.getVertexElements(renderType).buffer.writeBuffer(buffer -> {
                data.add(serializer.serialize(buffer));
            });
            value.getVertexElements(renderType).indices.writeBuffer(buffer -> {
                data.add(serializer.serialize(buffer));
            });
        }
        return serializer.serialize(data);
    }

    @Override
    public Optional<ChunkMesh> deserialize(PersistedData data) {
        List<ByteBuffer> asBuffers = new ArrayList<>();
        for (PersistedData datum : data.getAsArray()) {
            ByteBuffer buffer = datum.getAsByteBuffer();
            ByteBuffer directBuffer = BufferUtils.createByteBuffer(buffer.limit());
            directBuffer.put(buffer);
            directBuffer.rewind();
            asBuffers.add(directBuffer);
        }
        ChunkMesh result = new ChunkMeshImpl();
        for (ChunkMesh.RenderType renderType : ChunkMesh.RenderType.values()) {
            result.getVertexElements(renderType).buffer.replace(asBuffers.remove(0));
            result.getVertexElements(renderType).indices.replace(asBuffers.remove(0));
        }
        result.updateMesh();
        return Optional.of(result);
    }

}
