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

package org.terasology.rendering.md5;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.BoneWeight;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshDataBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
@RegisterAssetFileFormat
public class MD5SkeletonLoader extends AbstractAssetFileFormat<SkeletalMeshData> {

    private static final String INTEGER_PATTERN = "((?:[\\+-]?\\d+)(?:[eE][\\+-]?\\d+)?)";
    private static final String FLOAT_PATTERN = "((?:[\\+-]?\\d(?:\\.\\d*)?|\\.\\d+)(?:[eE][\\+-]?(?:\\d(?:\\.\\d*)?|\\.\\d+))?)";
    private static final String VECTOR3_PATTERN = "\\(\\s*" + FLOAT_PATTERN + "\\s+" + FLOAT_PATTERN + "\\s+" + FLOAT_PATTERN + "\\s+\\)";
    private static final String VECTOR2_PATTERN = "\\(\\s*" + FLOAT_PATTERN + "\\s+" + FLOAT_PATTERN + "\\s+\\)";

    private static final Logger logger = LoggerFactory.getLogger(MD5SkeletonLoader.class);

    private Pattern commandLinePattern = Pattern.compile("commandline \"(.*)\".*");
    private Pattern jointPattern = Pattern.compile("\"(.*)\"\\s+" + INTEGER_PATTERN + "\\s*" + VECTOR3_PATTERN + "\\s*" + VECTOR3_PATTERN);
    private Pattern vertPatten = Pattern.compile("vert\\s+" + INTEGER_PATTERN + "\\s+" + VECTOR2_PATTERN + "\\s+" + INTEGER_PATTERN + "\\s+" + INTEGER_PATTERN);
    private Pattern triPattern = Pattern.compile("tri\\s+" + INTEGER_PATTERN + "\\s+" + INTEGER_PATTERN + "\\s+" + INTEGER_PATTERN + "\\s+" + INTEGER_PATTERN);
    private Pattern weightPattern = Pattern.compile("weight\\s+" + INTEGER_PATTERN + "\\s+" + INTEGER_PATTERN + "\\s+" + FLOAT_PATTERN + "\\s+" + VECTOR3_PATTERN);

    public MD5SkeletonLoader() {
        super("md5mesh");
    }

