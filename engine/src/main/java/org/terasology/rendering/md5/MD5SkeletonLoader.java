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
import org.terasology.asset.AssetLoader;
import org.terasology.module.Module;
import org.terasology.rendering.assets.skeletalmesh.Bone;
import org.terasology.rendering.assets.skeletalmesh.BoneWeight;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.rendering.assets.skeletalmesh.SkeletalMeshDataBuilder;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Immortius
 */
public class MD5SkeletonLoader implements AssetLoader<SkeletalMeshData> {

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

    @Override
    public SkeletalMeshData load(Module module, InputStream stream, List<URL> urls, List<URL> deltas) throws IOException {
        try {
            MD5 md5 = parse(stream);
            return buildMeshData(md5);
        } catch (NumberFormatException e) {
            throw new IOException("Error parsing " + module.toString(), e);
        }
    }

    protected SkeletalMeshData buildMeshData(MD5 md5) {
        SkeletalMeshDataBuilder skeletonBuilder = new SkeletalMeshDataBuilder();
        List<Bone> bones = Lists.newArrayListWithCapacity(md5.joints.size());
        for (int i = 0; i < md5.joints.size(); ++i) {
            MD5Joint joint = md5.joints.get(i);
            Bone bone = new Bone(i, joint.name, joint.position, joint.orientation);
            bones.add(bone);
            if (joint.parent != -1) {
                bones.get(joint.parent).addChild(bone);
            }
            skeletonBuilder.addBone(bone);
        }
        if (md5.meshes.size() > 0) {
            // TODO: Support multiple mesh somehow?
            MD5Mesh mesh = md5.meshes.get(0);
            for (MD5Weight weight : mesh.weightList) {
                skeletonBuilder.addWeight(new BoneWeight(weight.position, weight.bias, weight.jointIndex));
            }

            List<Vector2f> uvs = Lists.newArrayList();
            TIntList vertexStartWeight = new TIntArrayList(mesh.vertexList.size());
            TIntList vertexWeightCount = new TIntArrayList(mesh.vertexList.size());
            for (MD5Vertex vert : mesh.vertexList) {
                uvs.add(vert.uv);
                vertexStartWeight.add(vert.startWeight);
                vertexWeightCount.add(vert.countWeight);
            }
            skeletonBuilder.setVertexWeights(vertexStartWeight, vertexWeightCount);
            skeletonBuilder.setUvs(uvs);
            TIntList indices = new TIntArrayList(mesh.indexList.size());
            for (int i = 0; i < mesh.indexList.size() / 3; ++i) {
                indices.add(mesh.indexList.get(i * 3));
                indices.add(mesh.indexList.get(i * 3 + 1));
                indices.add(mesh.indexList.get(i * 3 + 2));
            }
            skeletonBuilder.setIndices(indices);
        }

        return skeletonBuilder.build();
    }

    protected MD5 parse(InputStream stream) throws IOException {
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
        int numJoints = Integer.parseInt(line.split(" ", 3)[1]);
        line = MD5ParserCommon.readToLine(reader, "numMeshes ");
        int numMeshes = Integer.parseInt(line.split(" ", 3)[1]);

        MD5ParserCommon.readToLine(reader, "joints {");
        readJoints(reader, md5, numJoints);

        for (int i = 0; i < numMeshes; ++i) {
            MD5ParserCommon.readToLine(reader, "mesh {");
            md5.meshes.add(readMesh(reader));
        }

        return md5;
    }

    private MD5Mesh readMesh(BufferedReader reader) throws IOException {
        MD5Mesh mesh = new MD5Mesh();
        String line = MD5ParserCommon.readToLine(reader, "numverts ");
        int numVertices = Integer.parseInt(line.trim().split(" ", 3)[1]);

        for (int i = 0; i < numVertices; ++i) {
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
            mesh.vertexList.add(index, vert);
        }

        line = MD5ParserCommon.readToLine(reader, "numtris ");
        int numTriangles = Integer.parseInt(line.trim().split(" ", 3)[1]);
        for (int i = 0; i < numTriangles; ++i) {
            line = MD5ParserCommon.readNextLine(reader);
            Matcher matcher = triPattern.matcher(line);
            if (!matcher.find()) {
                throw new IOException("Invalid triangle line \"" + line + "\"");
            }
            int triIndex = Integer.parseInt(matcher.group(1));
            mesh.indexList.add(3 * triIndex, Integer.parseInt(matcher.group(2)));
            mesh.indexList.add(3 * triIndex + 1, Integer.parseInt(matcher.group(3)));
            mesh.indexList.add(3 * triIndex + 2, Integer.parseInt(matcher.group(4)));
        }

        line = MD5ParserCommon.readToLine(reader, "numweights ");
        int numWeights = Integer.parseInt(line.trim().split(" ", 3)[1]);
        for (int i = 0; i < numWeights; ++i) {
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
            mesh.weightList.add(weightIndex, weight);
        }
        return mesh;
    }

