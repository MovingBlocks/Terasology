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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.joml.Vector4i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.MatrixUtils;
import org.terasology.math.geom.*;
import org.terasology.rendering.assets.gltf.model.*;
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

            return builder.build();
        }
    }

    private List<Vector4i> loadVector4iList(MeshAttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf, List<byte[]> loadedBuffers) throws IOException {
        TIntList values = readIntBuffer(semantic, gltfPrimitive, gltf, loadedBuffers);
        List<Vector4i> vectors = Lists.newArrayListWithCapacity(values.size() / 4);
        for (int i = 0; i < values.size(); i += 4) {
            vectors.add(new Vector4i(values.get(i), values.get(i + 1), values.get(i + 2), values.get(i + 3)));
        }
        return vectors;
    }

    private List<Vector2f> loadVector2fList(MeshAttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf, List<byte[]> loadedBuffers) throws IOException {
        TFloatList floats = readFloatBuffer(semantic, gltfPrimitive, gltf, loadedBuffers);

        List<Vector2f> vectors = Lists.newArrayListWithCapacity(floats.size() / 2);
        for (int i = 0; i < floats.size(); i += 2) {
            vectors.add(new Vector2f(floats.get(i), floats.get(i + 1)));
        }
        return vectors;
    }

    private List<Vector3f> loadVector3fList(MeshAttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf, List<byte[]> loadedBuffers) throws IOException {
        TFloatList floats = readFloatBuffer(semantic, gltfPrimitive, gltf, loadedBuffers);
        List<Vector3f> vectors = Lists.newArrayListWithCapacity(floats.size() / 3);
        for (int i = 0; i < floats.size(); i += 3) {
            vectors.add(new Vector3f(floats.get(i), floats.get(i + 1), floats.get(i + 2)));
        }
        return vectors;
    }

    private List<Vector4f> loadVector4fList(MeshAttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf, List<byte[]> loadedBuffers) throws IOException {
        TFloatList floats = readFloatBuffer(semantic, gltfPrimitive, gltf, loadedBuffers);
        List<Vector4f> vectors = Lists.newArrayListWithCapacity(floats.size() / 4);
        for (int i = 0; i < floats.size(); i += 4) {
            vectors.add(new Vector4f(floats.get(i), floats.get(i + 1), floats.get(i + 2), floats.get(i + 3)));
        }
        return vectors;
    }

    private TFloatList readFloatBuffer(MeshAttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf, List<byte[]> loadedBuffers) throws IOException {
        GLTFAccessor gltfAccessor = getAccessor(semantic, gltfPrimitive, gltf);
        if (gltfAccessor != null && gltfAccessor.getBufferView() != null) {
            GLTFBufferView bufferView = gltf.getBufferViews().get(gltfAccessor.getBufferView());
            TFloatList floats = new TFloatArrayList();
            readBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, floats);

            return floats;
        }
        throw new IOException("Cannot load skeletal mesh without " + semantic);
    }

    private TIntList readIntBuffer(MeshAttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf, List<byte[]> loadedBuffers) throws IOException {
        GLTFAccessor gltfAccessor = getAccessor(semantic, gltfPrimitive, gltf);
        if (gltfAccessor != null && gltfAccessor.getBufferView() != null) {
            GLTFBufferView bufferView = gltf.getBufferViews().get(gltfAccessor.getBufferView());
            TIntList ints = new TIntArrayList();
            readBuffer(loadedBuffers.get(bufferView.getBuffer()), gltfAccessor, bufferView, ints);

            return ints;
        }
        throw new IOException("Cannot load skeletal mesh without " + semantic);
    }
}
