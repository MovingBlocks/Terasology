/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.assets.mesh;

import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

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

            MeshData data = processData(rawVertices, rawNormals, rawTexCoords, rawIndices);

            if (data.getVertices() == null) {
                throw new IOException("No vertices define");
            }
            if (!data.getNormals().isEmpty() && data.getNormals().size() != data.getVertices().size()) {
                throw new IOException("The number of normals does not match the number of vertices.");
            }
            if (!data.getTexCoord0().isEmpty() && data.getTexCoord0().size() / 2 != data.getVertices().size() / 3) {
                throw new IOException("The number of tex coords does not match the number of vertices.");
            }

            return data;
        }
    }

    private MeshData processData(List<Vector3f> rawVertices, List<Vector3f> rawNormals, List<Vector2f> rawTexCoords, List<Vector3i[]> rawIndices) throws IOException {
        MeshData result = new MeshData();
        TFloatList vertices = result.getVertices();
        TFloatList texCoord0 = result.getTexCoord0();
        TFloatList normals = result.getNormals();
        TIntList indices = result.getIndices();
        int vertCount = 0;
        for (Vector3i[] face : rawIndices) {
            for (Vector3i indexSet : face) {
                if (indexSet.x > rawVertices.size()) {
                    throw new IOException("Vertex index out of range: " + indexSet.x);
                }
                Vector3f vertex = rawVertices.get(indexSet.x - 1);
                vertices.add(vertex.x);
                vertices.add(vertex.y);
                vertices.add(vertex.z);

                if (indexSet.y != -1) {
                    if (indexSet.y > rawTexCoords.size()) {
                        throw new IOException("TexCoord index out of range: " + indexSet.y);
                    }
                    Vector2f texCoord = rawTexCoords.get(indexSet.y - 1);
                    texCoord0.add(texCoord.x);
                    texCoord0.add(1 - texCoord.y);
                }

                if (indexSet.z != -1) {
                    if (indexSet.z > rawNormals.size()) {
                        throw new IOException("Normal index out of range: " + indexSet.z);
                    }
                    Vector3f normal = rawNormals.get(indexSet.z - 1);
                    normals.add(normal.x);
                    normals.add(normal.y);
                    normals.add(normal.z);
                }
            }

            for (int i = 0; i < face.length - 2; ++i) {
                indices.add(vertCount);
                indices.add(vertCount + i + 1);
                indices.add(vertCount + i + 2);
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
                    throw new IOException(String.format("Incomplete statement"));
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
