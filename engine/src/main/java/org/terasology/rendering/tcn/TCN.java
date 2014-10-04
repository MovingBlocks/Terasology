/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.tcn;

import com.bulletphysics.linearmath.QuaternionUtil;
import com.google.common.collect.Lists;
import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.ElementSet;
import org.eaxy.Xml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.md5.MD5SkeletonLoader;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author synopia
 */
public class TCN {
    private static final Logger logger = LoggerFactory.getLogger(TCN.class);
    public final List<Box> boxes = Lists.newArrayList();

    public static TCN parse(InputStream stream) throws IOException, TCNParseException {
        Document document = Xml.readAndClose(stream);
        Element rootElement = document.getRootElement();

        ElementSet shapeSet = rootElement.find("Models", "Model", "Geometry", "Shape");

        TCN tcn = new TCN();
        for (Element shape : shapeSet) {

            logger.info("Parsing shape name=" + shape.name());

            String shapeType = shape.attr("type");
            if (!"d9e621f7-957f-4b77-b1ae-20dcd0da7751".equals(shapeType)) {
                throw new TCNParseException("Found unsupported " + shapeType + " shape type for shape name=" + shape.name());
            }
            ElementSet offsetElementSet = shape.find("Offset");
            if (1 != offsetElementSet.size()) {
                throw new TCNParseException("Found multiple offset asset values for shape name=" + shape.name());
            }
            Element offsetElement = offsetElementSet.first();
            String offsetString = offsetElement.text();
            String[] offsetArray = getItemsInString(offsetString, ",");
            if (offsetArray.length != 3) {
                throw new TCNParseException("Did not find three coordinates for offset  " + offsetString + " for shape name=" + shape.name());
            }

            ElementSet positionElementSet = shape.find("Position");
            if (1 != positionElementSet.size()) {
                throw new TCNParseException("Found multiple position asset values for shape name=" + shape.name());
            }
            Element positionElement = positionElementSet.first();
            String positionString = positionElement.text();
            String[] positionArray = getItemsInString(positionString, ",");
            if (positionArray.length != 3) {
                throw new TCNParseException("Did not find three coordinates for position  " + positionString + " for shape name=" + shape.name());
            }

            ElementSet rotationElementSet = shape.find("Rotation");
            if (1 != rotationElementSet.size()) {
                throw new TCNParseException("Found multiple rotation asset values for shape name=" + shape.name());
            }
            Element rotationElement = rotationElementSet.first();
            String rotationString = rotationElement.text();
            String[] rotationArray = getItemsInString(rotationString, ",");
            if (rotationArray.length != 3) {
                throw new TCNParseException("Did not find three coordinates for rotation  " + rotationString + " for shape name=" + shape.name());
            }

            ElementSet sizeElementSet = shape.find("Size");
            if (1 != sizeElementSet.size()) {
                throw new TCNParseException("Found multiple size asset values for shape name=" + shape.name());
            }
            Element sizeElement = sizeElementSet.first();
            String sizeString = sizeElement.text();
            String[] sizeArray = getItemsInString(sizeString, ",");
            if (sizeArray.length != 3) {
                throw new TCNParseException("Did not find three coordinates for size  " + sizeString + " for shape name=" + shape.name());
            }

            ElementSet textureOffsetElementSet = shape.find("TextureOffset");
            if (1 != textureOffsetElementSet.size()) {
                throw new TCNParseException("Found multiple textureOffset asset values for shape name=" + shape.name());
            }
            Element textureOffsetElement = textureOffsetElementSet.first();
            String textureOffsetString = textureOffsetElement.text();
            String[] textureOffsetArray = getItemsInString(textureOffsetString, ",");
            if (textureOffsetArray.length != 2) {
                throw new TCNParseException("Did not find two coordinates for Texture Offset  " + textureOffsetString + " for shape name=" + shape.name());
            }

            float positionX = Float.parseFloat(positionArray[0]);
            float positionY = Float.parseFloat(positionArray[1]);
            float positionZ = Float.parseFloat(positionArray[2]);
            Vector3f positionVector = new Vector3f(positionX, positionY, positionZ);

            float sizeX = Float.parseFloat(sizeArray[0]);
            float sizeY = Float.parseFloat(sizeArray[1]);
            float sizeZ = Float.parseFloat(sizeArray[2]);
            Vector3f sizeVector = new Vector3f(sizeX, sizeY, sizeZ);

            float rotationX = Float.parseFloat(rotationArray[0]);
            float rotationY = Float.parseFloat(rotationArray[1]);
            float rotationZ = Float.parseFloat(rotationArray[2]);
            Vector3f rotationVector = new Vector3f((float) Math.toRadians(rotationX), (float) Math.toRadians(rotationY), (float) Math.toRadians(rotationZ));

            // Offset is rotational center
            float offsetX = Float.parseFloat(offsetArray[0]);
            float offsetY = Float.parseFloat(offsetArray[1]);
            float offsetZ = Float.parseFloat(offsetArray[2]);
            Vector3f offsetVector = new Vector3f(offsetX, offsetY, offsetZ);

            float textureX = Float.parseFloat(textureOffsetArray[0]);
            float textureY = Float.parseFloat(textureOffsetArray[1]);
            Vector2f textureVector = new Vector2f(textureX, textureY);

            tcn.boxes.add(new Box(shape.attr("name"), offsetVector, positionVector, rotationVector, sizeVector, textureVector));
        }
        return tcn;
    }

