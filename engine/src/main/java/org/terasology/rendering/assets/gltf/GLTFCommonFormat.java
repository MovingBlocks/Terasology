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
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.AssetData;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.management.AssetManager;
import org.terasology.math.MatrixUtils;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.gltf.deserializers.*;
import org.terasology.rendering.assets.gltf.model.*;
import org.terasology.rendering.assets.skeletalmesh.Bone;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public abstract class GLTFCommonFormat<T extends AssetData> extends AbstractAssetFileFormat<T> {

    private static final Logger logger = LoggerFactory.getLogger(GLTFCommonFormat.class);

    private static final String DATA_APPLICATION_OCTET_STREAM_BASE_64 = "data:application/octet-stream;base64,";
    private static final String DATA_APPLICATION_GLTF_BUFFER_BASE_64 = "data:application/gltf-buffer;base64,";
    private static final GLTFVersion SUPPORTED_VERSION = new GLTFVersion(2, 0);

    protected AssetManager assetManager;

    protected Gson gson = new GsonBuilder()
            .registerTypeAdapter(GLTFVersion.class, new GLTFVersionDeserializer())
            .registerTypeAdapter(TIntList.class, new TIntListDeserializer())
            .registerTypeAdapter(TFloatList.class, new TFloatListDeserializer())
            .registerTypeAdapter(Matrix4f.class, new Matrix4fDeserializer())
            .registerTypeAdapter(Quat4f.class, new Quat4fDeserializer())
            .registerTypeAdapter(Vector3f.class, new Vector3fDeserializer())
            .registerTypeAdapter(GLTFComponentType.class, new GLTFComponentTypeDeserializer())
            .registerTypeAdapter(GLTFMode.class, new GLTFModeDeserializer())
            .registerTypeAdapter(GLTFTargetBuffer.class, new GLTFTargetBufferDeserializer())
            .create();

    public GLTFCommonFormat(AssetManager assetManager, String fileExtension, String... fileExtensions) {
        super(fileExtension, fileExtensions);
        this.assetManager = assetManager;
    }

    protected void readBuffer(byte[] bytes, GLTFAccessor accessor, GLTFBufferView bufferView, TIntList target) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, bufferView.getByteOffset(), bufferView.getByteLength());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        switch (accessor.getComponentType()) {
            case UNSIGNED_BYTE:
                while (byteBuffer.position() < byteBuffer.limit()) {
                    target.add(UnsignedBytes.toInt(byteBuffer.get()));
                }
                break;
            case UNSIGNED_SHORT:
                while (byteBuffer.position() < byteBuffer.limit()) {
                    short rawShort = byteBuffer.getShort();
                    target.add(rawShort >= 0 ? rawShort : 0x10000 + rawShort);
                }
                break;
            case UNSIGNED_INT:
                while (byteBuffer.position() < byteBuffer.limit()) {
                    target.add(byteBuffer.getInt());
                }
                break;
        }
    }

    protected void checkIndicesBuffer(GLTFBufferView indicesBuffer) throws IOException {
        if (indicesBuffer.getTarget() != null && indicesBuffer.getTarget() != GLTFTargetBuffer.ELEMENT_ARRAY_BUFFER) {
            throw new IOException("Invalid buffer view for indices, should target an ELEMENT_ARRAY_BUFFER");
        }
    }

    protected GLTFAccessor getIndicesAccessor(GLTFPrimitive gltfPrimitive, GLTF gltf, ResourceUrn urn) throws IOException {
        if (gltfPrimitive.getIndices() == null) {
            throw new IOException("Primitives without indicies not supported, failed to load " + urn);
        }
        GLTFAccessor gltfAccessor = gltf.getAccessors().get(gltfPrimitive.getIndices());
        if (!gltfAccessor.getComponentType().isValidForIndices() || gltfAccessor.getType() != GLTFAttributeType.SCALAR) {
            throw new IOException("Invalid accessor for indices");
        }
        return gltfAccessor;
    }

    protected GLTFAccessor getAccessor(MeshAttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf) throws
            IOException {
        Integer accessorIndex = gltfPrimitive.getAttributes().get(semantic.getName());
        if (accessorIndex != null) {
            GLTFAccessor gltfAccessor = gltf.getAccessors().get(accessorIndex);
            if (!semantic.getSupportedAccessorTypes().contains(gltfAccessor.getType())) {
                throw new IOException("Accessor type " + gltfAccessor.getType() + " is not supported for attribute " + semantic);
            }
            if (!semantic.getSupportedComponentTypes().contains(gltfAccessor.getComponentType())) {
                throw new IOException("Component type " + gltfAccessor.getComponentType() + " is not supported for attribute " + semantic);
            }
            return gltfAccessor;
        }
        return null;
    }

    protected List<byte[]> loadBinaryBuffers(ResourceUrn urn, GLTF gltf) throws IOException {
        List<byte[]> loadedBuffers = Lists.newArrayList();
        for (GLTFBuffer buffer : gltf.getBuffers()) {
            String uri = buffer.getUri();
            if (uri.startsWith(DATA_APPLICATION_OCTET_STREAM_BASE_64)) {
                uri = uri.substring(DATA_APPLICATION_OCTET_STREAM_BASE_64.length());
                byte[] data = BaseEncoding.base64().decode(uri);
                if (data.length != buffer.getByteLength()) {
                    throw new IOException("Byte buffer " + uri + " has incorrect length. Expected (" + buffer.getByteLength() + "), actual (" + data.length + ")");
                }
                loadedBuffers.add(data);
            } else if (uri.startsWith(DATA_APPLICATION_GLTF_BUFFER_BASE_64)) {
                uri = uri.substring(DATA_APPLICATION_GLTF_BUFFER_BASE_64.length());
                byte[] data = BaseEncoding.base64().decode(uri);
                if (data.length != buffer.getByteLength()) {
                    throw new IOException("Byte buffer " + uri + " has incorrect length. Expected (" + buffer.getByteLength() + "), actual (" + data.length + ")");
                }
                loadedBuffers.add(data);
            } else {
                if (uri.endsWith(".bin")) {
                    uri = uri.substring(0, uri.length() - 4);
                }
                ByteBufferAsset bufferAsset = assetManager.getAsset(uri, ByteBufferAsset.class, urn.getModuleName()).orElseThrow(() -> new IOException("Failed to resolve binary uri " + buffer.getUri() + " for " + urn));
                if (bufferAsset.getBytes().length != buffer.getByteLength()) {
                    throw new IOException("Byte buffer " + uri + " has incorrect length. Expected (" + buffer.getByteLength() + "), actual (" + bufferAsset.getBytes().length + ")");
                }
                loadedBuffers.add(bufferAsset.getBytes());
            }
        }
        return loadedBuffers;
    }

    protected void checkPrimitivePresent(ResourceUrn urn, GLTFMesh gltfMesh) throws IOException {
        if (gltfMesh.getPrimitives() == null || gltfMesh.getPrimitives().isEmpty()) {
            throw new IOException("No primitives found in gltf mesh for " + urn);
        }
        if (gltfMesh.getPrimitives().size() > 1) {
            logger.warn("Multiple primitives found in gltf file for {} - only first primitive will be loaded", urn);
        }
    }

    protected void checkMeshPresent(ResourceUrn urn, GLTF gltf) throws IOException {
        if (gltf.getMeshes() == null || gltf.getMeshes().isEmpty()) {
            throw new IOException("No mesh found in gltf file for " + urn);
        }
        if (gltf.getMeshes().size() > 1) {
            logger.warn("Multiple mesh found in gltf file for {} - only first mesh will be loaded", urn);
        }
    }

    protected void readBuffer(byte[] buffer, GLTFAccessor accessor, GLTFBufferView bufferView, TFloatList floatList) {
        if (accessor.getComponentType() != GLTFComponentType.FLOAT) {
            return;
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, bufferView.getByteOffset() + accessor.getByteOffset(), bufferView.getByteLength() - accessor.getByteOffset());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int gap = 0;
        if (bufferView.getByteStride() > 0) {
            gap = bufferView.getByteStride() - accessor.getComponentType().getByteLength() * accessor.getType().getDimension();
        }

        while (byteBuffer.position() < byteBuffer.limit()) {
            floatList.add(byteBuffer.getFloat());
            byteBuffer.position(byteBuffer.position() + gap);
        }
    }
    protected void checkVersionSupported(ResourceUrn urn, GLTF gltf) throws IOException {
        if (gltf.getAsset().getMinVersion() != null && (gltf.getAsset().getMinVersion().getMajor() != SUPPORTED_VERSION.getMajor() || gltf.getAsset().getMinVersion().getMinor() > SUPPORTED_VERSION.getMinor())) {
            throw new IOException("Cannot read gltf for " + urn + " as gltf version " + gltf.getAsset().getMinVersion() + " is not supported");
        } else if (gltf.getAsset().getVersion().getMajor() != SUPPORTED_VERSION.getMajor()) {
            throw new IOException("Cannot read gltf for " + urn + " as gltf version " + gltf.getAsset().getVersion() + " is not supported");
        }
    }

    protected TIntObjectMap<Bone> loadBones(GLTF gltf, List<byte[]> loadedBuffers) {
        GLTFSkin skin = gltf.getSkins().get(0);
        List<Matrix4f> inverseMats = loadMat4fList(skin.getInverseBindMatrices(), gltf, loadedBuffers);
        TIntObjectMap<Bone> bones = new TIntObjectHashMap<>();
        for (int i = 0; i < skin.getJoints().size(); i++) {
            int nodeIndex = skin.getJoints().get(i);
            GLTFNode node = gltf.getNodes().get(nodeIndex);
            Vector3f position = new Vector3f();
            Quat4f rotation = new Quat4f(Quat4f.IDENTITY);
            if (!inverseMats.isEmpty()) {
                inverseMats.get(i).invert();
                position = inverseMats.get(i).getTranslation();
                rotation = MatrixUtils.extractRotation(inverseMats.get(i));
            } else {
                if (node.getTranslation() != null) {
                    position = new Vector3f(node.getTranslation());
                }
                if (node.getRotation() != null) {
                    rotation = new Quat4f(node.getRotation());
                }
            }
            String boneName = node.getName();
            if (Strings.isNullOrEmpty(boneName)) {
                boneName = "bone_" + nodeIndex;
            }
            Bone bone = new Bone(nodeIndex, boneName, position, rotation);
            bones.put(nodeIndex, bone);
        }
        for (int i = 0; i < skin.getJoints().size(); i++) {
            int nodeIndex = skin.getJoints().get(i);
            GLTFNode node = gltf.getNodes().get(nodeIndex);
            Bone bone = bones.get(nodeIndex);
            TIntIterator iterator = node.getChildren().iterator();
            while (iterator.hasNext()) {
                bone.addChild(bones.get(iterator.next()));
            }
        }
        return bones;
    }

    private List<Matrix4f> loadMat4fList(int inverseBindMatrices, GLTF gltf, List<byte[]> loadedBuffers) {
        GLTFAccessor accessor = gltf.getAccessors().get(inverseBindMatrices);
        GLTFBufferView bufferView = gltf.getBufferViews().get(accessor.getBufferView());
        byte[] buffer = loadedBuffers.get(bufferView.getBuffer());
        TFloatList values = new TFloatArrayList();
        readBuffer(buffer, accessor, bufferView, values);
        List<Matrix4f> matricies = Lists.newArrayList();
        for (int i = 0; i < values.size(); i += 16) {
            Matrix4f mat = new Matrix4f(
                    values.get(i), values.get(i + 1), values.get(i + 2), values.get(i + 3),
                    values.get(i + 4), values.get(i + 5), values.get(i + 6), values.get(i + 7),
                    values.get(i + 8), values.get(i + 9), values.get(i + 10), values.get(i + 11),
                    values.get(i + 12), values.get(i + 13), values.get(i + 14), values.get(i + 15)
            );
            matricies.add(mat);
        }
        return matricies;
    }
}