    private void readJoints(BufferedReader reader, MD5 md5, int numJoints) throws IOException {
        for (int i = 0; i < numJoints; ++i) {
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
            md5.joints.add(i, joint);
            logger.trace("Read joint: {}", joint.name);
        }
    }

    public static class MD5 {
        public int version;
        public String commandline;

        public final List<MD5Joint> joints = Lists.newArrayList();
        public final List<MD5Mesh> meshes = Lists.newArrayList();

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            MD5Mesh mesh = meshes.get(0);
            sb.append("MD5Version 10\n" +
                    "commandline \"Exported from Terasology MD5SkeletonLoader\"\n" +
                    "\n");
            sb.append("numJoints ").append(joints.size()).append("\n");
            sb.append("numMeshes 1\n\n");
            sb.append("joints {\n");
            for (MD5Joint joint : joints) {
                sb.append("\t\"").append(joint.name).append("\" ").append(joint.parent).append(" ( ");
                sb.append(joint.position.x).append(" ");
                sb.append(joint.position.y).append(" ");
                sb.append(joint.position.z).append(" ) ( ");
                Quat4f rot = new Quat4f(joint.orientation);
                rot.normalize();
                if (rot.w > 0) {
                    rot.x = -rot.x;
                    rot.y = -rot.y;
                    rot.z = -rot.z;
                }
                sb.append(rot.x).append(" ");
                sb.append(rot.y).append(" ");
                sb.append(rot.z).append(" )\n");
            }
            sb.append("}\n\n");

            sb.append("mesh {\n");
            sb.append("\tshader \"alosaurustexture.png\"\n");
            sb.append("\tnumverts ").append(mesh.vertexList.size()).append("\n");
            int vertId = 0;
            for (MD5Vertex vert : mesh.vertexList) {
                sb.append("\tvert ").append(vertId).append(" (").append(vert.uv.x).append(" ").append(vert.uv.y).append(") ");
                sb.append(vert.startWeight).append(" ").append(vert.countWeight).append("\n");
                vertId++;
            }
            sb.append("\n");
            sb.append("\tnumtris ").append(mesh.indexList.size() / 3).append("\n");
            List<Integer> indexList = mesh.indexList;
            for (int i = 0; i < indexList.size() / 3; i++) {
                int i1 = indexList.get(i * 3);
                int i2 = indexList.get(i * 3 + 1);
                int i3 = indexList.get(i * 3 + 2);
                sb.append("\ttri ").append(i).append(" ").append(i1).append(" ").append(i2).append(" ").append(i3).append("\n");
            }
            sb.append("\n");
            sb.append("\tnumweights ").append(mesh.weightList.size()).append("\n");
            int meshId = 0;
            for (MD5Weight weight : mesh.weightList) {
                sb.append("\tweight ").append(meshId).append(" ").append(weight.jointIndex).append(" ");
                sb.append(weight.bias).append(" ( ");
                sb.append(weight.position.x).append(" ").append(weight.position.y).append(" ").append(weight.position.z).append(")\n");
                meshId++;
            }
            sb.append("}\n");
            return sb.toString();
        }
    }

    public static class MD5Joint {
        public String name;
        public int parent;
        public Vector3f position;
        public Quat4f orientation;
    }

    public static class MD5Mesh {
        public final List<MD5Vertex> vertexList = Lists.newArrayList();
        public List<Integer> indexList = Lists.newArrayList();
        public List<MD5Weight> weightList = Lists.newArrayList();
    }

    public static class MD5Vertex {
        public Vector2f uv;
        public int startWeight;
        public int countWeight;
    }

    public static class MD5Weight {
        public int jointIndex;
        public float bias;
        public Vector3f position;
    }
}