    public MD5SkeletonLoader.MD5 getMD5() {
        MD5SkeletonLoader.MD5 md5 = new MD5SkeletonLoader.MD5();
        MD5SkeletonLoader.MD5Mesh mesh = new MD5SkeletonLoader.MD5Mesh();
        md5.meshes.add(mesh);
        MD5SkeletonLoader.MD5Joint root = new MD5SkeletonLoader.MD5Joint();
        root.orientation = new Quat4f(1, 0, 0, 0);
        root.position = new Vector3f(0, 0, 0);
        root.parent = -1;
        root.name = "root";
        md5.joints.add(root);
        for (Box box : boxes) {
            box.addToMD5(md5);
        }
        return md5;
    }

    private static String[] getItemsInString(String dataString, String separator) {
        String string = dataString.replaceAll("\n", " ");
        string = string.replaceAll("\t", " ");
        string = string.replaceAll("\r", " ");
        while (string.contains("  ")) {
            string = string.replaceAll("  ", " ");
        }
        string = string.trim();
        String[] floatStrings = string.split(separator);
        return floatStrings;
    }

    protected static class TCNParseException extends Exception {

        private static final long serialVersionUID = 1L;

        public TCNParseException(String msg) {
            super(msg);
        }

    }

    public static class Box {
        private static float VERTICES[] = {
                // Front face
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,

                // Back face
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                // Top face
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,

                // Bottom face
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,

                // Right face
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,

                // Left face
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f
        };
        private static int INDICES[] = {
                0, 1, 2, 0, 2, 3,    // front
                4, 5, 6, 4, 6, 7,    // back
                8, 9, 10, 8, 10, 11,   // top
                12, 13, 14, 12, 14, 15,   // bottom
                16, 17, 18, 16, 18, 19,   // right
                20, 21, 22, 20, 22, 23    // left
        };
        public String name;
        public Vector3f offset;
        public Vector3f position;
        public Vector3f rotation;
        public Vector3f size;
        public Vector2f textureOffset;
        public Vector3f center;
        public float textureCoord[];

