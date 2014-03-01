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

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.engine.module.Module;

import com.google.common.base.Charsets;

/**
 * Importer for Collada data exchange model files.  Supports mesh data
 * 
 * The development of this loader was greatly influenced by 
 * http://www.wazim.com/Collada_Tutorial_1.htm
 *
 * @author mkienenb@gmail.com
 */

public class ColladaLoader implements AssetLoader<MeshData> {

    private static final Logger logger = LoggerFactory.getLogger(ColladaLoader.class);

    public static void main(String[] args) {
        ColladaLoader loader = new ColladaLoader();
        try {
            String contents = loadDataAsString(new File("/home/mkienenb/workspaces/keplar-Terasology/ParseCollada/Dwarf_crowd.dae.xml"));
            loader.parseMeshData(contents);
        } catch (IOException | ColladaParseException e) {
            e.printStackTrace();
        }
    }

    private static String loadDataAsString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file), 1024);
        if (file.getName().endsWith(".gz")) {
            reader.close();
            reader = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file))), 1024);
        }

        return loadDataAsString(reader);
    }

    private static String loadDataAsString(BufferedReader reader) throws IOException, FileNotFoundException {
        StringBuilder result = new StringBuilder();
        try {
            int c;
            while ((c = reader.read()) != -1) {
                result.append((char) c);
            }
        } finally {
            reader.close();
        }
        return result.toString();
    }

    private MeshData parseMeshData(String contents) throws ColladaParseException {
        Document document = Xml.xml(contents);
        Element rootElement = document.getRootElement();

        // TODO: we shouldn't just cram everything into a single mesh, but should expect separate meshes with differing materials

        MeshData result = new MeshData();
        TFloatList vertices = result.getVertices();
        TFloatList texCoord0 = result.getTexCoord0();
        TFloatList normals = result.getNormals();
        TIntList indices = result.getIndices();
        int vertCount = 0;

        ElementSet geometrySet = rootElement.find("library_geometries", "geometry");
        for (Element geometry : geometrySet) {

            ElementSet meshSet = geometry.find("mesh");

            if (1 != meshSet.size()) {
                throw new ColladaParseException("Found " + meshSet.size() + " mesh sets for geometry id=" + geometry.id() + " name=" + geometry.name());
            }

            logger.info("Parsing geometry id=" + geometry.id() + " name=" + geometry.name());

            for (Element mesh : meshSet) {

                ElementSet trianglesSet = mesh.find("triangles");
                for (Element triangles : trianglesSet) {
                    vertCount = parseTriangles(vertices, texCoord0, normals, indices, vertCount, geometry, mesh, triangles);
                }

                ElementSet polylistSet = mesh.find("polylist");
                for (Element polylist : polylistSet) {

                    ElementSet vCountSet = polylist.find("vcount");
                    if (1 != vCountSet.size()) {
                        throw new ColladaParseException("Found " + vCountSet.size() + " vcount sets for polylist in geometry id="
                                                        + geometry.id() + " name=" + geometry.name());
                    }
                    Element vCount = vCountSet.first();

                    String[] vCountStrings = getItemsInString(vCount.text());
                    for (String string : vCountStrings) {
                        if (!"3".equals(string)) {
                            throw new ColladaParseException("Found vertex count of " + string + " in polylist sets for geometry id=" + geometry.id() + " name="
                                                            + geometry.name()
                                                            + ".  polylist vertex counts other than 3 currently unsupported.  You must trianglulate the model.");
                        }
                    }

                    vertCount = parseTriangles(vertices, texCoord0, normals, indices, vertCount, geometry, mesh, polylist);
                }
            }
        }

        return result;
    }

    private int parseTriangles(TFloatList vertices, TFloatList texCoord0, TFloatList normals, TIntList indices, int vertCountParam, Element geometry, Element mesh,
                               Element triangles) throws ColladaParseException {
        int vertCount = vertCountParam;
        String triangleCountString = triangles.attr("count");
        int triangleCount = Integer.parseInt(triangleCountString);
        ElementSet triangleInputSet = triangles.find("input");
        List<Input> triangleInputs = parseInputs(triangleInputSet);

        for (Input triangleInput : triangleInputs) {
            if ("VERTEX".equals(triangleInput.semantic)) {
                ElementSet verticesSet = mesh.find("vertices");
                if (1 != verticesSet.size()) {
                    throw new ColladaParseException("Found " + verticesSet.size() + " vertices sets for geometry id=" + geometry.id() + " name=" + geometry.name());
                }
                Element verticesElement = verticesSet.first();
                ElementSet verticesInputSet = verticesElement.find("input");
                List<Input> verticesInputs = parseInputs(verticesInputSet);
                for (Input vertexInput : verticesInputs) {
                    if ("POSITION".equals(vertexInput.semantic)) {
                        Element vertexSourceElement = mesh.select(vertexInput.sourceName);
                        triangleInput.vertexPositionSource = parseSource(vertexSourceElement);
                    } else if ("NORMAL".equals(vertexInput.semantic)) {
                        Element normalSourceElement = mesh.select(vertexInput.sourceName);
                        triangleInput.vertexNormalSource = parseSource(normalSourceElement);
                    } else {
                        throw new ColladaParseException("Found unexpected vertex Input semantic " + vertexInput.semantic +
                                                        " for geometry id=" + geometry.id() + " name=" + geometry.name());
                    }
                }
            } else if ("NORMAL".equals(triangleInput.semantic)) {
                Element normalSourceElement = mesh.select(triangleInput.sourceName);
                triangleInput.normalSource = parseSource(normalSourceElement);
                if (3 != triangleInput.normalSource.stride) {
                    throw new ColladaParseException("Found stride of " + triangleInput.normalSource.stride
                                                    + " for triangle Input semantic " + triangleInput.semantic +
                                                    " for geometry id=" + geometry.id() + " name=" + geometry.name());
                }
            } else if ("TEXCOORD".equals(triangleInput.semantic)) {
                Element texCoordSourceElement = mesh.select(triangleInput.sourceName);
                triangleInput.texCoordSource = parseSource(texCoordSourceElement);
            } else {
                throw new ColladaParseException("Found unexpected triangle Input semantic " + triangleInput.semantic +
                                                " for geometry id=" + geometry.id() + " name=" + geometry.name());
            }
        }
        ElementSet triangleDataSet = triangles.find("p");
        if (1 != triangleDataSet.size()) {
            throw new ColladaParseException("Found " + triangleDataSet.size() + " triangleData sets for geometry id=" + geometry.id() + " name=" + geometry.name());
        }
        Element triangleData = triangleDataSet.first();
        String triangleDataString = triangleData.text();
        String[] trianglesStrings = getItemsInString(triangleDataString);
        if (trianglesStrings.length != (triangleCount * triangleInputs.size() * 3)) {
            throw new ColladaParseException("Expected String 3 vertices *  " + triangleCount + " * input count of " + triangleInputs.size() + " but was "
                                            + trianglesStrings.length + " for geometry id=" + geometry.id() + " name=" + geometry.name());
        }

        // TODO: for now, assume the offsets will always perfectly match the sorted-by-offset list indexes
        Collections.sort(triangleInputs, new Comparator<Input>() {
            @Override
            public int compare(Input i1, Input i2) {
                return i1.offset - i2.offset;
            }
        });
        for (int i = 0; i < triangleInputs.size(); i++) {
            Input input = triangleInputs.get(i);
            if (input.offset != i) {
                throw new ColladaParseException("Triangle input list offset does not match list index for triangle input " + input + " for geometry id=" + geometry.id()
                                                + " name=" + geometry.name());
            }
        }

        for (int triangleIndex = 0; triangleIndex < triangleCount; triangleIndex++) {
            for (int triangleVertexIndex = 0; triangleVertexIndex < 3; triangleVertexIndex++) {
                for (int triangleInputOffset = 0; triangleInputOffset < triangleInputs.size(); triangleInputOffset++) {
                    Input triangleInput = triangleInputs.get(triangleInputOffset);

                    int triangleDataIndex = (triangleIndex * triangleInputs.size() * 3) + (triangleVertexIndex * triangleInputs.size()) + triangleInputOffset;
                    String indexString = trianglesStrings[triangleDataIndex];
                    int index = Integer.parseInt(indexString);

                    if ("VERTEX".equals(triangleInput.semantic)) {
                        int vertexStride = triangleInput.vertexPositionSource.stride;
                        if (3 != vertexStride) {
                            throw new ColladaParseException("Found non-3 stride of " + triangleInput.vertexPositionSource.stride
                                                            + " for vertex Input semantic " + triangleInput.semantic +
                                                            " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                        float vertexX = triangleInput.vertexPositionSource.values[index * vertexStride + 0];
                        float vertexY = triangleInput.vertexPositionSource.values[index * vertexStride + 1];
                        float vertexZ = triangleInput.vertexPositionSource.values[index * vertexStride + 2];
                        vertices.add(vertexX);
                        vertices.add(vertexY);
                        vertices.add(vertexZ);

                        // TODO: Sometimes we get the normal attached to the triangle, sometimes to the vertex
                        if (null != triangleInput.vertexNormalSource) {
                            int normalStride = triangleInput.vertexNormalSource.stride;
                            if (3 != normalStride) {
                                throw new ColladaParseException("Found non-3 stride of " + triangleInput.vertexNormalSource.stride
                                                                + " for vertex Input semantic " + triangleInput.semantic +
                                                                " for geometry id=" + geometry.id() + " name=" + geometry.name());
                            }
                            // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                            float normalX = triangleInput.vertexNormalSource.values[index * normalStride + 0];
                            float normalY = triangleInput.vertexNormalSource.values[index * normalStride + 1];
                            float normalZ = triangleInput.vertexNormalSource.values[index * normalStride + 2];
                            normals.add(normalX);
                            normals.add(normalY);
                            normals.add(normalZ);
                        }

                        indices.add(vertCount++);
                    } else if ("NORMAL".equals(triangleInput.semantic)) {
                        // TODO: Sometimes we get the normal attached to the triangle, sometimes to the vertex

                        int normalStride = triangleInput.normalSource.stride;
                        if (3 != normalStride) {
                            throw new ColladaParseException("Found non-3 stride of " + triangleInput.normalSource.stride
                                                            + " for vertex Input semantic " + triangleInput.semantic +
                                                            " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                        float normalX = triangleInput.normalSource.values[index * normalStride + 0];
                        float normalY = triangleInput.normalSource.values[index * normalStride + 1];
                        float normalZ = triangleInput.normalSource.values[index * normalStride + 2];
                        normals.add(normalX);
                        normals.add(normalY);
                        normals.add(normalZ);
                    } else if ("TEXCOORD".equals(triangleInput.semantic)) {
                        int texCoordStride = triangleInput.texCoordSource.stride;
                        if (2 != texCoordStride) {
                            throw new ColladaParseException("Found non-2 stride of " + triangleInput.texCoordSource.stride
                                                            + " for vertex Input semantic " + triangleInput.semantic +
                                                            " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming S,T order
                        float texCoordS = triangleInput.texCoordSource.values[index * texCoordStride + 0];
                        float texCoordT = triangleInput.texCoordSource.values[index * texCoordStride + 1];
                        texCoord0.add(texCoordS);
                        texCoord0.add(1 - texCoordT);
                        // texCoord0.add(texCoordT);
                    } else {
                        throw new ColladaParseException("Found unexpected triangle Input semantic " + triangleInput.semantic +
                                                        " for geometry id=" + geometry.id() + " name=" + geometry.name());
                    }
                }
            }
        }
        return vertCount;
    }

    private List<Input> parseInputs(ElementSet inputElementSet) {
        List<Input> inputList = new ArrayList<Input>();
        for (Element inputElement : inputElementSet) {
            Input input = new Input();
            inputList.add(input);

            input.semantic = inputElement.attr("semantic");
            input.sourceName = inputElement.attr("source");
            String offsetString = inputElement.attr("offset");
            if (null != offsetString) {
                input.offset = Integer.parseInt(offsetString);
            }
        }

        return inputList;
    }

    private MeshDataSource parseSource(Element sourceElement) throws ColladaParseException {
        MeshDataSource meshDataSource = new MeshDataSource();

        ElementSet accessorSet = sourceElement.find("technique_common", "accessor");
        if (1 != accessorSet.size()) {
            throw new ColladaParseException("Found " + accessorSet.size() + " accessor sets for sourceElement id=" + sourceElement.id() + " name=" + sourceElement.name());
        }
        Element accessor = accessorSet.first();
        String accessorCount = accessor.attr("count");
        meshDataSource.count = Integer.parseInt(accessorCount);
        String accessorStride = accessor.attr("stride");
        meshDataSource.stride = Integer.parseInt(accessorStride);
        String accessorSource = accessor.attr("source");

        ElementSet paramSet = sourceElement.find("param");
        int paramSize = paramSet.size();
        meshDataSource.parameterNames = new String[paramSize];
        meshDataSource.parameterTypes = new String[paramSize];
        for (int i = 0; i < paramSize; i++) {
            Element param = paramSet.get(i);
            meshDataSource.parameterNames[i] = param.attr("name");
            meshDataSource.parameterTypes[i] = param.attr("type");
        }

        Element floatArray = sourceElement.select(accessorSource);
        if (null == floatArray) {
            throw new ColladaParseException("Unable to find id " + accessorSource + " for float array in sourceElement id=" + sourceElement.id() + " name="
                                            + sourceElement.name());
        }
        String arraySizeString = floatArray.attr("count");
        int arraySize = Integer.parseInt(arraySizeString);
        meshDataSource.values = new float[arraySize];
        String floatArrayDataString = floatArray.text().trim();
        String[] floatStrings = getItemsInString(floatArrayDataString);
        if (floatStrings.length != arraySize) {
            throw new ColladaParseException("Expected float array size " + arraySize + " but was " + floatStrings.length + " for sourceElement id=" + sourceElement.id()
                                            + " name="
                                            + sourceElement.name());
        }
        for (int i = 0; i < floatStrings.length; i++) {
            String floatString = floatStrings[i];
            meshDataSource.values[i] = Float.parseFloat(floatString);
        }
        return meshDataSource;
    }

    private String[] getItemsInString(String dataString) {
        String string = dataString.replaceAll("\n", " ");
        string = string.replaceAll("\t", " ");
        string = string.replaceAll("\r", " ");
        while (string.contains("  ")) {
            string = string.replaceAll("  ", " ");
        }
        string = string.trim();
        String[] floatStrings = string.split(" ");
        return floatStrings;
    }

    @Override
    public MeshData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));

        String contents = loadDataAsString(reader);
        MeshData data;
        try {
            data = parseMeshData(contents);
        } catch (ColladaParseException e) {
            logger.error("Unable to load mesh", e);
            return null;
        }

        if (data.getVertices() == null) {
            throw new IOException("No vertices define");
        }
        //if (data.getNormals() == null || data.getNormals().size() != data.getVertices().size()) {
        //    throw new IOException("The number of normals does not match the number of vertices.");
        //}
        if (data.getTexCoord0() == null || data.getTexCoord0().size() / 2 != data.getVertices().size() / 3) {
            throw new IOException("The number of tex coords does not match the number of vertices.");
        }

        return data;
    }

    private class Input {
        public String semantic;
        public String sourceName;
        public int offset;

        public MeshDataSource vertexPositionSource;
        public MeshDataSource vertexNormalSource;
        public MeshDataSource normalSource;
        public MeshDataSource texCoordSource;
    }

    private class MeshDataSource {
        public float[] values;
        public int count;
        public int stride;
        String[] parameterNames;
        String[] parameterTypes;
    }

    private class ColladaParseException extends Exception {
        public ColladaParseException(String msg) {
            super(msg);
        }
    }

}
