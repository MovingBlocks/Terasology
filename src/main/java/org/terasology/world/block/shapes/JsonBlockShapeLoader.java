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

package org.terasology.world.block.shapes;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.math.Rotation;
import org.terasology.utilities.gson.Vector2fHandler;
import org.terasology.utilities.gson.Vector3fHandler;
import org.terasology.world.block.BlockPart;

import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author Immortius
 */
public class JsonBlockShapeLoader implements AssetLoader<BlockShape> {
    private Gson gson;
    private static BoxShape CUBE_SHAPE = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));

    public JsonBlockShapeLoader() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(BlockShape.class, new BlockShapeHandler())
                .registerTypeAdapter(BlockMeshPart.class, new BlockMeshPartHandler())
                .registerTypeAdapter(Vector3f.class, new Vector3fHandler())
                .registerTypeAdapter(Vector2f.class, new Vector2fHandler())
                .create();
    }


    @Override
    public BlockShape load(InputStream stream, AssetUri uri, List<URL> urls) throws IOException {
        BlockShape shape = gson.fromJson(new InputStreamReader(stream), BlockShape.class);
        shape.setURI(uri);
        return shape;
    }

    private class BlockShapeHandler implements JsonDeserializer<BlockShape> {

        @Override
        public BlockShape deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BlockShape shape = new BlockShape();
            JsonObject shapeObj = json.getAsJsonObject();

            for (BlockPart part : BlockPart.values()) {
                if (shapeObj.has(part.toString().toLowerCase(Locale.ENGLISH))) {
                    JsonObject meshObj = shapeObj.getAsJsonObject(part.toString().toLowerCase(Locale.ENGLISH));
                    shape.setMeshPart(part, (BlockMeshPart) context.deserialize(meshObj, BlockMeshPart.class));
                    if (part.isSide() && meshObj.has("fullSide")) {
                        shape.setBlockingSide(part.getSide(), meshObj.get("fullSide").getAsBoolean());
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

        /*private IndexedMesh toIndexedMesh(BlockMeshPart meshPart) {
            IndexedMesh mesh = new IndexedMesh();
            // 3 Floats per vertex
            mesh.vertexBase = BufferUtils.createByteBuffer(meshPart.size() * 3 * 4);
            mesh.vertexStride = 3 * 4;
            // 3 Vertices per triangle, each index is a Integer
            mesh.triangleIndexBase = BufferUtils.createByteBuffer(meshPart.indicesSize() * 4);
            mesh.triangleIndexStride = 3 * 4;
            mesh.numVertices = meshPart.size();
            mesh.numTriangles = meshPart.indicesSize() / 3;
            mesh.indexType = ScalarType.INTEGER;

            ByteBuffer vertices = BufferUtils.createByteBuffer(3 * 4 * meshPart.size());
            for (int i = 0; i < meshPart.size(); ++i) {
                Vector3f vertex = meshPart.getVertex(i);
                mesh.vertexBase.putFloat(vertex.x);
                mesh.vertexBase.putFloat(vertex.y);
                mesh.vertexBase.putFloat(vertex.z);
            }
            ByteBuffer indices = BufferUtils.createByteBuffer(meshPart.indicesSize() * 4);
            for (int i = 0; i < meshPart.indicesSize(); ++i) {
                mesh.triangleIndexBase.putInt(meshPart.getIndex(i));
            }
            return mesh;
        }*/

        private ObjectArrayList<Vector3f> buildVertList(BlockShape shape) {
            ObjectArrayList<Vector3f> result = new ObjectArrayList<Vector3f>();
            for (BlockPart part : BlockPart.values()) {
                BlockMeshPart meshPart = shape.getMeshPart(part);
                if (meshPart != null) {
                    for (int i = 0; i < meshPart.size(); ++i) {
                        result.add(meshPart.getVertex(i));
                    }
                }
            }
            return result;
        }

        private ColliderInfo processColliders(JsonDeserializationContext context, JsonArray colliderArray, Rotation rot) {
            List<ColliderInfo> colliders = Lists.newArrayList();
            for (JsonElement elem : colliderArray) {
                if (elem.isJsonObject()) {
                    JsonObject colliderObj = elem.getAsJsonObject();
                    if (colliderObj.has("type") && colliderObj.get("type").isJsonPrimitive() && colliderObj.getAsJsonPrimitive("type").isString()) {
                        String type = colliderObj.get("type").getAsString();
                        if ("AABB".equals(type)) {
                            colliders.add(processAABBShape(context, colliderObj, rot));
                        } else if ("Sphere".equals(type)) {
                            colliders.add(processSphereShape(context, colliderObj, rot));
                        }
                    }
                }
            }
            if (colliders.size() > 1) {
                return processCompoundShape(colliders);
            } else if (colliders.size() == 1) {
                return colliders.get(0);
            } else {
                ColliderInfo result = new ColliderInfo(new Vector3f(), CUBE_SHAPE);
                result.symmetric = true;
                return result;
            }
        }

        private ColliderInfo processCompoundShape(List<ColliderInfo> colliders) {
            CompoundShape collisionShape = new CompoundShape();

            for (ColliderInfo collider : colliders) {
                Transform transform = new Transform(new Matrix4f(Rotation.NONE.getQuat4f(), collider.offset, 1.0f));
                collisionShape.addChildShape(transform, collider.collisionShape);
            }
            return new ColliderInfo(new Vector3f(), collisionShape);
        }

        private ColliderInfo processAABBShape(JsonDeserializationContext context, JsonObject colliderDef, Rotation rot) {
            Vector3f offset = context.deserialize(colliderDef.get("position"), Vector3f.class);
            Vector3f extent = context.deserialize(colliderDef.get("extents"), Vector3f.class);
            if (offset == null) throw new JsonParseException("AABB Collider missing position");
            if (extent == null) throw new JsonParseException("AABB Collider missing extents");

            QuaternionUtil.quatRotate(rot.getQuat4f(), extent, extent);
            extent.absolute();

            return new ColliderInfo(QuaternionUtil.quatRotate(rot.getQuat4f(), offset, offset), new BoxShape(extent));
        }

        private ColliderInfo processSphereShape(JsonDeserializationContext context, JsonObject colliderDef, Rotation rot) {
            Vector3f offset = context.deserialize(colliderDef.get("position"), Vector3f.class);
            float radius = colliderDef.get("radius").getAsFloat();
            if (offset == null) throw new JsonParseException("Sphere Collider missing position");

            return new ColliderInfo(QuaternionUtil.quatRotate(rot.getQuat4f(), offset, offset), new SphereShape(radius));
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

}