        public Box(String name, Vector3f offset, Vector3f position, Vector3f rotation, Vector3f size, Vector2f textureOffset) {
            this.name = name;
            this.offset = offset;
            this.position = position;
            this.rotation = rotation;
            this.size = size;
            this.textureOffset = textureOffset;

            center = new Vector3f(size);
            center.scaleAdd(0.5f, offset);

            float width = size.x;
            float height = size.y;
            float depth = size.z;
            /**
             * 0   1
             *
             * 3   2
             *
             * 0 1 2   0 2 3
             */
            textureCoord = new float[]{
                    // Back
                    2 * depth + width, depth,
                    2 * depth + 2 * width, depth,
                    2 * depth + 2 * width, depth + height,
                    2 * depth + width, depth + height,
                    // front
                    depth + width, depth,
                    depth + width, depth + height,
                    depth, depth + height,
                    depth, depth,
                    // Bottom
                    depth + 2 * width, 0,
                    depth + 2 * width, depth,
                    depth + width, depth,
                    depth + width, 0,
                    // Top
                    depth, depth,
                    depth + width, depth,
                    depth + width, 0,
                    depth, 0,
                    // Right
                    depth + width, depth,
                    depth + width, depth + height,
                    2 * depth + width, depth + height,
                    2 * depth + width, depth,
                    // Left
                    depth, depth,
                    0, depth,
                    0, depth + height,
                    depth, depth + height,
            };

            for (int i = 0; i < textureCoord.length / 2; i++) {
                textureCoord[i * 2] = textureOffset.x + textureCoord[i * 2];
                textureCoord[i * 2 + 1] = textureOffset.y + textureCoord[i * 2 + 1];
            }
        }

        private Vector2f getTextureCoord(int i) {
            return new Vector2f(textureCoord[i * 2] / 128, textureCoord[i * 2 + 1] / 128);
        }

        public void addToMD5(MD5SkeletonLoader.MD5 md5) {
            MD5SkeletonLoader.MD5Mesh mesh = md5.meshes.get(0);
            int vertId = mesh.vertexList.size();
            int weightId = mesh.weightList.size();
            int jointId = md5.joints.size();

            Quat4f quat = new Quat4f();
            Quat4f qZ = new Quat4f();
            QuaternionUtil.setRotation(qZ, new Vector3f(0, 0, 1), rotation.z);
            Quat4f qY = new Quat4f();
            QuaternionUtil.setRotation(qY, new Vector3f(0, 1, 0), rotation.y);
            Quat4f qX = new Quat4f();
            QuaternionUtil.setRotation(qX, new Vector3f(1, 0, 0), rotation.x);
            quat.set(1, 0, 0, 0);
            quat.mul(qZ);
            quat.mul(qY);
            quat.mul(qX);
            MD5SkeletonLoader.MD5Joint joint = new MD5SkeletonLoader.MD5Joint();
            joint.name = name + ".";
            joint.orientation = quat;
            joint.parent = 0;
            joint.position = new Vector3f(position.x, -position.y, -position.z);
            md5.joints.add(joint);
            for (int i = 0; i < VERTICES.length / 3; i++) {
                Vector3f corner = new Vector3f(VERTICES[i * 3], VERTICES[i * 3 + 1], VERTICES[i * 3 + 2]);
                corner.scale(1 / 2f);

                Vector3f point = new Vector3f(size.x * corner.x, size.y * corner.y, size.z * corner.z);
                point.add(center);

                MD5SkeletonLoader.MD5Vertex vertex = new MD5SkeletonLoader.MD5Vertex();
                vertex.countWeight = 1;
                vertex.startWeight = weightId + i;
                vertex.uv = getTextureCoord(i);
                mesh.vertexList.add(vertex);
                MD5SkeletonLoader.MD5Weight weight = new MD5SkeletonLoader.MD5Weight();
                weight.bias = 1;
                weight.jointIndex = jointId;
                weight.position = point;
                mesh.weightList.add(weight);
            }
            for (int i = 0; i < INDICES.length / 3; i++) {
                mesh.indexList.add(vertId + INDICES[i * 3]);
                mesh.indexList.add(vertId + INDICES[i * 3 + 1]);
                mesh.indexList.add(vertId + INDICES[i * 3 + 2]);
            }

            float scale = 0.25f;
            Vector3f move = new Vector3f(0, 0, 0);
            joint.position.add(move);
            joint.position.scale(scale);
            for (int i = weightId; i < mesh.weightList.size(); i++) {
                MD5SkeletonLoader.MD5Weight weight = mesh.weightList.get(i);
                weight.position.scale(scale);
            }
        }
    }

}
