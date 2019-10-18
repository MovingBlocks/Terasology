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

import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import org.joml.Vector4i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.geom.*;
import org.terasology.rendering.gltf.model.*;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.BoneWeight;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshDataBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
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
            GLTFMesh gltfMesh = gltf.getMeshes().get(0);

            checkPrimitivePresent(urn, gltfMesh);
            GLTFPrimitive gltfPrimitive = gltfMesh.getPrimitives().get(0);

            List<byte[]> loadedBuffers = loadBinaryBuffers(urn, gltf);

            SkeletalMeshDataBuilder builder = new SkeletalMeshDataBuilder();

            List<Vector3f> positions = loadVector3fList(MeshAttributeSemantic.Position, gltfPrimitive, gltf, loadedBuffers);
            List<Vector3f> normals = loadVector3fList(MeshAttributeSemantic.Normal, gltfPrimitive, gltf, loadedBuffers);
            TIntList joints = readIntBuffer(MeshAttributeSemantic.Joints_0, gltfPrimitive, gltf, loadedBuffers);
            TFloatList weights = readFloatBuffer(MeshAttributeSemantic.Weights_0, gltfPrimitive, gltf, loadedBuffers);

            TIntList vertexStartWeights = new TIntArrayList();
            TIntList vertexWeightCounts = new TIntArrayList();
            int weightCount = 0;
            for (int index = 0; index < positions.size(); index++) {
                vertexStartWeights.add(weightCount);
                int weightsAdded = 0;
                for (int i = 0; i < 4; i++) {
                    if (weights.get(4 * index + i) > 0) {
                        BoneWeight boneWeight = new BoneWeight(positions.get(index), weights.get(4 * index + i), joints.get(4 * index + i));
                        // TODO: Support missing normals
                        if (!normals.isEmpty()) {
                            boneWeight.setNormal(normals.get(index));
                        }
                        builder.addWeight(boneWeight);
                        weightsAdded++;
                        weightCount++;
                    }
                }
                vertexWeightCounts.add(weightsAdded);
            }
            builder.setVertexWeights(vertexStartWeights, vertexWeightCounts);

            List<Vector2f> uvs = loadVector2fList(MeshAttributeSemantic.Texcoord_0, gltfPrimitive, gltf, loadedBuffers);
            builder.setUvs(uvs);

            GLTFAccessor indicesAccessor = getIndicesAccessor(gltfPrimitive, gltf, urn);
            if (indicesAccessor.getBufferView() == null) {
                throw new IOException("Missing buffer view for indices accessor in " + urn);
            }
            GLTFBufferView indicesBuffer = gltf.getBufferViews().get(indicesAccessor.getBufferView());
            checkIndicesBuffer(indicesBuffer);
            TIntList indicies = new TIntArrayList();
            readBuffer(loadedBuffers.get(indicesBuffer.getBuffer()), indicesAccessor, indicesBuffer, indicies);
            builder.setIndices(indicies);

            if (gltf.getSkins().isEmpty()) {
                throw new IOException("Skeletal mesh '" + urn + "' missing skin");
            }
            TIntObjectMap<Bone> bones = loadBones(gltf, loadedBuffers);

            bones.forEachValue(bone -> {
                builder.addBone(bone);
                return true;
            });

            List<Matrix4f> inverseMats = loadInverseMats(gltf.getSkins().get(0).getInverseBindMatrices(), gltf.getSkins().get(0).getJoints().size(), gltf, loadedBuffers);
            for (int i = 0; i < gltf.getSkins().get(0).getJoints().size(); i++) {
                int jointIndex = gltf.getSkins().get(0).getJoints().get(i);
                bones.get(jointIndex).setInverseBindMatrix(inverseMats.get(i));
            }

            return builder.build();
        }
    }

}
