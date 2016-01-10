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

import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.NonMatchingPathException;
import org.eaxy.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.BoneWeight;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshDataBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Importer for Collada data exchange model files.
 * <br><br>
 * The development of this loader was greatly influenced by
 * http://www.wazim.com/Collada_Tutorial_1.htm
 * <br><br>
 * TODO: Consider documenting this class similar to what has been done at this web page:
 * <br><br>
 * http://docs.garagegames.com/torque-3d/official/content/documentation/Artist%20Guide/Formats/ColladaLoader.html
 *
 */

public class ColladaLoader {

    private static final Logger logger = LoggerFactory.getLogger(ColladaLoader.class);

    protected TFloatList vertices;
    protected TFloatList texCoord0;
    protected TFloatList texCoord1;
    protected TFloatList normals;
    protected TFloatList colors;
    protected TIntList indices;
    protected double unitsPerMeter;

    protected SkeletalMeshDataBuilder skeletonBuilder;

    public SkeletalMeshData parseSkeletalMeshData(InputStream inputStream) throws ColladaParseException, IOException {
        Document document = Xml.readAndClose(inputStream);
        Element rootElement = document.getRootElement();

        parseMeshData(rootElement);
        parseSkeletalMeshData(rootElement);
        return skeletonBuilder.build();
    }

    public void parseMeshData(InputStream inputStream) throws ColladaParseException, IOException {
        Document document = Xml.readAndClose(inputStream);
        Element rootElement = document.getRootElement();

        parseMeshData(rootElement);
    }

    public TFloatList getVertices() {
        return vertices;
    }

    public TFloatList getTexCoord0() {
        return texCoord0;
    }

    public TFloatList getTexCoord1() {
        return texCoord1;
    }

    public TFloatList getNormals() {
        return normals;
    }

    public TFloatList getColors() {
        return colors;
    }

    public TIntList getIndices() {
        return indices;
    }

    public double getUnitsPerMeter() {
        return unitsPerMeter;
    }

    private void createMd5JointForElementAndParent(Map<String, MD5Joint> md5JointBySidMap,
                                                   Element element, MD5Joint parentMD5Joint) throws ColladaParseException {
        MD5Joint joint = createMD5Joint(element);
        joint.element = element;
        joint.parent = parentMD5Joint;

        if (null != parentMD5Joint) {
            parentMD5Joint.addChild(joint);
        }

        ElementSet elementChildSet = element.find("node");
        for (Element childElement : elementChildSet) {
            createMd5JointForElementAndParent(md5JointBySidMap, childElement, joint);
        }

        String sid = element.attr("sid");
        if (null != sid) {
            md5JointBySidMap.put(sid, joint);
        }
    }

