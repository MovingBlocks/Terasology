package org.terasology.asset.loaders;

import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.math.Vector3i;
import org.terasology.rendering.primitives.Mesh;

import javax.vecmath.Tuple3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

/**
 * Importer for Wavefront obj files. Supports core obj mesh data
 * @author Immortius <immortius@gmail.com>
 */

public class ObjMeshLoader implements AssetLoader<Mesh> {

    private Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public Mesh load(InputStream stream, AssetUri uri, List<URL> urls) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        List<Vector3f> rawVertices = Lists.newArrayList();
        List<Vector3f> rawNormals = Lists.newArrayList();
        List<Vector2f> rawTexCoords = Lists.newArrayList();
        List<Tuple3i[]> rawIndices = Lists.newArrayList();

        // Gather data
        readMeshData(reader, rawVertices, rawNormals, rawTexCoords, rawIndices);

        // Process data
        TFloatList vertices = new TFloatArrayList();
        TFloatList texCoord0 = new TFloatArrayList();
        TFloatList normals = new TFloatArrayList();
        TIntList indices = new TIntArrayList();

        // Determine face format;
        if (rawIndices.size() == 0) {
            throw new IOException("No index data");
        }

        processData(rawVertices, rawNormals, rawTexCoords, rawIndices, vertices, texCoord0, normals, indices);

        if (normals.size() != vertices.size() || texCoord0.size() / 2 != vertices.size() / 3) {
            throw new IOException("Mixed face format");
        }

        return Mesh.buildMesh(uri, vertices, texCoord0, null, normals, null, indices);
    }

    private void processData(List<Vector3f> rawVertices, List<Vector3f> rawNormals, List<Vector2f> rawTexCoords, List<Tuple3i[]> rawIndices, TFloatList vertices, TFloatList texCoord0, TFloatList normals, TIntList indices) throws IOException {
        int vertCount = 0;
        for (Tuple3i[] face : rawIndices) {
            for (Tuple3i indexSet : face) {
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
                    texCoord0.add(texCoord.y);
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
    }

    private void readMeshData(BufferedReader reader, List<Vector3f> rawVertices, List<Vector3f> rawNormals, List<Vector2f> rawTexCoords, List<Tuple3i[]> rawIndices) throws IOException {
        String line = null;
        int lineNum = 0;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                lineNum++;
                if (line.isEmpty())
                    continue;
                String[] prefixSplit = line.trim().split("\\s+", 2);
                String prefix = prefixSplit[0];

                // Comment
                if ("#".equals(prefix))
                    continue;

                if (prefixSplit.length < 2) {
                    throw new IOException(String.format("Incomplete statement"));
                }

                // JAVA7: Replace with switch
                // Object name
                if ("o".equals(prefix)) {
                    // Just skip the name
                }
                // Vertex position
                else if ("v".equals(prefix)) {
                    String[] floats = prefixSplit[1].trim().split("\\s+", 4);
                    if (floats.length != 3) {
                        throw new IOException("Bad statement");
                    }
                    rawVertices.add(new Vector3f(Float.parseFloat(floats[0]), Float.parseFloat(floats[1]), Float.parseFloat(floats[2])));
                }
                // Vertex texture coords
                else if ("vt".equals(prefix)) {
                    String[] floats = prefixSplit[1].trim().split("\\s+", 4);
                    if (floats.length < 2 || floats.length > 3) {
                        throw new IOException("Bad statement");
                    }
                    // Need to flip v coord, apparently
                    rawTexCoords.add(new Vector2f(Float.parseFloat(floats[0]), 1- Float.parseFloat(floats[1])));
                }
                // Vertex normal
                else if ("vn".equals(prefix)) {
                    String[] floats = prefixSplit[1].trim().split("\\s+", 4);
                    if (floats.length != 3) {
                        throw new IOException("Bad statement");
                    }
                    rawNormals.add(new Vector3f(Float.parseFloat(floats[0]), Float.parseFloat(floats[1]), Float.parseFloat(floats[2])));
                }
                // Material name (ignored)
                else if ("usemtl".equals(prefix)) {
                    continue;
                }
                // Smoothing group (not supported)
                else if ("s".equals(prefix)) {
                    if (!"off".equals(prefixSplit[1]) && !"0".equals(prefixSplit[1])) {
                        logger.warning("Smoothing groups not supported in obj import yet");
                    }
                }
                // Face (polygon)
                else if ("f".equals(prefix)) {
                    String[] elements = prefixSplit[1].trim().split("\\s+");
                    Tuple3i[] result = new Tuple3i[elements.length];
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
                }
                else {
                    logger.warning(String.format("Skipping unsupported obj statement on line %d:\"%s\"", lineNum, line));
                }
            }
        } catch (Exception e) {
            throw new IOException(String.format("Failed to process line %d:\"%s\"", lineNum, line), e);
        }
    }

}
