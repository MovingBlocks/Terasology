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
package org.terasology.rendering.collada;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.BoneWeight;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshDataBuilder;

import com.google.common.collect.Lists;

/**
 * Importer for Collada data exchange model files.
 * 
 * The development of this loader was greatly influenced by 
 * http://www.wazim.com/Collada_Tutorial_1.htm
 *
 * @author mkienenb@gmail.com
 */

public class ColladaLoader {

    private static final Logger logger = LoggerFactory.getLogger(ColladaLoader.class);

    protected TFloatList vertices;
    protected TFloatList texCoord0;
    protected TFloatList texCoord1;
    protected TFloatList normals;
    protected TFloatList colors;
    protected TIntList indices;

    protected SkeletalMeshDataBuilder skeletonBuilder;

    protected String loadDataAsString(BufferedReader reader) throws IOException, FileNotFoundException {
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

    protected void parseData(String contents) throws ColladaParseException {
        Document document = Xml.xml(contents);
        Element rootElement = document.getRootElement();

        parseSkeletalMeshData(rootElement);
        parseMeshData(rootElement);
    }

    private static class MD5Joint {
        String name;
        int parent;
        Vector3f position;
        Quat4f orientation;
    }

    private static class MD5Vertex {
        Vector2f uv;
        int startWeight;
        int countWeight;
    }

    private static class MD5Weight {
        int jointIndex;
        float bias;
        Vector3f position;
    }

    protected void parseSkeletalMeshData(Element rootElement) throws ColladaParseException {

        skeletonBuilder = new SkeletalMeshDataBuilder();
        ElementSet controllerSet = rootElement.find("library_controllers", "controller");
        for (Element controller : controllerSet) {
            ElementSet skinSet = controller.find("skin");
            if (1 != skinSet.size()) {
                throw new ColladaParseException("Found " + skinSet.size() + " skin sets for controller id=" + controller.id() + " name=" + controller.name());
            }
            Element skin = skinSet.first();

            ElementSet jointsSet = skin.find("joints");
            if (1 != jointsSet.size()) {
                throw new ColladaParseException("Found " + jointsSet.size() + " joints sets for controller id=" + controller.id() + " name=" + controller.name());
            }
            Element joints = jointsSet.first();

            ElementSet vertexWeightsSet = skin.find("vertex_weights");
            if (1 != vertexWeightsSet.size()) {
                throw new ColladaParseException("Found " + vertexWeightsSet.size() + " vertex weights sets for controller id=" + controller.id() + " name=" + controller.name());
            }
            Element vertexWeights = vertexWeightsSet.first();
            String vertexWeightsCountString = vertexWeights.attr("count");
            int vertexWeightsCount = Integer.parseInt(vertexWeightsCountString);
            
            String[] jointNameArray = null;
            float[] inverseBindMatrixArray;
            Quat4f[] rotationArray;
            ElementSet jointsInputSet = joints.find("input");
            List<Input> inputList = parseInputs(jointsInputSet);
            for (Input jointsInput : inputList) {
                if ("JOINT".equals(jointsInput.semantic)) {
                    Element jointNameSourceElement =  skin.select(jointsInput.sourceName);
                    Source jointNameSource = parseSource(jointNameSourceElement);
                    jointNameArray = jointNameSource.nameValues;
                }
                if ("INV_BIND_MATRIX".equals(jointsInput.semantic)) {
                    Element jointMatrixSourceElement =  skin.select(jointsInput.sourceName);
                    Source jointMatrixSource = parseSource(jointMatrixSourceElement);
                    inverseBindMatrixArray = jointMatrixSource.floatValues;
                    
                    rotationArray = new Quat4f[inverseBindMatrixArray.length / 16];
                    for (int i = 0; i < inverseBindMatrixArray.length / 16; ++i) {
                        int offset = i * 16;
                        Matrix4f matrix4f = new Matrix4f(Arrays.copyOfRange(inverseBindMatrixArray, offset, offset + 16));
                        Quat4f rotation = new Quat4f();
                        rotation.set(matrix4f);
                        rotationArray[i] = rotation;
                    }
                }
            }
            
            float[] weightsArray = null;

            ElementSet vertexWeightsInputSet = vertexWeights.find("input");
            List<Input> vertexWeightsInputList = parseInputs(vertexWeightsInputSet);
            
            // TODO: for now, assume the offsets will always perfectly match the sorted-by-offset list indexes
            Collections.sort(vertexWeightsInputList, new Comparator<Input>() {
                @Override
                public int compare(Input i1, Input i2) {
                    return i1.offset - i2.offset;
                }
            });
            for (int i = 0; i < vertexWeightsInputList.size(); i++) {
                Input input = vertexWeightsInputList.get(i);
                if (input.offset != i) {
                    throw new ColladaParseException("vertex weights input list offset does not match list index for vertex weights input " + input
                            + " for controller id=" + controller.id() + " name=" + controller.name());
                }
            }

            for (Input vertexWeightsInput : vertexWeightsInputList) {
//                if ("JOINT".equals(vertexWeightsInput.semantic)) {
//                    Element jointNameSourceElement =  skin.select(vertexWeightsInput.sourceName);
//                    Source jointNameSource = parseSource(jointNameSourceElement);
//                    jointNameArray = jointNameSource.nameValues;
//                }
                if ("WEIGHT".equals(vertexWeightsInput.semantic)) {
                    Element jointMatrixSourceElement =  skin.select(vertexWeightsInput.sourceName);
                    Source weightsArraySource = parseSource(jointMatrixSourceElement);
                    weightsArray = weightsArraySource.floatValues;
                }
            }

            ElementSet vertexWeightsVCountDataSet = vertexWeights.find("vcount");
            if (1 != vertexWeightsVCountDataSet.size()) {
                throw new ColladaParseException("Found " + vertexWeightsVCountDataSet.size()
                        + " vertex weights vcount sets for controller id=" + controller.id() + " name=" + controller.name());
            }
            Element vertexWeightsVCountData = vertexWeightsVCountDataSet.first();
            String vertexWeightsVCountString = vertexWeightsVCountData.text();
            String[] vertexWeightsVCountStrings = getItemsInString(vertexWeightsVCountString);
            if (vertexWeightsVCountStrings.length != vertexWeightsCount) {
                throw new ColladaParseException("Expected " + vertexWeightsCount + " but was "
                                                + vertexWeightsVCountStrings.length + " for controller id=" + controller.id() + " name=" + controller.name());
            }

            ElementSet vertexWeightsVDataSet = vertexWeights.find("v");
            if (1 != vertexWeightsVDataSet.size()) {
                throw new ColladaParseException("Found " + vertexWeightsVDataSet.size()
                        + " vertex weights v sets for controller id=" + controller.id() + " name=" + controller.name());
            }
            Element vertexWeightsVData = vertexWeightsVDataSet.first();
            String vertexWeightsVDataString = vertexWeightsVData.text();
            String[] vertexWeightsVStrings = getItemsInString(vertexWeightsVDataString);
//            if (vertexWeightsVStrings.length != (vertexWeightsCount * vertexWeightsInputList.size())) {
//                throw new ColladaParseException("Expected " + vertexWeightsCount + " * input count of "
//                                                + vertexWeightsInputList.size() + " but was "
//                                                + vertexWeightsVStrings.length + " for controller id=" + controller.id() + " name=" + controller.name());
//            }

            
            String[] vertexWeightsJointNameArray = new String[vertexWeightsCount];
            float[] vertexWeightsArray = new float[vertexWeightsCount];

            int vertexWeightsVDataIndex = -1;
            for (int vertexWeightsIndex = 0; vertexWeightsIndex < vertexWeightsCount; vertexWeightsIndex++) {
                String vCountString = vertexWeightsVCountStrings[vertexWeightsIndex];
                int vCount = Integer.parseInt(vCountString);
                for (int vCountIndex = 0; vCountIndex < vCount; vCountIndex++) {
                    for (int vertexWeightsInputOffset = 0; vertexWeightsInputOffset < vertexWeightsInputList.size(); vertexWeightsInputOffset++) {
                        Input vertexWeightsInput = vertexWeightsInputList.get(vertexWeightsInputOffset);
    
                        // vCount varies each time
//                      int vertexWeightsVDataIndex = (vertexWeightsIndex * vertexWeightsInputList.size() * vCount) + (vCountIndex * vertexWeightsInputList.size()) + vertexWeightsInputOffset;
                        ++vertexWeightsVDataIndex;
                        
                        String indexString = vertexWeightsVStrings[vertexWeightsVDataIndex];
                        int index = Integer.parseInt(indexString);
                        if (-1 == index) {
                            throw new ColladaParseException("We do not support indexing into the bind shape yet");
                        }

                        if ("JOINT".equals(vertexWeightsInput.semantic)) {
                            vertexWeightsJointNameArray[vertexWeightsIndex] = jointNameArray[index];
//                            logger.debug(String.valueOf(vertexWeightsVDataIndex) + ": " + "jointName=" + vertexWeightsJointNameArray[vertexWeightsIndex]);
                        } else if ("WEIGHT".equals(vertexWeightsInput.semantic)) {
                            vertexWeightsArray[vertexWeightsIndex] = weightsArray[index];
//                            logger.debug(String.valueOf(vertexWeightsVDataIndex) + ": " + "weight=" + vertexWeightsArray[vertexWeightsIndex]);
                        } else {
                            throw new ColladaParseException("Found unexpected vertex weights Input semantic " + vertexWeightsInput.semantic +
                                                            " for controller id=" + controller.id() + " name=" + controller.name());
                        }
                    }
                }
            }
        }

        ElementSet nodeSet = rootElement.find("library_visual_scenes", "visual_scene", "node", "instance_controller", "skeleton");

        
        ElementSet nodeSet = rootElement.find("library_visual_scenes", "visual_scene", "node");
        for (Element node : node) {
            
        }

//        List<Bone> bones = Lists.newArrayListWithCapacity(md5.numJoints);
//        for (int i = 0; i < md5.numJoints; ++i) {
//            MD5Joint joint = md5.joints[i];
//            Bone bone = new Bone(i, joint.name, joint.position, joint.orientation);
//            bones.add(bone);
//            if (joint.parent != -1) {
//                bones.get(joint.parent).addChild(bone);
//            }
//            skeletonBuilder.addBone(bone);
//        }
//        if (md5.meshes.length > 0) {
//            // TODO: Support multiple mesh somehow?
//            MD5Mesh mesh = md5.meshes[0];
//            for (MD5Weight weight : mesh.weightList) {
//                skeletonBuilder.addWeight(new BoneWeight(weight.position, weight.bias, weight.jointIndex));
//            }
//
//            List<Vector2f> uvs = Lists.newArrayList();
//            TIntList vertexStartWeight = new TIntArrayList(mesh.numVertices);
//            TIntList vertexWeightCount = new TIntArrayList(mesh.numVertices);
//            for (MD5Vertex vert : mesh.vertexList) {
//                uvs.add(vert.uv);
//                vertexStartWeight.add(vert.startWeight);
//                vertexWeightCount.add(vert.countWeight);
//            }
//            skeletonBuilder.setVertexWeights(vertexStartWeight, vertexWeightCount);
//            skeletonBuilder.setUvs(uvs);
//            TIntList indices = new TIntArrayList(mesh.indexList.length);
//            for (int i = 0; i < mesh.numTriangles; ++i) {
//                indices.add(mesh.indexList[i * 3]);
//                indices.add(mesh.indexList[i * 3 + 2]);
//                indices.add(mesh.indexList[i * 3 + 1]);
//            }
//            skeletonBuilder.setIndices(indices);
//        }
//
//        return skeletonBuilder.build();
    }

    protected void parseMeshData(Element rootElement) throws ColladaParseException {

        vertices = new TFloatArrayList();
        texCoord0 = new TFloatArrayList();
        texCoord1 = new TFloatArrayList();
        normals = new TFloatArrayList();
        colors = new TFloatArrayList();
        indices = new TIntArrayList();
        int vertCount = 0;

        // TODO: we shouldn't just cram everything into a single mesh, but should expect separate meshes with differing materials

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
    }

    private int parseTriangles(TFloatList verticesParam, TFloatList texCoord0Param, TFloatList normalsParam, TIntList indicesParam, int vertCountParam, Element geometry, Element mesh,
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
                        float vertexX = triangleInput.vertexPositionSource.floatValues[index * vertexStride + 0];
                        float vertexY = triangleInput.vertexPositionSource.floatValues[index * vertexStride + 1];
                        float vertexZ = triangleInput.vertexPositionSource.floatValues[index * vertexStride + 2];
                        verticesParam.add(vertexX);
                        verticesParam.add(vertexY);
                        verticesParam.add(vertexZ);

                        // TODO: Sometimes we get the normal attached to the triangle, sometimes to the vertex
                        if (null != triangleInput.vertexNormalSource) {
                            int normalStride = triangleInput.vertexNormalSource.stride;
                            if (3 != normalStride) {
                                throw new ColladaParseException("Found non-3 stride of " + triangleInput.vertexNormalSource.stride
                                                                + " for vertex Input semantic " + triangleInput.semantic +
                                                                " for geometry id=" + geometry.id() + " name=" + geometry.name());
                            }
                            // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                            float normalX = triangleInput.vertexNormalSource.floatValues[index * normalStride + 0];
                            float normalY = triangleInput.vertexNormalSource.floatValues[index * normalStride + 1];
                            float normalZ = triangleInput.vertexNormalSource.floatValues[index * normalStride + 2];
                            normalsParam.add(normalX);
                            normalsParam.add(normalY);
                            normalsParam.add(normalZ);
                        }

                        // TODO: how to triangulate faces on the fly
                        indicesParam.add(vertCount++);
//                      for (int i = 0; i < face.length - 2; ++i) {
//                          indices.add(vertCount);
//                          indices.add(vertCount + i + 1);
//                          indices.add(vertCount + i + 2);
//                      }
//                      vertCount += face.length;

                      
                    } else if ("NORMAL".equals(triangleInput.semantic)) {
                        // TODO: Sometimes we get the normal attached to the triangle, sometimes to the vertex

                        int normalStride = triangleInput.normalSource.stride;
                        if (3 != normalStride) {
                            throw new ColladaParseException("Found non-3 stride of " + triangleInput.normalSource.stride
                                                            + " for vertex Input semantic " + triangleInput.semantic +
                                                            " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                        float normalX = triangleInput.normalSource.floatValues[index * normalStride + 0];
                        float normalY = triangleInput.normalSource.floatValues[index * normalStride + 1];
                        float normalZ = triangleInput.normalSource.floatValues[index * normalStride + 2];
                        normalsParam.add(normalX);
                        normalsParam.add(normalY);
                        normalsParam.add(normalZ);
                    } else if ("TEXCOORD".equals(triangleInput.semantic)) {
                        int texCoordStride = triangleInput.texCoordSource.stride;
                        if (2 != texCoordStride) {
                            throw new ColladaParseException("Found non-2 stride of " + triangleInput.texCoordSource.stride
                                                            + " for vertex Input semantic " + triangleInput.semantic +
                                                            " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming S,T order
                        float texCoordS = triangleInput.texCoordSource.floatValues[index * texCoordStride + 0];
                        float texCoordT = triangleInput.texCoordSource.floatValues[index * texCoordStride + 1];
                        
                        // For texture coordinates, COLLADA’s right-handed coordinate system applies;
                        // therefore, an ST texture coordinate of [0,0] maps to the lower-left texel of a texture image
                        texCoord0Param.add(texCoordS);
                        texCoord0Param.add(1 - texCoordT);
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

    private Source parseSource(Element sourceElement) throws ColladaParseException {
        Source source = new Source();

        ElementSet accessorSet = sourceElement.find("technique_common", "accessor");
        if (1 != accessorSet.size()) {
            throw new ColladaParseException("Found " + accessorSet.size() + " accessor sets for sourceElement id=" + sourceElement.id() + " name=" + sourceElement.name());
        }
        Element accessor = accessorSet.first();
        String accessorCount = accessor.attr("count");
        source.count = Integer.parseInt(accessorCount);
        String accessorStride = accessor.attr("stride");
        if (null != accessorStride) {
            source.stride = Integer.parseInt(accessorStride);
        }
        String accessorSource = accessor.attr("source");
        source.accessorSource = accessorSource;

        ElementSet paramSet = accessor.find("param");
        int paramSize = paramSet.size();
        source.parameterNames = new String[paramSize];
        source.parameterTypes = new String[paramSize];
        for (int i = 0; i < paramSize; i++) {
            Element param = paramSet.get(i);
            source.parameterNames[i] = param.attr("name");
            source.parameterTypes[i] = param.attr("type");
        }

        Element objectArray = sourceElement.select(accessorSource);
        if (null == objectArray) {
            throw new ColladaParseException("Unable to find id " + accessorSource + " for float array in sourceElement id=" + sourceElement.id() + " name="
                                            + sourceElement.name());
        }
        String arraySizeString = objectArray.attr("count");
        int arraySize = Integer.parseInt(arraySizeString);
        String objectArrayDataString = objectArray.text().trim();

        // TODO: we should really parse each parameter type, but we'll assume they are homogeneneous for now
        if ( ("float".equalsIgnoreCase(source.parameterTypes[0]))
          || ("float4x4".equalsIgnoreCase(source.parameterTypes[0])) ) {
            source.floatValues = new float[arraySize];
            String[] floatStrings = getItemsInString(objectArrayDataString);
            if (floatStrings.length != arraySize) {
                throw new ColladaParseException("Expected float array size " + arraySize + " but was " + floatStrings.length + " for sourceElement id=" + sourceElement.id()
                                                + " name="
                                                + sourceElement.name());
            }
            for (int i = 0; i < floatStrings.length; i++) {
                String floatString = floatStrings[i];
                source.floatValues[i] = Float.parseFloat(floatString);
            }
        } else if ("name".equalsIgnoreCase(source.parameterTypes[0])) {
            source.nameValues = new String[arraySize];
            String[] nameStrings = getItemsInString(objectArrayDataString);
            if (nameStrings.length != arraySize) {
                throw new ColladaParseException("Expected name array size " + arraySize + " but was " + nameStrings.length + " for sourceElement id=" + sourceElement.id()
                                                + " name="
                                                + sourceElement.name());
            }
            for (int i = 0; i < nameStrings.length; i++) {
                source.nameValues[i] = nameStrings[i];
            }
        } else {
            throw new ColladaParseException("Unsupported parameter type " + source.parameterTypes[0]);
        }
        return source;
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

    private String loadDataAsString(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file), 1024);
        if (file.getName().endsWith(".gz")) {
            reader.close();
            reader = new BufferedReader(
                    new InputStreamReader(new GZIPInputStream(new FileInputStream(file))), 1024);
        }

        return loadDataAsString(reader);
    }

    public static void main(String[] args) {
        ColladaLoader loader = new ColladaLoader();
        try {
            String contents = loader.loadDataAsString(new File("/home/mkienenb/workspaces/keplar-Terasology/ParseCollada/Dwarf_crowd.dae.xml"));
            loader.parseData(contents);
        } catch (IOException | ColladaParseException e) {
            e.printStackTrace();
        }
    }

    private class Input {
        public String semantic;
        public String sourceName;
        public int offset;

        public Source vertexPositionSource;
        public Source vertexNormalSource;
        public Source normalSource;
        public Source texCoordSource;
    }

    private class Source {
        public float[] floatValues;
        public String[] nameValues;
        public String accessorSource;
        public int count;
        public int stride;
        String[] parameterNames;
        String[] parameterTypes;
    }

    protected class ColladaParseException extends Exception {
        private static final long serialVersionUID = 1L;

        public ColladaParseException(String msg) {
            super(msg);
        }
    }

}