    protected void parseSkeletalMeshData(Element rootElement) throws ColladaParseException {

        List<MD5Joint> md5JointList = new ArrayList<>();
        List<MD5Mesh> md5MeshList = new ArrayList<>();

        skeletonBuilder = new SkeletalMeshDataBuilder();

        // TODO: we need a better way to construct the parent/child nodes, especially for the non-joint nodes
        // MAYBE we can construct all of the nodes up-front, and then fill in the missing data for the ones of type JOINT later
        // And only keep the MD5 nodes in the final list if they are used?

        Map<String, MD5Joint> md5JointBySidMap = new HashMap<>();

        MD5Joint parentMD5Joint = null;
        ElementSet nodeParentSet = rootElement.find("library_visual_scenes", "visual_scene", "node");
        for (Element element : nodeParentSet) {
            createMd5JointForElementAndParent(md5JointBySidMap, element, parentMD5Joint);
        }

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
                throw new ColladaParseException("Found " + vertexWeightsSet.size() + " vertex weights sets for controller id=" + controller.id() + " name="
                        + controller.name());
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
                    Element jointNameSourceElement = skin.select(jointsInput.sourceName);
                    Source jointNameSource = parseSource(jointNameSourceElement);
                    jointNameArray = jointNameSource.nameValues;
                }
                if ("INV_BIND_MATRIX".equals(jointsInput.semantic)) {
                    Element jointMatrixSourceElement = skin.select(jointsInput.sourceName);
                    Source jointMatrixSource = parseSource(jointMatrixSourceElement);
                    inverseBindMatrixArray = jointMatrixSource.floatValues;

                    rotationArray = quad4fArrayFromFloat16ArrayData(inverseBindMatrixArray);
                }
            }

            List<MD5Weight> md5WeightList = Lists.newArrayList();

            float[] weightsArray = null;

            ElementSet vertexWeightsInputSet = vertexWeights.find("input");
            List<Input> vertexWeightsInputList = parseInputs(vertexWeightsInputSet);

            // TODO: for now, assume the offsets will always perfectly match the sorted-by-offset list indexes
            Collections.sort(vertexWeightsInputList, (i1, i2) -> i1.offset - i2.offset);
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
                    Element jointMatrixSourceElement = skin.select(vertexWeightsInput.sourceName);
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

            // TODO: these aren't actually needed once we are populating MD5Weight records
            String[] vertexWeightsJointNameArray = new String[vertexWeightsCount];
            float[] vertexWeightsArray = new float[vertexWeightsCount];

            int vertexWeightsVDataIndex = -1;
            for (int vertexWeightsIndex = 0; vertexWeightsIndex < vertexWeightsCount; vertexWeightsIndex++) {

                MD5Weight md5Weight = new MD5Weight();
                Vector3f vertexPosition = new Vector3f();
                vertexPosition.x = vertices.get(3 * vertexWeightsIndex + 0);
                vertexPosition.y = vertices.get(3 * vertexWeightsIndex + 1);
                vertexPosition.z = vertices.get(3 * vertexWeightsIndex + 2);
                md5Weight.position = vertexPosition;
                md5WeightList.add(md5Weight);

                String vCountString = vertexWeightsVCountStrings[vertexWeightsIndex];
                int vCount = Integer.parseInt(vCountString);
                for (int vCountIndex = 0; vCountIndex < vCount; vCountIndex++) {
                    for (Input vertexWeightsInput : vertexWeightsInputList) {
                        // vCount varies each time
                        ++vertexWeightsVDataIndex;

                        String indexString = vertexWeightsVStrings[vertexWeightsVDataIndex];
                        int index = Integer.parseInt(indexString);
                        if (-1 == index) {
                            throw new ColladaParseException("We do not support indexing into the bind shape yet");
                        }

                        if ("JOINT".equals(vertexWeightsInput.semantic)) {
                            md5Weight.jointIndex = index;
                            vertexWeightsJointNameArray[vertexWeightsIndex] = jointNameArray[index];
                            // logger.debug(String.valueOf(vertexWeightsVDataIndex) + ": " + "jointName=" + vertexWeightsJointNameArray[vertexWeightsIndex]);
                        } else if ("WEIGHT".equals(vertexWeightsInput.semantic)) {
                            md5Weight.bias = weightsArray[index];
                            vertexWeightsArray[vertexWeightsIndex] = weightsArray[index];
                            // logger.debug(String.valueOf(vertexWeightsVDataIndex) + ": " + "weight=" + vertexWeightsArray[vertexWeightsIndex]);
                        } else {
                            throw new ColladaParseException("Found unexpected vertex weights Input semantic " + vertexWeightsInput.semantic +
                                    " for controller id=" + controller.id() + " name=" + controller.name());
                        }
                    }
                }
            }

            MD5Mesh md5Mesh = new MD5Mesh();
            md5Mesh.weightList = md5WeightList;

            // Find a node with sid="joint-name"
            for (String jointName : jointNameArray) {

                MD5Joint md5Joint = md5JointBySidMap.get(jointName);

                if (null == md5Joint) {
                    throw new ColladaParseException("Cannot find joint node for node sid value for joint " + jointName + " in nodes for library_visual_scenes");
                }

                md5JointList.add(md5Joint);
            }
        }

        Deque<MD5Joint> jointsToProcess = new LinkedList<>(md5JointList);
        while (!jointsToProcess.isEmpty()) {
            MD5Joint joint = jointsToProcess.pop();
            MD5Joint parentJoint = joint.parent;
            if (null != parentJoint) {
                if (!md5JointList.contains(parentJoint)) {
                    md5JointList.add(parentJoint);
                    jointsToProcess.push(parentJoint);
                }
            }
        }

        for (MD5Joint joint : md5JointList) {
            if (null == joint.position) {
                throw new ColladaParseException("no joint position for joint with element id " + joint.element.id());
            }
            if (null == joint.orientation) {
                throw new ColladaParseException("no joint orientation for joint with element id " + joint.element.id());
            }
            // index argument is not used for anything currently, so we'll just set it to -1
            joint.bone = new Bone(-1, joint.name, joint.position, joint.orientation);
        }

        for (MD5Joint joint : md5JointList) {
            // We can probably skip unused end nodes
            joint.childList.stream().filter(childJoint -> childJoint.bone != null).forEach(childJoint ->
                    joint.bone.addChild(childJoint.bone));
        }

        for (MD5Joint joint : md5JointList) {
            skeletonBuilder.addBone(joint.bone);
        }

        if (md5MeshList.size() > 0) {
            // TODO: Support multiple mesh somehow?
            MD5Mesh mesh = md5MeshList.get(0);
            for (MD5Weight weight : mesh.weightList) {
                skeletonBuilder.addWeight(new BoneWeight(weight.position, weight.bias, weight.jointIndex));
            }

            List<Vector2f> uvs = Lists.newArrayList();

            TIntList vertexStartWeight = new TIntArrayList(vertices.size() / 3);
            TIntList vertexWeightCount = new TIntArrayList(vertices.size() / 3);
            //            for (MD5Vertex vert : mesh.vertexList) {
            //                uvs.add(vert.uv);
            //                vertexStartWeight.add(vert.startWeight);
            //                vertexWeightCount.add(vert.countWeight);
            //            }

            for (int i = 0; i < vertices.size() / 3; i++) {
                vertexStartWeight.add(i);
                vertexWeightCount.add(1);
            }

            skeletonBuilder.setVertexWeights(vertexStartWeight, vertexWeightCount);

            for (int i = 0; i < normals.size() / 2; i++) {
                uvs.add(new Vector2f(normals.get(i * 2 + 0), normals.get(i * 2 + 1)));
            }
            skeletonBuilder.setUvs(uvs);
            skeletonBuilder.setIndices(indices);
        }

        // Now if you have come this far, you should be able to read the geometry data,
        // as well as the skeleton and skinning data from COLLADA documents. And you should be able to draw
        // the model in raw triangles, as well as draw the skeleton. Although I haven't discussed how you
        // can accumulate the world matrices for each joint and then draw in world coordinates for debugging
        // purposes but I think I gave a hint that we have to multiply parent joint's world matrix with current
        // joint's Joint matrix and save the result in current joint's world matrix. We have to start this
        // process from the root bone. So that we don't have dirty world matrices from parents, and the root
        // Joint's world matrix becomes the Joint matrix, since root don't have any parent. If you are also
        // reading the COLLADA specification version 1.5 you can find the skinning equation so you should also
        // be able to put the model in bind shape. How can we animate this model is still not covered and will
        // be covered in the following sections.

        // THIS IS THE TARGET GOAL:

        /*
        Bones
        - String name
        - int index
        - V3 object position
        - Quat4f obj rotation
        - parent / children bones

        SkeletalMesh


        // This part may not be required if we can implement SkeletalMeshData methods without it

        //////////////

        public SkeletalMeshData(List<Bone> bones, List<BoneWeight> weights,
           List<Vector2f> uvs,
           TIntList vertexStartWeights, TIntList vertexWeightCounts,
           TIntList indices) {

        BoneWeight
        Vector3f position = new Vector3f();
        float bias;
        int boneIndex;
        Vector3f normal = new Vector3f();

        //////////////


           public Collection<Bone> getBones();
           public Bone getRootBone();
           public Bone getBone(String name);

           public int getVertexCount();

           public List<Vector3f> getBindPoseVertexPositions();
           public List<Vector3f> getVertexPositions(List<Vector3f> bonePositions, List<Quat4f> boneRotations);

           public List<Vector3f> getBindPoseVertexNormals();
           public List<Vector3f> getVertexNormals(List<Vector3f> bonePositions, List<Quat4f> boneRotations);

           public TIntList getIndices();
           public List<Vector2f> getUVs();
         */
    }

    private MD5Joint createMD5Joint(Element jointNodeElement) throws ColladaParseException {
        MD5Joint md5Joint = new MD5Joint();

        ElementSet matrixSet = jointNodeElement.find("matrix");
        if (1 == matrixSet.size()) {
            Element matrix = matrixSet.first();

            String floatStringArray = matrix.text();
            String[] floatStrings = getItemsInString(floatStringArray);
            if (floatStrings.length != 16) {
                throw new ColladaParseException("Found float list of " + floatStrings.length + " instead of 16 for joint matrix sets for element " + jointNodeElement.id());
            }
            float[] matrixDataArray = new float[16];
            for (int i = 0; i < floatStrings.length; i++) {
                String floatString = floatStrings[i];
                matrixDataArray[i] = Float.parseFloat(floatString);
            }

            Quat4f[] jointMatrix = quad4fArrayFromFloat16ArrayData(matrixDataArray);
            Vector3f[] positionVectorArray = positionFromFloat16ArrayData(matrixDataArray);
            md5Joint.position = positionVectorArray[0];
            md5Joint.orientation = jointMatrix[0];
        } else if (1 < matrixSet.size()) {
            throw new ColladaParseException("Found " + matrixSet.size() + " joint matrix sets for element " + jointNodeElement.id());
            // } else {
            // TODO: Might be translation, rotation pairs instead of a matrix
            // Or might be an unused joint node
            //            throw new ColladaParseException("Found " + matrixSet.size() + " joint matrix sets for element " + jointNodeElement.id());
        }

        //        boolean isJointNode;
        //        String jointType = jointNodeElement.attr("type");
        //        if ("JOINT".equals(jointType)) {
        //            isJointNode = true;
        //        } else if ("NODE".equals(jointType)) {
        //            isJointNode = false;
        //        } else {
        //            throw new ColladaParseException("Found unknown node type of " + jointType + " for joint matrix sets" + errorLocation);
        //        }

        md5Joint.element = jointNodeElement;
        md5Joint.name = jointNodeElement.id();

        md5Joint.childList = new ArrayList<>();

        return md5Joint;
    }

    private Quat4f[] quad4fArrayFromFloat16ArrayData(float[] inverseBindMatrixArray) {
        Quat4f[] rotationArray = new Quat4f[inverseBindMatrixArray.length / 16];
        for (int i = 0; i < inverseBindMatrixArray.length / 16; ++i) {
            int offset = i * 16;
            Matrix4f matrix4f = new Matrix4f(Arrays.copyOfRange(inverseBindMatrixArray, offset, offset + 16));
            Quat4f rotation = new Quat4f();
            rotation.set(matrix4f);
            rotationArray[i] = rotation;
        }

        return rotationArray;
    }

    private Vector3f[] positionFromFloat16ArrayData(float[] inverseBindMatrixArray) {
        Vector3f[] translationVectorArray = new Vector3f[inverseBindMatrixArray.length / 16];
        for (int i = 0; i < inverseBindMatrixArray.length / 16; ++i) {
            int offset = i * 16;
            Matrix4f matrix4f = new Matrix4f(Arrays.copyOfRange(inverseBindMatrixArray, offset, offset + 16));
            Vector3f translationVector = matrix4f.getTranslation();
            translationVectorArray[i] = translationVector;
        }

        return translationVectorArray;
    }

    protected void parseMeshData(Element rootElement) throws ColladaParseException {

        vertices = new TFloatArrayList();
        texCoord0 = new TFloatArrayList();
        texCoord1 = new TFloatArrayList();
        normals = new TFloatArrayList();
        colors = new TFloatArrayList();
        indices = new TIntArrayList();
        int vertCount = 0;

        ElementSet upAxisSet = rootElement.find("asset", "up_axis");
        if (1 != upAxisSet.size()) {
            throw new ColladaParseException("Found multiple up_axis asset values");
        }
        Element upAxisElement = upAxisSet.first();
        String upAxis = upAxisElement.text();

        ElementSet unitSet = rootElement.find("asset", "unit");
        if (1 != unitSet.size()) {
            throw new ColladaParseException("Found multiple unit asset values");
        }
        Element unitElement = unitSet.first();
        String unitsPerMeterString = unitElement.attr("meter");
        if (null != unitsPerMeterString) {
            unitsPerMeter = Double.parseDouble(unitsPerMeterString);
        }

        boolean yUp = "Y_UP".equals(upAxis);
        boolean zUp = "Z_UP".equals(upAxis);
        boolean xUp = "X_UP".equals(upAxis);
        if (xUp) {
            throw new ColladaParseException("Not supporting X_UP as the upAxis value yet.");
        }

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
                    vertCount = parseTriangles(rootElement, vertices, texCoord0,
                            normals, indices, colors,
                            vertCount, geometry, mesh, triangles,
                            yUp, zUp);
                }

                ElementSet polylistSet = mesh.find("polylist");
                for (Element polylist : polylistSet) {

                    ElementSet vCountSet = polylist.find("vcount");
                    if (1 != vCountSet.size()) {
                        throw new ColladaParseException("Found " + vCountSet.size() + " vcount sets for polylist in geometry id="
                                + geometry.id() + " name=" + geometry.name());
                    }
                    Element vCountElement = vCountSet.first();

                    TIntList vcountList = new TIntArrayList();
                    String[] vCountStrings = getItemsInString(vCountElement.text());
                    for (String string : vCountStrings) {

                        int vCount = Integer.parseInt(string);
                        vcountList.add(vCount);
                    }

                    vertCount = parseFaces(rootElement, vcountList, vertices, texCoord0,
                            normals, indices, colors,
                            vertCount, geometry, mesh, polylist,
                            yUp, zUp);
                }
            }
        }
    }

    private int parseTriangles(Element rootElement, TFloatList verticesParam, TFloatList texCoord0Param,
                               TFloatList normalsParam, TIntList indicesParam, TFloatList colorsParam,
                               int vertCountParam, Element geometry, Element mesh,
                               Element triangles, boolean yUp, boolean zUp) throws ColladaParseException {
        return parseFaces(rootElement, null, verticesParam, texCoord0Param,
                normalsParam, indicesParam, colorsParam,
                vertCountParam, geometry, mesh, triangles,
                yUp, zUp);
    }

    private int parseFaces(Element rootElement, TIntList vcountList, TFloatList verticesParam, TFloatList texCoord0Param,
                           TFloatList normalsParam, TIntList indicesParam, TFloatList colorsParam,
                           int vertCountParam, Element geometry, Element mesh, Element faces,
                           boolean yUp, boolean zUp) throws ColladaParseException {
        int vertCount = vertCountParam;
        String faceCountString = faces.attr("count");
        int faceCount = Integer.parseInt(faceCountString);
        ElementSet faceInputSet = faces.find("input");
        List<Input> faceInputs = parseInputs(faceInputSet);

        String facesMaterial = faces.attr("material");

        float[] vertexColors = null;
        ElementSet libraryMaterialsSet = rootElement.find("library_materials");
        if (0 != libraryMaterialsSet.size()) {
            if (1 != libraryMaterialsSet.size()) {
                throw new ColladaParseException("Found " + libraryMaterialsSet.size() + " library Material sets for geometry id="
                        + geometry.id() + " name=" + geometry.name());
            }
            Element libraryMaterials = libraryMaterialsSet.first();

            Element material;
            try {
                // TODO: this one isn't standard like the others, and sometimes it doesn't exist
                material = libraryMaterials.select("#" + facesMaterial);
                if (null == material) {
                    throw new ColladaParseException("No material for " + facesMaterial + " for geometry id=" + geometry.id() + " name=" + geometry.name());
                }
                ElementSet instanceEffectSet = material.find("instance_effect");
                if (1 != instanceEffectSet.size()) {
                    throw new ColladaParseException("Found " + instanceEffectSet.size() + " instance_effect sets for material " + facesMaterial + " for geometry id="
                            + geometry.id() + " name=" + geometry.name());
                }
                Element instanceEffect = instanceEffectSet.first();

                String effectUrl = instanceEffect.attr("url");

                ElementSet libraryEffectsSet = rootElement.find("library_effects");
                if (0 != libraryEffectsSet.size()) {
                    if (1 != libraryEffectsSet.size()) {
                        throw new ColladaParseException("Found " + libraryEffectsSet.size() + " library effects sets for geometry id=" + geometry.id() + " name="
                                + geometry.name());
                    }
                    Element libraryEffects = libraryEffectsSet.first();

                    Element effect = libraryEffects.select(effectUrl);
                    if (null == effect) {
                        throw new ColladaParseException("No effect for " + effectUrl + " for geometry id=" + geometry.id() + " name=" + geometry.name());
                    }

                    ElementSet colorSet = effect.find("profile_COMMON", "technique", "lambert", "diffuse", "color");
                    if (1 == colorSet.size()) {
                        Element color = colorSet.first();

                        String colorListString = color.text();
                        String[] colorString = getItemsInString(colorListString);
                        if (4 != colorString.length) {
                            throw new ColladaParseException("mesh only supports 4-float color arrays but color list was '" + colorListString + "' for geometry id="
                                    + geometry.id() + " name=" + geometry.name());
                        }
                        vertexColors = new float[colorString.length];
                        for (int i = 0; i < colorString.length; i++) {
                            vertexColors[i] = Float.parseFloat(colorString[i]);
                        }
                    }
                }
            } catch (NonMatchingPathException e) {
                // If we don't find the material, then we're done.
                logger.debug("Material not found, skipping", e);
            }
        }

        for (Input faceInput : faceInputs) {
            if ("VERTEX".equals(faceInput.semantic)) {
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
                        faceInput.vertexPositionSource = parseSource(vertexSourceElement);
                    } else if ("NORMAL".equals(vertexInput.semantic)) {
                        Element normalSourceElement = mesh.select(vertexInput.sourceName);
                        faceInput.vertexNormalSource = parseSource(normalSourceElement);
                    } else {
                        throw new ColladaParseException("Found unexpected vertex Input semantic " + vertexInput.semantic +
                                " for geometry id=" + geometry.id() + " name=" + geometry.name());
                    }
                }
            } else if ("NORMAL".equals(faceInput.semantic)) {
                Element normalSourceElement = mesh.select(faceInput.sourceName);
                faceInput.normalSource = parseSource(normalSourceElement);
                if (3 != faceInput.normalSource.stride) {
                    throw new ColladaParseException("Found stride of " + faceInput.normalSource.stride
                            + " for triangle Input semantic " + faceInput.semantic +
                            " for geometry id=" + geometry.id() + " name=" + geometry.name());
                }
            } else if ("TEXCOORD".equals(faceInput.semantic)) {
                Element texCoordSourceElement = mesh.select(faceInput.sourceName);
                faceInput.texCoordSource = parseSource(texCoordSourceElement);

                if (2 != faceInput.texCoordSource.stride) {
                    logger.warn("Found non-2 stride of " + faceInput.texCoordSource.stride
                            + " for vertex Input semantic " + faceInput.semantic +
                            " for geometry id=" + geometry.id() + " name=" + geometry.name()
                            + ". Ignoring all but first two texture coordinate values.");
                }

            } else {
                throw new ColladaParseException("Found unexpected triangle Input semantic " + faceInput.semantic +
                        " for geometry id=" + geometry.id() + " name=" + geometry.name());
            }
        }
        ElementSet faceDataSet = faces.find("p");
        if (1 != faceDataSet.size()) {
            throw new ColladaParseException("Found " + faceDataSet.size() + " triangleData sets for geometry id=" + geometry.id() + " name=" + geometry.name());
        }
        Element faceData = faceDataSet.first();
        String faceDataString = faceData.text();
        String[] facesStrings = getItemsInString(faceDataString);

        // TODO: for now, assume the offsets will always perfectly match the sorted-by-offset list indexes
        Collections.sort(faceInputs, (i1, i2) -> i1.offset - i2.offset);
        for (int i = 0; i < faceInputs.size(); i++) {
            Input input = faceInputs.get(i);
            if (input.offset != i) {
                throw new ColladaParseException("Triangle input list offset does not match list index for triangle input " + input + " for geometry id=" + geometry.id()
                        + " name=" + geometry.name());
            }
        }

        int facesDataIndex = -1;
        for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
            int vCount = 3; // default to 3 for triangles so we don't have to create a vcountList
            if (null != vcountList) {
                vCount = vcountList.get(faceIndex);
            }
            for (int vertexIndex = 0; vertexIndex < vCount; vertexIndex++) {
                for (Input faceInput : faceInputs) {
                    ++facesDataIndex;
                    String indexString = facesStrings[facesDataIndex];
                    int index = Integer.parseInt(indexString);

                    if ("VERTEX".equals(faceInput.semantic)) {
                        int vertexStride = faceInput.vertexPositionSource.stride;
                        if (3 != vertexStride) {
                            throw new ColladaParseException("Found non-3 stride of " + faceInput.vertexPositionSource.stride
                                    + " for vertex Input semantic " + faceInput.semantic +
                                    " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                        float vertexX = faceInput.vertexPositionSource.floatValues[index * vertexStride + 0];
                        float vertexY = faceInput.vertexPositionSource.floatValues[index * vertexStride + 1];
                        float vertexZ = faceInput.vertexPositionSource.floatValues[index * vertexStride + 2];
                        // See http://docs.garagegames.com/torque-3d/official/content/documentation/Artist%20Guide/Formats/ColladaLoader.html
                        // for up_axis coordinate systems
                        if (yUp) {
                            verticesParam.add(vertexX);
                            verticesParam.add(vertexY);
                            verticesParam.add(vertexZ);
                        } else if (zUp) {
                            verticesParam.add(vertexX);
                            verticesParam.add(vertexZ);
                            verticesParam.add(vertexY); // negated compared to z in yUp
                            // TODO: Y is not negated relative to the orgin like it probably needs to be.
                            //                        } else if (xUp) {
                            //                            verticesParam.add(vertexY); // negated compared to x in yUp
                            //                            verticesParam.add(vertexX);
                            //                            verticesParam.add(vertexZ);
                        }

                        if (null != vertexColors) {
                            for (float vertexColor : vertexColors) {
                                colorsParam.add(vertexColor);
                            }
                        }

                        // TODO: Sometimes we get the normal attached to the triangle, sometimes to the vertex
                        if (null != faceInput.vertexNormalSource) {
                            int normalStride = faceInput.vertexNormalSource.stride;
                            if (3 != normalStride) {
                                throw new ColladaParseException("Found non-3 stride of " + faceInput.vertexNormalSource.stride
                                        + " for vertex Input semantic " + faceInput.semantic +
                                        " for geometry id=" + geometry.id() + " name=" + geometry.name());
                            }
                            // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                            float normalX = faceInput.vertexNormalSource.floatValues[index * normalStride + 0];
                            float normalY = faceInput.vertexNormalSource.floatValues[index * normalStride + 1];
                            float normalZ = faceInput.vertexNormalSource.floatValues[index * normalStride + 2];
                            if (yUp) {
                                normalsParam.add(normalX);
                                normalsParam.add(normalY);
                                normalsParam.add(normalZ);
                            } else if (zUp) {
                                normalsParam.add(normalX);
                                normalsParam.add(normalZ);
                                normalsParam.add(normalY);
                            }
                        }

                        //                        // TODO: how to triangulate faces on the fly
                        //                        indicesParam.add(vertCount++);

                    } else if ("NORMAL".equals(faceInput.semantic)) {
                        // TODO: Sometimes we get the normal attached to the triangle, sometimes to the vertex

                        int normalStride = faceInput.normalSource.stride;
                        if (3 != normalStride) {
                            throw new ColladaParseException("Found non-3 stride of " + faceInput.normalSource.stride
                                    + " for vertex Input semantic " + faceInput.semantic +
                                    " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming X,Y,Z order
                        float normalX = faceInput.normalSource.floatValues[index * normalStride + 0];
                        float normalY = faceInput.normalSource.floatValues[index * normalStride + 1];
                        float normalZ = faceInput.normalSource.floatValues[index * normalStride + 2];
                        if (yUp) {
                            normalsParam.add(normalX);
                            normalsParam.add(normalY);
                            normalsParam.add(normalZ);
                        } else if (zUp) {
                            normalsParam.add(normalX);
                            normalsParam.add(normalZ);
                            normalsParam.add(normalY);
                        }
                    } else if ("TEXCOORD".equals(faceInput.semantic)) {
                        int texCoordStride = faceInput.texCoordSource.stride;
                        if (2 > texCoordStride) {
                            throw new ColladaParseException("Found non-2 stride of " + faceInput.texCoordSource.stride
                                    + " for vertex Input semantic " + faceInput.semantic +
                                    " for geometry id=" + geometry.id() + " name=" + geometry.name());
                        }
                        // TODO: probably should consider parameter indexes instead of assuming S,T order
                        float texCoordS = faceInput.texCoordSource.floatValues[index * texCoordStride + 0];
                        float texCoordT = faceInput.texCoordSource.floatValues[index * texCoordStride + 1];

                        // For texture coordinates, COLLADA's right-handed coordinate system applies;
                        // therefore, an ST texture coordinate of [0,0] maps to the lower-left texel of a texture image
                        texCoord0Param.add(texCoordS);
                        texCoord0Param.add(1 - texCoordT);
                        // texCoord0.add(texCoordT);
                    } else {
                        throw new ColladaParseException("Found unexpected triangle Input semantic " + faceInput.semantic +
                                " for geometry id=" + geometry.id() + " name=" + geometry.name());
                    }
                }
            }

            for (int i = 0; i < vCount - 2; ++i) {
                indices.add(vertCount);
                indices.add(vertCount + i + 1);
                indices.add(vertCount + i + 2);
            }
            vertCount += vCount;

        }
        return vertCount;
    }

    private List<Input> parseInputs(ElementSet inputElementSet) {
        List<Input> inputList = new ArrayList<>();
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
        if (("float".equalsIgnoreCase(source.parameterTypes[0]))
                || ("float4x4".equalsIgnoreCase(source.parameterTypes[0]))) {
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
        public String[] parameterNames;
        public String[] parameterTypes;
    }

    private static class MD5Joint {
        private String name;
        private Vector3f position;
        private Quat4f orientation;

        private Element element;
        private MD5Joint parent;
        private List<MD5Joint> childList = new ArrayList<>();
        private Bone bone;

        public void addChild(MD5Joint joint) {
            childList.add(joint);
        }

        @Override
        public String toString() {
            return "name=" + name
                    + ", element=" + element
                    + ", position=" + position
                    + ", orientation=" + orientation;
        }
    }

    private static class MD5Weight {
        int jointIndex;
        float bias;
        Vector3f position;

        @Override
        public String toString() {
            return "jointIndex=" + jointIndex
                    + ", bias=" + bias
                    + ", position=" + position;
        }
    }

    private static class MD5Mesh {
        List<MD5Weight> weightList;
    }
}
