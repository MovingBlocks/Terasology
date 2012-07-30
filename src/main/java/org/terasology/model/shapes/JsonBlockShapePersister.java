/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.model.shapes;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.gson.*;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import org.terasology.math.Rotation;
import org.terasology.math.Side;

import javax.vecmath.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Locale;

/**
 * @author Immortius
 */
public class JsonBlockShapePersister {
    private Gson gson;
    private String currentShapeName;
    private static BoxShape CUBE_SHAPE = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));

    public JsonBlockShapePersister() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(BlockShape.class, new BlockShapeHandler())
                .registerTypeAdapter(BlockMeshPart.class, new BlockMeshPartHandler())
                .registerTypeAdapter(Vector3f.class, new Vector3fHandler())
                .registerTypeAdapter(Vector2f.class, new Vector2fHandler())
                .create();
    }

    public BlockShape load(String title, InputStream stream) throws IOException {
        currentShapeName = title;
        return gson.fromJson(new InputStreamReader(stream), BlockShape.class);
    }

    private class BlockShapeHandler implements JsonDeserializer<BlockShape> {

        @Override
        public BlockShape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BlockShape shape = new BlockShape(currentShapeName);
            JsonObject shapeObj = json.getAsJsonObject();
            if (shapeObj.has("center")) {
                shape.setCenterMesh((BlockMeshPart) context.deserialize(shapeObj.get("center"), BlockMeshPart.class));
            }

            for (Side side : Side.values()) {
                if (shapeObj.has(side.toString().toLowerCase(Locale.ENGLISH))) {
                    JsonObject sideMeshObj = shapeObj.getAsJsonObject(side.toString().toLowerCase(Locale.ENGLISH));
                    shape.setSideMesh(side, (BlockMeshPart) context.deserialize(sideMeshObj, BlockMeshPart.class));
                    if (sideMeshObj.has("fullSide")) {
                        shape.setBlockingSide(side, sideMeshObj.get("fullSide").getAsBoolean());
                    }
                }
            }

            if (shapeObj.has("collision") && shapeObj.get("collision").isJsonObject()) {
                JsonObject collisionInfo = shapeObj.get("collision").getAsJsonObject();
                processCollision(context, shape, collisionInfo);
            } else {
                shape.setCollisionShape(CUBE_SHAPE);
                shape.setCollisionSymmetric(true);
            }
            return shape;
        }

        private void processCollision(JsonDeserializationContext context, BlockShape shape, JsonObject collisionInfo) {
            if (collisionInfo.has("symmetric") && collisionInfo.get("symmetric").isJsonPrimitive() && collisionInfo.get("symmetric").getAsJsonPrimitive().isBoolean()) {
                shape.setCollisionSymmetric(collisionInfo.get("symmetric").getAsBoolean());
            }
            if (collisionInfo.has("convexHull") && collisionInfo.get("convexHull").isJsonPrimitive() && collisionInfo.get("convexHull").getAsJsonPrimitive().isBoolean()) {
                ObjectArrayList<Vector3f> verts = buildVertList(shape);
                if (shape.isCollisionSymmetric()) {
                    ConvexHullShape convexHull = new ConvexHullShape(verts);
                    shape.setCollisionShape(convexHull);
                } else {
                    for (Rotation rot : Rotation.horizontalRotations()) {
                        ObjectArrayList<Vector3f> transformedVerts = new ObjectArrayList<Vector3f>();
                        for (Vector3f vert : verts) {
                            transformedVerts.add(QuaternionUtil.quatRotate(rot.getQuat4f(), vert, new Vector3f()));
                        }
                        ConvexHullShape convexHull = new ConvexHullShape(transformedVerts);
                        shape.setCollisionShape(rot, convexHull);
                    }
                }
            } else if (collisionInfo.has("colliders") && collisionInfo.get("colliders").isJsonArray() && collisionInfo.get("colliders").getAsJsonArray().size() > 0) {
                JsonArray colliderArray = collisionInfo.get("colliders").getAsJsonArray();
                if (shape.isCollisionSymmetric()) {
                    ColliderInfo info = processColliders(context, colliderArray, Rotation.NONE);
                    shape.setCollisionShape(info.collisionShape);
                    shape.setCollisionOffset(info.offset);
                } else {
                    for (Rotation rot : Rotation.horizontalRotations()) {
                        ColliderInfo info = processColliders(context, colliderArray, rot);
                        shape.setCollisionShape(rot, info.collisionShape);
                        shape.setCollisionOffset(rot, info.offset);
                        if (info.symmetric) {
                            shape.setCollisionSymmetric(true);
                            break;
                        }
                    }
                }
            } else {
                shape.setCollisionShape(CUBE_SHAPE);
                shape.setCollisionSymmetric(true);
            }
        }

        private ObjectArrayList<Vector3f> buildVertList(BlockShape shape) {
            ObjectArrayList<Vector3f> result = new ObjectArrayList<Vector3f>();
            BlockMeshPart meshPart = shape.getCenterMesh();
            if (meshPart != null) {
                for (int i = 0; i < meshPart.size(); ++i) {
                    result.add(meshPart.getVertex(i));
                }
            }
            for (Side side : Side.values()) {
                BlockMeshPart sidePart = shape.getSideMesh(side);
                if (sidePart != null) {
                    for (int i = 0; i < sidePart.size(); ++i) {
                        result.add(sidePart.getVertex(i));
                    }
                }
            }
            return result;
        }

        private ColliderInfo processColliders(JsonDeserializationContext context, JsonArray colliderArray, Rotation rot) {
            if (colliderArray.size() == 1) {
                JsonElement shapeDef = colliderArray.get(0);
                if (shapeDef.isJsonObject()) {
                    JsonObject collider = shapeDef.getAsJsonObject();
                    return processBoxShape(context, collider, rot);
                } else {
                    ColliderInfo info = new ColliderInfo(new Vector3f(), CUBE_SHAPE);
                    info.symmetric = true;
                    return info;
                }
            } else {
                return processCompoundShape(context, colliderArray, rot);
            }
        }

        private ColliderInfo processCompoundShape(JsonDeserializationContext context, JsonArray colliderArray, Rotation rot) {
            CompoundShape collisionShape = new CompoundShape();
            Quat4f rotQuat = rot.getQuat4f();

            for (JsonElement item : colliderArray) {
                if (item.isJsonObject()) {
                    JsonObject collider = item.getAsJsonObject();
                    ColliderInfo info = processBoxShape(context, collider, Rotation.NONE);
                    Matrix4f shapeOffset = new Matrix4f(rotQuat, QuaternionUtil.quatRotate(rot.getQuat4f(), info.offset, info.offset), 1.0f);
                    Transform transform = new Transform(shapeOffset);
                    collisionShape.addChildShape(transform, info.collisionShape);
                }
            }
            return new ColliderInfo(new Vector3f(), collisionShape);
        }

        private ColliderInfo processBoxShape(JsonDeserializationContext context, JsonObject colliderDef, Rotation rot) {
            Vector3f offset = context.deserialize(colliderDef.get("position"), Vector3f.class);
            Vector3f extent = context.deserialize(colliderDef.get("extents"), Vector3f.class);
            if (offset == null) throw new JsonParseException("Collider missing position");
            if (extent == null) throw new JsonParseException("Collider missing extents");

            QuaternionUtil.quatRotate(rot.getQuat4f(), extent, extent);
            extent.absolute();

            return new ColliderInfo(QuaternionUtil.quatRotate(rot.getQuat4f(), offset, offset), new BoxShape(extent));
        }

        private class ColliderInfo {
            public Vector3f offset;
            public CollisionShape collisionShape;
            public boolean symmetric;

            public ColliderInfo() {}

            public ColliderInfo(Vector3f offset, CollisionShape shape) {
                this.offset = offset;
                this.collisionShape = shape;
            }

        }
    }


    private class BlockMeshPartHandler implements JsonDeserializer<BlockMeshPart> {

        @Override
        public BlockMeshPart deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject meshObj = json.getAsJsonObject();
            final Vector3f[] vertices = context.deserialize(meshObj.get("vertices"), Vector3f[].class);
            final Vector3f[] normals = context.deserialize(meshObj.get("normals"), Vector3f[].class);
            final Vector2f[] texCoords = context.deserialize(meshObj.get("texcoords"), Vector2f[].class);

            if (vertices == null) throw new JsonParseException("Vertices missing");
            if (normals == null) throw new JsonParseException("Normals missing");
            if (texCoords == null) throw new JsonParseException("Texcoords missing");
            if (!meshObj.has("faces")) throw new JsonParseException("Faces missing");

            if (vertices.length != normals.length || vertices.length != texCoords.length) {
                throw new JsonParseException("vertices, normals and texcoords must have the same length");
            }

            // Normalise the normals for safety
            for (Vector3f norm : normals) {
                norm.normalize();
            }

            int[][] faces = context.deserialize(meshObj.get("faces"), int[][].class);

            // Convert faces to indices via triangle fan
            TIntList indices = new TIntArrayList();
            for (int[] face : faces) {
                for (int tri = 0; tri < face.length - 2; tri++) {
                    indices.add(face[0]);
                    indices.add(face[tri + 1]);
                    indices.add(face[tri + 2]);
                }
            }

            // Check indices in bounds
            indices.forEach(new TIntProcedure() {
                @Override
                public boolean execute(int value) {
                    if (value < 0 || value >= vertices.length)
                        throw new JsonParseException("Face value out of range: " + value + ", max vertex is " + (vertices.length - 1));
                    return true;
                }
            });

            return new BlockMeshPart(vertices, normals, texCoords, indices.toArray());
        }
    }

    private class Vector3fHandler implements JsonDeserializer<Vector3f> {
        @Override
        public Vector3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray jsonArray = json.getAsJsonArray();
            return new Vector3f(jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat(), jsonArray.get(2).getAsFloat());
        }
    }

    private class Vector2fHandler implements JsonDeserializer<Vector2f> {
        @Override
        public Vector2f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonArray jsonArray = json.getAsJsonArray();
            return new Vector2f(jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat());
        }
    }
}