    @Override
    public SkeletalMeshData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        try (InputStream stream = inputs.get(0).openStream()) {
            MD5 md5 = parse(stream);
            SkeletalMeshDataBuilder skeletonBuilder = new SkeletalMeshDataBuilder();
            List<Bone> bones = Lists.newArrayListWithCapacity(md5.numJoints);
            for (int i = 0; i < md5.numJoints; ++i) {
                MD5Joint joint = md5.joints[i];
                Bone bone = new Bone(i, joint.name, joint.position, joint.orientation);
                bones.add(bone);
                if (joint.parent != -1) {
                    bones.get(joint.parent).addChild(bone);
                }
                skeletonBuilder.addBone(bone);
            }
            if (md5.meshes.length > 0) {
                // TODO: Support multiple mesh somehow?
                MD5Mesh mesh = md5.meshes[0];
                for (MD5Weight weight : mesh.weightList) {
                    skeletonBuilder.addWeight(new BoneWeight(weight.position, weight.bias, weight.jointIndex));
                }

                List<Vector2f> uvs = Lists.newArrayList();
                TIntList vertexStartWeight = new TIntArrayList(mesh.numVertices);
                TIntList vertexWeightCount = new TIntArrayList(mesh.numVertices);
                for (MD5Vertex vert : mesh.vertexList) {
                    uvs.add(vert.uv);
                    vertexStartWeight.add(vert.startWeight);
                    vertexWeightCount.add(vert.countWeight);
                }
                skeletonBuilder.setVertexWeights(vertexStartWeight, vertexWeightCount);
                skeletonBuilder.setUvs(uvs);
                TIntList indices = new TIntArrayList(mesh.indexList.length);
                for (int i = 0; i < mesh.numTriangles; ++i) {
                    indices.add(mesh.indexList[i * 3]);
                    indices.add(mesh.indexList[i * 3 + 2]);
                    indices.add(mesh.indexList[i * 3 + 1]);
                }
                skeletonBuilder.setIndices(indices);
            }

            return skeletonBuilder.build();
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing " + inputs.get(0).getFilename(), e);
        }
    }

    private MD5 parse(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charsets.UTF_8));
        MD5 md5 = new MD5();
        String line = MD5ParserCommon.readToLine(reader, "MD5Version ");
        md5.version = Integer.parseInt(line.split(" ", 3)[1]);

        line = MD5ParserCommon.readToLine(reader, "commandline ");
        Matcher commandlineMatch = commandLinePattern.matcher(line);
        if (commandlineMatch.matches()) {
            md5.commandline = commandlineMatch.group(1);
        }

        line = MD5ParserCommon.readToLine(reader, "numJoints ");
        md5.numJoints = Integer.parseInt(line.split(" ", 3)[1]);
        line = MD5ParserCommon.readToLine(reader, "numMeshes ");
        md5.numMeshes = Integer.parseInt(line.split(" ", 3)[1]);

        MD5ParserCommon.readToLine(reader, "joints {");
        readJoints(reader, md5);

        md5.meshes = new MD5Mesh[md5.numMeshes];
        for (int i = 0; i < md5.numMeshes; ++i) {
            MD5ParserCommon.readToLine(reader, "mesh {");
            md5.meshes[i] = readMesh(reader);
        }

        return md5;
    }

    private MD5Mesh readMesh(BufferedReader reader) throws IOException {
        MD5Mesh mesh = new MD5Mesh();
        String line = MD5ParserCommon.readToLine(reader, "numverts ");
        mesh.numVertices = Integer.parseInt(line.trim().split(" ", 3)[1]);
        mesh.vertexList = new MD5Vertex[mesh.numVertices];
        for (int i = 0; i < mesh.numVertices; ++i) {
            line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = vertPatten.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid vertex line \"" + line + "\"");
            }
            int index = Integer.parseInt(matcher.group(1));
            MD5Vertex vert = new MD5Vertex();
            vert.uv = MD5ParserCommon.readUV(matcher.group(2), matcher.group(3));
            vert.startWeight = Integer.parseInt(matcher.group(4));
            vert.countWeight = Integer.parseInt(matcher.group(5));
            mesh.vertexList[index] = vert;
        }

        line = MD5ParserCommon.readToLine(reader, "numtris ");
        mesh.numTriangles = Integer.parseInt(line.trim().split(" ", 3)[1]);
        mesh.indexList = new int[mesh.numTriangles * 3];
        for (int i = 0; i < mesh.numTriangles; ++i) {
            line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = triPattern.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid triangle line \"" + line + "\"");
            }
            int triIndex = Integer.parseInt(matcher.group(1));
            mesh.indexList[3 * triIndex] = Integer.parseInt(matcher.group(2));
            mesh.indexList[3 * triIndex + 1] = Integer.parseInt(matcher.group(3));
            mesh.indexList[3 * triIndex + 2] = Integer.parseInt(matcher.group(4));
        }

        line = MD5ParserCommon.readToLine(reader, "numweights ");
        mesh.numWeights = Integer.parseInt(line.trim().split(" ", 3)[1]);
        mesh.weightList = new MD5Weight[mesh.numWeights];
        for (int i = 0; i < mesh.numWeights; ++i) {
            line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = weightPattern.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid weight line \"" + line + "\"");
            }
            int weightIndex = Integer.parseInt(matcher.group(1));
            MD5Weight weight = new MD5Weight();
            weight.jointIndex = Integer.parseInt(matcher.group(2));
            weight.bias = Float.parseFloat(matcher.group(3));
            weight.position = MD5ParserCommon.readVector3f(matcher.group(4), matcher.group(5), matcher.group(6));
            mesh.weightList[weightIndex] = weight;
        }
        return mesh;
    }

    private void readJoints(BufferedReader reader, MD5 md5) throws IOException {
        md5.joints = new MD5Joint[md5.numJoints];
        for (int i = 0; i < md5.numJoints; ++i) {
            String line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = jointPattern.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid joint line: \"" + line + "\"");
            }
            MD5Joint joint = new MD5Joint();
            joint.name = matcher.group(1);
            joint.parent = Integer.parseInt(matcher.group(2));
            joint.position = MD5ParserCommon.readVector3fAndCorrect(matcher.group(3), matcher.group(4), matcher.group(5));
            joint.orientation = MD5ParserCommon.readQuat4f(matcher.group(6), matcher.group(7), matcher.group(8));
            md5.joints[i] = joint;
            logger.trace("Read joint: {}", joint.name);
        }
    }

    private static class MD5 {
        int version;
        String commandline;
        int numJoints;
        int numMeshes;

        MD5Joint[] joints;
        MD5Mesh[] meshes;
    }

    private static class MD5Joint {
        String name;
        int parent;
        Vector3f position;
        Quat4f orientation;
    }

    private static class MD5Mesh {
        int numVertices;
        MD5Vertex[] vertexList;
        int numTriangles;
        int[] indexList;
        int numWeights;
        MD5Weight[] weightList;
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
}
