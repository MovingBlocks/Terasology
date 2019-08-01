/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.assets.gltf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.assets.gltf.model.*;

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

            MeshData mesh = new MeshData();
            for (MeshAttributeSemantic semantic : MeshAttributeSemantic.values()) {
                GLTFAccessor gltfAccessor = getAccessor(semantic, gltfPrimitive, gltf);
                if (gltfAccessor != null && gltfAccessor.getBufferView() != null) {
                    GLTFBufferView bufferView = gltf.getBufferViews().get(gltfAccessor.getBufferView());
                    readBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, semantic.getTargetFloatBuffer(mesh));
                }
            }
            GLTFAccessor indicesAccessor = getIndicesAccessor(gltfPrimitive, gltf, urn);
            if (indicesAccessor.getBufferView() == null) {
                throw new IOException("Missing buffer view for indices accessor in " + urn);
            }
            GLTFBufferView indicesBuffer = gltf.getBufferViews().get(indicesAccessor.getBufferView());
            checkIndicesBuffer(indicesBuffer);

            readBuffer(loadedBuffers.get(indicesBuffer.getBuffer()), indicesAccessor, indicesBuffer, mesh.getIndices());
            return mesh;
        }
    }


}
