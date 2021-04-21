// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.engine.rendering.assets.skeletalmesh.BoneWeight;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.engine.rendering.gltf.model.GLTF;
import org.terasology.engine.rendering.gltf.model.GLTFAccessor;
import org.terasology.engine.rendering.gltf.model.GLTFBufferView;
import org.terasology.engine.rendering.gltf.model.GLTFMesh;
import org.terasology.engine.rendering.gltf.model.GLTFPrimitive;
import org.terasology.engine.rendering.gltf.model.GLTFSkin;
import org.terasology.joml.geom.AABBf;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

@RegisterAssetFileFormat
public class GLTFSkeletalMeshFormat extends GLTFCommonFormat<SkeletalMeshData> {

    private Logger logger = LoggerFactory.getLogger(GLTFMeshFormat.class);

    public GLTFSkeletalMeshFormat(AssetManager assetManager) {
        super(assetManager, "gltf");
    }

    @Override
    public SkeletalMeshData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (Reader in = new InputStreamReader(inputs.get(0).openStream())) {
            GLTF gltf = gson.fromJson(in, GLTF.class);

            checkVersionSupported(urn, gltf);
            checkMeshPresent(urn, gltf);

            GLTFSkin skin = gltf.getSkins().get(0);
            GLTFMesh gltfMesh = gltf.getMeshes().get(0);

            checkPrimitivePresent(urn, gltfMesh);
            GLTFPrimitive gltfPrimitive = gltfMesh.getPrimitives().get(0);

            List<byte[]> loadedBuffers = loadBinaryBuffers(urn, gltf);

//            SkeletalMeshDataBuilder builder = new SkeletalMeshDataBuilder();

            List<Bone> bones = loadBones(gltf, skin, loadedBuffers);
            int rootBones = 0;
            for (Bone bone : bones) {
                if(bone.getParent() == null) {
                    rootBones++;
                }
            }
            if (rootBones == 0) {
                throw new IllegalStateException("Cannot create a skeleton with no root bones");
            } else if (rootBones > 1) {
                throw new IllegalStateException("Cannot create a skeleton with multiple root bones");
            }

            List<Vector3f> positions = loadVector3fList(MeshAttributeSemantic.Position, gltfPrimitive, gltf, loadedBuffers);
            List<Vector3f> normals = loadVector3fList(MeshAttributeSemantic.Normal, gltfPrimitive, gltf, loadedBuffers);
            TIntList joints = readIntBuffer(MeshAttributeSemantic.Joints_0, gltfPrimitive, gltf, loadedBuffers);
            TFloatList weights = readFloatBuffer(MeshAttributeSemantic.Weights_0, gltfPrimitive, gltf, loadedBuffers);

            List<BoneWeight> boneWeights = new ArrayList<>();
            for (int index = 0; index < positions.size(); index++) {
                TIntList weightJoints = new TIntArrayList();
                TFloatList weightBiases = new TFloatArrayList();
                for (int i = 0; i < 4; i++) {
                    if (weights.get(4 * index + i) > 0) {
                        weightBiases.add(weights.get(4 * index + i));
                        weightJoints.add(joints.get(4 * index + i));
                    }
                }
                boneWeights.add(new BoneWeight(weightBiases, weightJoints));
            }


//
//            builder.addVertices(positions);
//            builder.addNormals(normals);
//            builder.addWeights(boneWeights);
//            builder.setUvs(loadVector2fList(MeshAttributeSemantic.Texcoord_0, gltfPrimitive, gltf, loadedBuffers));

            GLTFAccessor indicesAccessor = getIndicesAccessor(gltfPrimitive, gltf, urn);
            if (indicesAccessor.getBufferView() == null) {
                throw new IOException("Missing buffer view for indices accessor in " + urn);
            }
            GLTFBufferView indicesBuffer = gltf.getBufferViews().get(indicesAccessor.getBufferView());
            checkIndicesBuffer(indicesBuffer);
            TIntList indicies = new TIntArrayList();
            readBuffer(loadedBuffers.get(indicesBuffer.getBuffer()), indicesAccessor, indicesBuffer, indicies);
//            builder.setIndices(indicies);

            AABBf bounds = new AABBf();
            for(Vector3f pos: positions) {
                bounds.union(pos);
            }


            if (gltf.getSkins().isEmpty()) {
                throw new IOException("Skeletal mesh '" + urn + "' missing skin");
            }

            return new SkeletalMeshData(bones, positions, normals, boneWeights,
                    loadVector2fList(MeshAttributeSemantic.Texcoord_0, gltfPrimitive, gltf, loadedBuffers), indicies, bounds);

        }
    }

}
