// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import com.google.common.collect.Lists;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Importer for Wavefront obj files. Supports core obj mesh data
 *
 */
@RegisterAssetFileFormat
public class ObjMeshFormat extends AbstractAssetFileFormat<MeshData> {

    private static final Logger logger = LoggerFactory.getLogger(ObjMeshFormat.class);

    public ObjMeshFormat() {
        super("obj");
    }

    @Override
    public MeshData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputs.get(0).openStream()))) {

            List<Vector3f> rawVertices = Lists.newArrayList();
            List<Vector3f> rawNormals = Lists.newArrayList();
            List<Vector2f> rawTexCoords = Lists.newArrayList();
            List<Vector3i[]> rawIndices = Lists.newArrayList();

            // Gather data
            readMeshData(reader, rawVertices, rawNormals, rawTexCoords, rawIndices);

            // Determine face format;
            if (rawIndices.size() == 0) {
                throw new IOException("No index data");
            }

            return processData(rawVertices, rawNormals, rawTexCoords, rawIndices);
        }
    }

    private StandardMeshData processData(List<Vector3f> rawVertices, List<Vector3f> rawNormals,
                                         List<Vector2f> rawTexCoords, List<Vector3i[]> rawIndices) throws IOException {
        int numIndices = 0;
        int numVerts = 0;
        for (Vector3i[] rawIndex : rawIndices) {
            numIndices += (rawIndex.length - 2) * 3;
            numVerts += rawIndex.length;
        }

        StandardMeshData result = new StandardMeshData();
        result.reserve(numVerts, numIndices);
        int vertCount = 0;
        for (Vector3i[] face : rawIndices) {
            for (Vector3i indexSet : face) {
                if (indexSet.x > rawVertices.size()) {
                    throw new IOException("Vertex index out of range: " + indexSet.x);
                }
                Vector3f vertex = rawVertices.get(indexSet.x - 1);
                result.position.put(vertex);

                if (indexSet.y != -1) {
                    if (indexSet.y > rawTexCoords.size()) {
                        throw new IOException("TexCoord index out of range: " + indexSet.y);
                    }
                    Vector2f texCoord = rawTexCoords.get(indexSet.y - 1);
                    result.uv0.put(new Vector2f(texCoord.x, 1 - texCoord.y));
                }

                if (indexSet.z != -1) {
                    if (indexSet.z > rawNormals.size()) {
                        throw new IOException("Normal index out of range: " + indexSet.z);
                    }
                    Vector3f normal = rawNormals.get(indexSet.z - 1);
                    result.normal.put(normal);
                }
            }

            for (int i = 0; i < face.length - 2; ++i) {
                result.indices.put(vertCount);
                result.indices.put(vertCount + i + 1);
                result.indices.put(vertCount + i + 2);
            }
            vertCount += face.length;
        }
        return result;
    }

    private void readMeshData(BufferedReader reader, List<Vector3f> rawVertices, List<Vector3f> rawNormals,
                              List<Vector2f> rawTexCoords, List<Vector3i[]> rawIndices) throws IOException {
        String line = null;
        int lineNum = 0;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lineNum++;
                if (line.isEmpty()) {
                    continue;
                }
                String[] prefixSplit = line.trim().split("\\s+", 2);
                String prefix = prefixSplit[0];

                // Comment
                if ("#".equals(prefix)) {
                    continue;
                }

                if (prefixSplit.length < 2) {
                    throw new IOException("Incomplete statement");
                }

                switch (prefix) {
                    // Object name
                    case "o":
                        // Just skip the name
                        break;

                    // Vertex position
                    case "v": {
                        String[] floats = prefixSplit[1].trim().split("\\s+", 4);
                        if (floats.length != 3) {
                            throw new IOException("Bad statement");
                        }
                        rawVertices.add(new Vector3f(Float.parseFloat(floats[0]), Float.parseFloat(floats[1]), Float.parseFloat(floats[2])));
                        break;
                    }
                    // Vertex texture coords
                    case "vt": {
                        String[] floats = prefixSplit[1].trim().split("\\s+", 4);
                        if (floats.length < 2 || floats.length > 3) {
                            throw new IOException("Bad statement");
                        }
                        // Need to flip v coord, apparently
                        rawTexCoords.add(new Vector2f(Float.parseFloat(floats[0]), Float.parseFloat(floats[1])));
                        break;
                    }
                    // Vertex normal
                    case "vn": {
                        String[] floats = prefixSplit[1].trim().split("\\s+", 4);
                        if (floats.length != 3) {
                            throw new IOException("Bad statement");
                        }
                        rawNormals.add(new Vector3f(Float.parseFloat(floats[0]), Float.parseFloat(floats[1]), Float.parseFloat(floats[2])));
                        break;
                    }
                    // Material name (ignored)
                    case "usemtl":
                        break;
                    // Smoothing group (not supported)
                    case "s": {
                        if (!"off".equals(prefixSplit[1]) && !"0".equals(prefixSplit[1])) {
                            logger.warn("Smoothing groups not supported in obj import yet");
                        }
                        break;
                    }
                    // Face (polygon)
                    case "f": {
                        String[] elements = prefixSplit[1].trim().split("\\s+");
                        Vector3i[] result = new Vector3i[elements.length];
                        for (int i = 0; i < elements.length; ++i) {
                            String[] parts = elements[i].split("/", 4);
                            if (parts.length > 3) {
                                throw new IOException("Bad Statement");
                            }
                            result[i] = new Vector3i(Integer.parseInt(parts[0]), -1, -1);
                            if (parts.length > 1 && !parts[1].isEmpty()) {
                                result[i].y = Integer.parseInt(parts[1]);
                            }
                            if (parts.length > 2 && !parts[2].isEmpty()) {
                                result[i].z = Integer.parseInt(parts[2]);
                            }
                        }
                        rawIndices.add(result);
                        break;
                    }
                    default:
                        logger.warn("Skipping unsupported obj statement on line {}:\"{}\"", lineNum, line);
                }
            }
        } catch (RuntimeException e) {
            throw new IOException(String.format("Failed to process line %d:\"%s\"", lineNum, line), e);
        }
    }

}
