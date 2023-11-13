// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.gltf.model.GLTF;
import org.terasology.engine.rendering.gltf.model.GLTFAccessor;
import org.terasology.engine.rendering.gltf.model.GLTFBufferView;
import org.terasology.engine.rendering.gltf.model.GLTFMesh;
import org.terasology.engine.rendering.gltf.model.GLTFPrimitive;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

/**
 * glTF mesh format loader. Currently supports:
 * <ul>
 *     <li>Single mesh with single primitive</li>
 *     <li>Float attributes</li>
 *     <li>Reference binary data</li>
 * </ul>
 * Does not support:
 * <ul>
 *     <li>Sparse attributes</li>
 *     <li>Embedded binary data</li>
 * </ul>
 */
@RegisterAssetFileFormat
public class GLTFMeshFormat extends GLTFCommonFormat<MeshData> {

    private Logger logger = LoggerFactory.getLogger(GLTFMeshFormat.class);

    public GLTFMeshFormat(AssetManager assetManager) {
        super(assetManager, "gltf");
        this.assetManager = assetManager;
    }

    @Override
    public MeshData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (Reader in = new InputStreamReader(inputs.get(0).openStream())) {
            GLTF gltf = gson.fromJson(in, GLTF.class);

            checkVersionSupported(urn, gltf);
            checkMeshPresent(urn, gltf);
            GLTFMesh gltfMesh = gltf.getMeshes().get(0);

            checkPrimitivePresent(urn, gltfMesh);
            GLTFPrimitive gltfPrimitive = gltfMesh.getPrimitives().get(0);

            List<byte[]> loadedBuffers = loadBinaryBuffers(urn, gltf);

            StandardMeshData meshData = new StandardMeshData();
            for (MeshAttributeSemantic semantic : MeshAttributeSemantic.values()) {
                GLTFAccessor gltfAccessor = getAccessor(semantic, gltfPrimitive, gltf);
                if (gltfAccessor != null && gltfAccessor.getBufferView() != null) {
                    GLTFBufferView bufferView = gltf.getBufferViews().get(gltfAccessor.getBufferView());
                    switch (semantic) {
                        case Position:
                            GLTFAttributeMapping.readVec3FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor,
                                    bufferView, meshData.position);
                            break;
                        case Normal:
                            GLTFAttributeMapping.readVec3FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor,
                                    bufferView, meshData.normal);
                            break;
                        case Texcoord_0:
                            GLTFAttributeMapping.readVec2FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor,
                                    bufferView, meshData.uv0);
                            break;
                        case Texcoord_1:
                            GLTFAttributeMapping.readVec2FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor,
                                    bufferView, meshData.uv1);
                            break;
                        case Color_0:
                            GLTFAttributeMapping.readColor4FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor,
                                    bufferView, meshData.color0);
                            break;
                    }
                }
            }
            GLTFAccessor indicesAccessor = getIndicesAccessor(gltfPrimitive, gltf, urn);
            if (indicesAccessor.getBufferView() == null) {
                throw new IOException("Missing buffer view for indices accessor in " + urn);
            }
            GLTFBufferView indicesBuffer = gltf.getBufferViews().get(indicesAccessor.getBufferView());
            checkIndicesBuffer(indicesBuffer);

            TIntArrayList indices = new TIntArrayList();
            readBuffer(loadedBuffers.get(indicesBuffer.getBuffer()), indicesAccessor, indicesBuffer, indices);
            for (int x = 0; x < indices.size(); x++) {
                meshData.indices.put(indices.get(x));
            }
            return meshData;
        }
    }
}
