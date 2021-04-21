// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.assets.mesh.resouce.IndexResource;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexAttribute;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexFloatAttribute;
import org.terasology.engine.rendering.assets.mesh.resouce.VertexResource;
import org.terasology.engine.rendering.gltf.model.GLTF;
import org.terasology.engine.rendering.gltf.model.GLTFAccessor;
import org.terasology.engine.rendering.gltf.model.GLTFBufferView;
import org.terasology.engine.rendering.gltf.model.GLTFMesh;
import org.terasology.engine.rendering.gltf.model.GLTFNode;
import org.terasology.engine.rendering.gltf.model.GLTFPrimitive;
import org.terasology.nui.Color;

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


            VertexResource.VertexResourceBuilder builder;
            VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> position = VertexFloatAttribute.EMPTY_BINDING;
            VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> normal = VertexFloatAttribute.EMPTY_BINDING;
            VertexFloatAttribute.VertexAttributeFloatBinding<Vector2f> uv0 = VertexFloatAttribute.EMPTY_BINDING;
            VertexFloatAttribute.VertexAttributeFloatBinding<Vector2f> uv1 = VertexFloatAttribute.EMPTY_BINDING;
            VertexFloatAttribute.VertexAttributeFloatBinding<Color> color0 = VertexFloatAttribute.EMPTY_BINDING;
            VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> light0 = VertexFloatAttribute.EMPTY_BINDING;


            for (MeshAttributeSemantic semantic : MeshAttributeSemantic.values()) {
                GLTFAccessor gltfAccessor = getAccessor(semantic, gltfPrimitive, gltf);
                if (gltfAccessor != null && gltfAccessor.getBufferView() != null) {
                    GLTFBufferView bufferView = gltf.getBufferViews().get(gltfAccessor.getBufferView());
                    switch (semantic) {
                        case Position:
                            builder = new VertexResource.VertexResourceBuilder(gltfAccessor.getCount());
                            position = builder.add(StandardMeshData.VERTEX_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, true);
                            builder.build();
                            GLTFAttributeMapping.readVec3FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, position);
                            break;
                        case Normal:
                            builder = new VertexResource.VertexResourceBuilder(gltfAccessor.getCount());
                            VertexFloatAttribute.VertexAttributeFloatBinding<Vector3f> normals = builder.add(StandardMeshData.NORMAL_INDEX, VertexAttribute.VECTOR_3_F_VERTEX_ATTRIBUTE, false);
                            builder.build();
                            GLTFAttributeMapping.readVec3FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, normals);
                            break;
                        case Texcoord_0:
                            builder = new VertexResource.VertexResourceBuilder(gltfAccessor.getCount());
                            uv0 = builder.add(StandardMeshData.UV0_INDEX, VertexAttribute.VECTOR_2_F_VERTEX_ATTRIBUTE, false);
                            builder.build();
                            GLTFAttributeMapping.readVec2FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, uv0);
                            break;
                        case Texcoord_1:
                            builder = new VertexResource.VertexResourceBuilder(gltfAccessor.getCount());
                            uv1 = builder.add(StandardMeshData.UV1_INDEX, VertexAttribute.VECTOR_2_F_VERTEX_ATTRIBUTE, false);
                            builder.build();
                            GLTFAttributeMapping.readVec2FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, uv1);
                            break;
                        case Color_0:
                            builder = new VertexResource.VertexResourceBuilder(gltfAccessor.getCount());
                            color0 = builder.add(StandardMeshData.COLOR0_INDEX, VertexAttribute.COLOR_4_F_VERTEX_ATTRIBUTE, false);
                            builder.build();
                            GLTFAttributeMapping.readColor4FBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, color0);
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
            IndexResource indexResource = new IndexResource(indices.size(), true);
            indexResource.map(0, indices.size(), indices.toArray(), 0);

            return new StandardMeshData(position, normal, uv0, uv1, color0, light0, indexResource);
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
