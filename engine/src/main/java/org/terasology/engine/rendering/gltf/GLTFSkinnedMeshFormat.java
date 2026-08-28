// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.rendering.assets.mesh.SkinnedMeshData;
import org.terasology.engine.rendering.assets.mesh.StandardSkinnedMeshData;
import org.terasology.engine.rendering.gltf.model.GLTF;
import org.terasology.engine.rendering.gltf.model.GLTFAccessor;
import org.terasology.engine.rendering.gltf.model.GLTFBufferView;
import org.terasology.engine.rendering.gltf.model.GLTFMesh;
import org.terasology.engine.rendering.gltf.model.GLTFPrimitive;
import org.terasology.engine.rendering.gltf.model.GLTFSkin;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@RegisterAssetFileFormat
public class GLTFSkinnedMeshFormat extends GLTFCommonFormat<SkinnedMeshData> {

    private Logger logger = LoggerFactory.getLogger(GLTFMeshFormat.class);

    public GLTFSkinnedMeshFormat(AssetManager assetManager) {
        super(assetManager, "gltf");
    }

    @Override
    public SkinnedMeshData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (Reader in = new InputStreamReader(inputs.get(0).openStream())) {
            GLTF gltf = gson.fromJson(in, GLTF.class);

            checkVersionSupported(urn, gltf);
            checkMeshPresent(urn, gltf);

            GLTFSkin skin = gltf.getSkins().get(0);
            GLTFMesh gltfMesh = gltf.getMeshes().get(0);

            checkPrimitivePresent(urn, gltfMesh);
            GLTFPrimitive gltfPrimitive = gltfMesh.getPrimitives().get(0);

            List<byte[]> loadedBuffers = loadBinaryBuffers(urn, gltf);

            StandardSkinnedMeshData skinnedMeshData = new StandardSkinnedMeshData(loadBones(gltf, skin, loadedBuffers));

            List<Vector3f> positions = loadVector3fList(MeshAttributeSemantic.Position, gltfPrimitive, gltf, loadedBuffers);
            List<Vector3f> normals = loadVector3fList(MeshAttributeSemantic.Normal, gltfPrimitive, gltf, loadedBuffers);
            TIntList joints = readIntBuffer(MeshAttributeSemantic.Joints_0, gltfPrimitive, gltf, loadedBuffers);
            TFloatList weights = readFloatBuffer(MeshAttributeSemantic.Weights_0, gltfPrimitive, gltf, loadedBuffers);
            List<Vector2f> uvs = loadVector2fList(MeshAttributeSemantic.Texcoord_0, gltfPrimitive, gltf, loadedBuffers);

            Vector4f tempWeight = new Vector4f();
            for (int i  = 0; i < positions.size(); i++) {
                skinnedMeshData.position.put(positions.get(i));
                skinnedMeshData.normal.put(normals.get(i));

                skinnedMeshData.boneIndex0.put((byte) joints.get(4 * i + 0));
                skinnedMeshData.boneIndex1.put((byte) joints.get(4 * i + 1));
                skinnedMeshData.boneIndex2.put((byte) joints.get(4 * i + 2));
                skinnedMeshData.boneIndex3.put((byte) joints.get(4 * i + 3));
                tempWeight.set(
                        weights.get(4 * i + 0),
                        weights.get(4 * i + 1),
                        weights.get(4 * i + 2),
                        weights.get(4 * i + 3)
                );
                skinnedMeshData.weight.put(tempWeight);
                skinnedMeshData.uv0.put(uvs.get(i));
            }

            GLTFAccessor indicesAccessor = getIndicesAccessor(gltfPrimitive, gltf, urn);
            if (indicesAccessor.getBufferView() == null) {
                throw new IOException("Missing buffer view for indices accessor in " + urn);
            }
            GLTFBufferView indicesBuffer = gltf.getBufferViews().get(indicesAccessor.getBufferView());
            checkIndicesBuffer(indicesBuffer);
            TIntList indicies = new TIntArrayList();
            readBuffer(loadedBuffers.get(indicesBuffer.getBuffer()), indicesAccessor, indicesBuffer, indicies);

            indicies.forEach(value -> {
                skinnedMeshData.indices.put(value);
                return true;
            });

            if (gltf.getSkins().isEmpty()) {
                throw new IOException("Skeletal mesh '" + urn + "' missing skin");
            }

            return skinnedMeshData;
        }
    }

}
