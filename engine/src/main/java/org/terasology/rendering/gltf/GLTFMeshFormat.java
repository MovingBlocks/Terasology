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
package org.terasology.rendering.gltf;

import gnu.trove.list.TFloatList;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.gltf.model.GLTF;
import org.terasology.rendering.gltf.model.GLTFAccessor;
import org.terasology.rendering.gltf.model.GLTFBufferView;
import org.terasology.rendering.gltf.model.GLTFMesh;
import org.terasology.rendering.gltf.model.GLTFNode;
import org.terasology.rendering.gltf.model.GLTFPrimitive;

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
            applyTransformations(gltf, mesh.getVertices(), mesh.getNormals());
            return mesh;
        }
    }

    private void applyTransformations(GLTF gltf, TFloatList vertices, TFloatList normals) {
        int nodeIndex = -1;
        for (int i = 0; i < gltf.getNodes().size(); i++) {
            if (gltf.getNodes().get(i).getMesh() == 0) {
                nodeIndex = i;
                break;
            }
        }

        Matrix4f transform = getMatrix(gltf, nodeIndex);

        applyTransformations(vertices, transform, false);
        transform.setTranslation(new Vector3f());
        applyTransformations(normals, transform, true);
    }

    private Matrix4f getMatrix(GLTF gltf, int nodeIndex) {
        Matrix4f transform = new Matrix4f();

        if (nodeIndex != -1) {
            GLTFNode node = gltf.getNodes().get(nodeIndex);
            if (node.getMatrix() == null) {
                Vector3f position = new Vector3f();
                Quaternionf rotation = new Quaternionf();
                Vector3f scale = new Vector3f(1, 1, 1);

                if (node.getTranslation() != null) {
                    position.set(node.getTranslation());
                }
                if (node.getRotation() != null) {
                    rotation.set(node.getRotation());
                }
                if (node.getScale() != null) {
                    scale.set(node.getScale());
                }
                transform.translationRotateScale(position, rotation, scale);
            } else {
                transform.set(node.getMatrix());
            }

            int parentNodeIndex = getParentNode(gltf, nodeIndex);
            Matrix4f parentTransform = getMatrix(gltf, parentNodeIndex);
            parentTransform.mul(transform); //Must be multiplied in the right order
            transform.set(parentTransform);
        }

        return transform;
    }

    private int getParentNode(GLTF gltf, int nodeIndex) {
        for (int i = 0; i < gltf.getNodes().size(); i++) {
            GLTFNode curr = gltf.getNodes().get(i);
            if (curr.getChildren() != null && curr.getChildren().contains(nodeIndex)) {
                return i;
            }
        }
        return -1;
    }

    private void applyTransformations(TFloatList buffer, Matrix4f transform, boolean normalize) {
        Vector3f current = new Vector3f();
        for (int i = 0; i < buffer.size(); i += 3) {
            current.set(buffer.get(i), buffer.get(i + 1), buffer.get(i + 2));
            transform.transformPosition(current);
            if (normalize) {
                current.normalize();
            }
            buffer.set(i, current.x());
            buffer.set(i + 1, current.y());
            buffer.set(i + 2, current.z());
        }
    }
}
