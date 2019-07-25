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
package org.terasology.rendering.assets.mesh.gltf;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.geom.ImmutableMatrix4f;
import org.terasology.math.geom.ImmutableQuat4f;
import org.terasology.math.geom.ImmutableVector3f;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.assets.mesh.gltf.deserializers.*;
import org.terasology.rendering.assets.mesh.gltf.model.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
public class GLTFMeshFormat extends AbstractAssetFileFormat<MeshData> {

    private static final GLTFVersion SUPPORTED_VERSION = new GLTFVersion(2, 0);
    public static final String DATA_APPLICATION_OCTET_STREAM_BASE_64 = "data:application/octet-stream;base64,";

    private Logger logger = LoggerFactory.getLogger(GLTFMeshFormat.class);

    private AssetManager assetManager;

    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(GLTFVersion.class, new GLTFVersionDeserializer())
            .registerTypeAdapter(TIntList.class, new TIntListDeserializer())
            .registerTypeAdapter(TFloatList.class, new TFloatListDeserializer())
            .registerTypeAdapter(ImmutableMatrix4f.class, new ImmutableMatrix4fDeserializer())
            .registerTypeAdapter(ImmutableQuat4f.class, new ImmutableQuat4fDeserializer())
            .registerTypeAdapter(ImmutableVector3f.class, new ImmutableVector3fDeserializer())
            .registerTypeAdapter(GLTFComponentType.class, new GLTFComponentTypeDeserializer())
            .registerTypeAdapter(GLTFMode.class, new GLTFModeDeserializer())
            .registerTypeAdapter(GLTFTargetBuffer.class, new GLTFTargetBufferDeserializer())
            .create();

    public GLTFMeshFormat(AssetManager assetManager) {
        super("gltf");
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
            for (AttributeSemantic semantic : AttributeSemantic.values()) {
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

    private void readBuffer(byte[] bytes, GLTFAccessor accessor, GLTFBufferView bufferView, TIntList target) {
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

    private void checkIndicesBuffer(GLTFBufferView indicesBuffer) throws IOException {
        if (indicesBuffer.getTarget() != null && indicesBuffer.getTarget() != GLTFTargetBuffer.ELEMENT_ARRAY_BUFFER) {
            throw new IOException("Invalid buffer view for indices, should target an ELEMENT_ARRAY_BUFFER");
        }
    }

    private GLTFAccessor getIndicesAccessor(GLTFPrimitive gltfPrimitive, GLTF gltf, ResourceUrn urn) throws IOException {
        if (gltfPrimitive.getIndices() == null) {
            throw new IOException("Primitives without indicies not supported, failed to load " + urn);
        }
        GLTFAccessor gltfAccessor = gltf.getAccessors().get(gltfPrimitive.getIndices());
        if (!gltfAccessor.getComponentType().isValidForIndices() || gltfAccessor.getType() != GLTFAttributeType.SCALAR) {
            throw new IOException("Invalid accessor for indices");
        }
        return gltfAccessor;
    }

    private GLTFAccessor getAccessor(AttributeSemantic semantic, GLTFPrimitive gltfPrimitive, GLTF gltf) throws
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

    private List<byte[]> loadBinaryBuffers(ResourceUrn urn, GLTF gltf) throws IOException {
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

    private void checkPrimitivePresent(ResourceUrn urn, GLTFMesh gltfMesh) throws IOException {
        if (gltfMesh.getPrimitives() == null || gltfMesh.getPrimitives().isEmpty()) {
            throw new IOException("No primitives found in gltf mesh for " + urn);
        }
        if (gltfMesh.getPrimitives().size() > 1) {
            logger.warn("Multiple primitives found in gltf file for {} - only first primitive will be loaded", urn);
        }
    }

    private void checkMeshPresent(ResourceUrn urn, GLTF gltf) throws IOException {
        if (gltf.getMeshes() == null || gltf.getMeshes().isEmpty()) {
            throw new IOException("No mesh found in gltf file for " + urn);
        }
        if (gltf.getMeshes().size() > 1) {
            logger.warn("Multiple mesh found in gltf file for {} - only first mesh will be loaded", urn);
        }
    }

    private void readBuffer(byte[] buffer, GLTFAccessor accessor, GLTFBufferView bufferView, TFloatList floatList) {
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

    private void checkVersionSupported(ResourceUrn urn, GLTF gltf) throws IOException {
        if (gltf.getAsset().getMinVersion() != null && (gltf.getAsset().getMinVersion().getMajor() != SUPPORTED_VERSION.getMajor() || gltf.getAsset().getMinVersion().getMinor() > SUPPORTED_VERSION.getMinor())) {
            throw new IOException("Cannot read gltf for " + urn + " as gltf version " + gltf.getAsset().getMinVersion() + " is not supported");
        } else if (gltf.getAsset().getVersion().getMajor() != SUPPORTED_VERSION.getMajor()) {
            throw new IOException("Cannot read gltf for " + urn + " as gltf version " + gltf.getAsset().getVersion() + " is not supported");
        }
    }
}
