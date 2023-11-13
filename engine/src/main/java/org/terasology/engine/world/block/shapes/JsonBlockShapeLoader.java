// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.block.shapes;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.format.AbstractAssetFileFormat;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.physics.shapes.CompoundShape;
import org.terasology.engine.physics.shapes.ConvexHullShape;
import org.terasology.engine.utilities.gson.Vector2fTypeAdapter;
import org.terasology.engine.utilities.gson.Vector3fTypeAdapter;
import org.terasology.engine.world.block.BlockPart;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.terasology.engine.physics.engine.PhysicsEngineManager.COLLISION_SHAPE_FACTORY;

@RegisterAssetFileFormat
public class JsonBlockShapeLoader extends AbstractAssetFileFormat<BlockShapeData> {
    private Gson gson;

    public JsonBlockShapeLoader() {
        super("shape");
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(BlockShapeData.class, new BlockShapeHandler())
                .registerTypeAdapter(BlockMeshPart.class, new BlockMeshPartHandler())
                .registerTypeAdapter(Vector3f.class, new Vector3fTypeAdapter())
                .registerTypeAdapter(Vector2f.class, new Vector2fTypeAdapter())
                .create();
    }

    @Override
    public BlockShapeData load(ResourceUrn resourceUrn, List<AssetDataFile> inputs) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8)) {
            return gson.fromJson(reader, BlockShapeData.class);
        }
    }


    private static class BlockShapeHandler implements JsonDeserializer<BlockShapeData> {

        public static final String DISPLAY_NAME = "displayName";
        public static final String PITCH_SYMMETRIC = "pitchSymmetric";
        public static final String YAW_SYMMETRIC = "yawSymmetric";
        public static final String ROLL_SYMMETRIC = "rollSymmetric";
        public static final String SYMMETRIC = "symmetric";
        public static final String CONVEX_HULL = "convexHull";
        public static final String COLLIDERS = "colliders";
        public static final String COLLISION = "collision";
        public static final String FULL_SIDE = "fullSide";
        public static final String TYPE = "type";
        public static final String AABB = "AABB";
        public static final String SPHERE = "Sphere";
        public static final String POSITION = "position";
        public static final String EXTENTS = "extents";
        public static final String RADIUS = "radius";

        @Override
        public BlockShapeData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            BlockShapeData shape = new BlockShapeData();
            JsonObject shapeObj = json.getAsJsonObject();

            if (shapeObj.has(DISPLAY_NAME)) {
                shape.setDisplayName(shapeObj.getAsJsonPrimitive(DISPLAY_NAME).getAsString());
            }

            for (BlockPart part : BlockPart.values()) {
                if (shapeObj.has(part.toString().toLowerCase(Locale.ENGLISH))) {
                    JsonObject meshObj = shapeObj.getAsJsonObject(part.toString().toLowerCase(Locale.ENGLISH));
                    shape.setMeshPart(part, (BlockMeshPart) context.deserialize(meshObj, BlockMeshPart.class));
                    if (part.isSide() && meshObj.has(FULL_SIDE)) {
                        shape.setBlockingSide(part.getSide(), meshObj.get(FULL_SIDE).getAsBoolean());
                    }
                }
            }

            if (shapeObj.has(COLLISION) && shapeObj.get(COLLISION).isJsonObject()) {
                JsonObject collisionInfo = shapeObj.get(COLLISION).getAsJsonObject();
                processCollision(context, shape, collisionInfo);
            } else {
                shape.setCollisionShape(COLLISION_SHAPE_FACTORY.getNewUnitCube());
                shape.setCollisionSymmetric(true);
            }
            return shape;
        }

        private void processCollision(JsonDeserializationContext context, BlockShapeData shape, JsonObject collisionInfo) {
            if (collisionInfo.has(PITCH_SYMMETRIC) && collisionInfo.get(PITCH_SYMMETRIC).isJsonPrimitive()
                    && collisionInfo.get(PITCH_SYMMETRIC).getAsJsonPrimitive().isBoolean()) {
                shape.setPitchSymmetric(collisionInfo.get(PITCH_SYMMETRIC).getAsBoolean());
            }
            if (collisionInfo.has(YAW_SYMMETRIC) && collisionInfo.get(YAW_SYMMETRIC).isJsonPrimitive()
                    && collisionInfo.get(YAW_SYMMETRIC).getAsJsonPrimitive().isBoolean()) {
                shape.setYawSymmetric(collisionInfo.get(YAW_SYMMETRIC).getAsBoolean());
            }
            if (collisionInfo.has(ROLL_SYMMETRIC) && collisionInfo.get(ROLL_SYMMETRIC).isJsonPrimitive()
                    && collisionInfo.get(ROLL_SYMMETRIC).getAsJsonPrimitive().isBoolean()) {
                shape.setRollSymmetric(collisionInfo.get(ROLL_SYMMETRIC).getAsBoolean());
            }

            if (collisionInfo.has(SYMMETRIC) && collisionInfo.get(SYMMETRIC).isJsonPrimitive()
                    && collisionInfo.get(SYMMETRIC).getAsJsonPrimitive().isBoolean() && collisionInfo.get(SYMMETRIC).getAsBoolean()) {
                shape.setCollisionSymmetric(true);
            }

            if (collisionInfo.has(CONVEX_HULL) && collisionInfo.get(CONVEX_HULL).isJsonPrimitive()
                    && collisionInfo.get(CONVEX_HULL).getAsJsonPrimitive().isBoolean()) {
                List<Vector3f> verts = buildVertList(shape);
                ConvexHullShape convexHull = COLLISION_SHAPE_FACTORY.getNewConvexHull(new ArrayList<>(verts));
                shape.setCollisionShape(convexHull);
            } else if (collisionInfo.has(COLLIDERS) && collisionInfo.get(COLLIDERS).isJsonArray()
                    && collisionInfo.get(COLLIDERS).getAsJsonArray().size() > 0) {
                JsonArray colliderArray = collisionInfo.get(COLLIDERS).getAsJsonArray();
                processColliders(context, colliderArray, shape);
            } else {
                shape.setCollisionShape(COLLISION_SHAPE_FACTORY.getNewUnitCube());
                shape.setCollisionSymmetric(true);
            }
        }

        private List<Vector3f> buildVertList(BlockShapeData shape) {
            List<Vector3f> result = new ArrayList<>();
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

        private void processColliders(JsonDeserializationContext context, JsonArray colliderArray, BlockShapeData shape) {

            List<ColliderInfo> colliders = Lists.newArrayList();
            for (JsonElement elem : colliderArray) {
                if (elem.isJsonObject()) {
                    JsonObject colliderObj = elem.getAsJsonObject();
                    if (colliderObj.has(TYPE) && colliderObj.get(TYPE).isJsonPrimitive() && colliderObj.getAsJsonPrimitive(TYPE).isString()) {
                        String type = colliderObj.get(TYPE).getAsString();
                        if (AABB.equals(type)) {
                            colliders.add(processAABBShape(context, colliderObj));
                        } else if (SPHERE.equals(type)) {
                            colliders.add(processSphereShape(context, colliderObj));
                        }
                    }
                }
            }
            if (colliders.size() > 1) {
                ColliderInfo info = processCompoundShape(colliders);
                shape.setCollisionShape(info.collisionShape);
                shape.setCollisionOffset(info.offset);
            } else if (colliders.size() == 1) {
                shape.setCollisionShape(colliders.get(0).collisionShape);
                shape.setCollisionOffset(colliders.get(0).offset);
            } else {
                shape.setCollisionShape(COLLISION_SHAPE_FACTORY.getNewUnitCube());
                shape.setCollisionOffset(new Vector3f(0, 0, 0));
                shape.setCollisionSymmetric(true);
            }
        }

        private ColliderInfo processCompoundShape(List<ColliderInfo> colliders) {
            CompoundShape collisionShape = COLLISION_SHAPE_FACTORY.getNewCompoundShape();

            for (ColliderInfo collider : colliders) {
                collisionShape.addChildShape(collider.offset, Rotation.none().orientation(), 1.0f, collider.collisionShape);
            }
            return new ColliderInfo(new Vector3f(), collisionShape);
        }

        private ColliderInfo processAABBShape(JsonDeserializationContext context, JsonObject colliderDef) {
            Vector3f offset = context.deserialize(colliderDef.get(POSITION), Vector3f.class);
            Vector3f extent = context.deserialize(colliderDef.get(EXTENTS), Vector3f.class);
            if (offset == null) {
                throw new JsonParseException("AABB Collider missing position");
            }
            if (extent == null) {
                throw new JsonParseException("AABB Collider missing extents");
            }
            extent.absolute();

            return new ColliderInfo(offset, COLLISION_SHAPE_FACTORY.getNewBox(extent));
        }

        private ColliderInfo processSphereShape(JsonDeserializationContext context, JsonObject colliderDef) {
            Vector3f offset = context.deserialize(colliderDef.get(POSITION), Vector3f.class);
            float radius = colliderDef.get(RADIUS).getAsFloat();
            if (offset == null) {
                throw new JsonParseException("Sphere Collider missing position");
            }

            return new ColliderInfo(offset, COLLISION_SHAPE_FACTORY.getNewSphere(radius));
        }

        private static class ColliderInfo {
            public Vector3f offset;
            public CollisionShape collisionShape;

            ColliderInfo(Vector3f offset, CollisionShape shape) {
                this.offset = offset;
                this.collisionShape = shape;
            }
        }
    }

    private static class BlockMeshPartHandler implements JsonDeserializer<BlockMeshPart> {

        @Override
        public BlockMeshPart deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            final JsonObject meshObj = json.getAsJsonObject();
            final Vector3f[] vertices = context.deserialize(meshObj.get("vertices"), Vector3f[].class);
            final Vector3f[] normals = context.deserialize(meshObj.get("normals"), Vector3f[].class);
            final Vector2f[] texCoords = context.deserialize(meshObj.get("texcoords"), Vector2f[].class);

            if (vertices == null) {
                throw new JsonParseException("Vertices missing");
            }
            if (normals == null) {
                throw new JsonParseException("Normals missing");
            }
            if (texCoords == null) {
                throw new JsonParseException("Texcoords missing");
            }
            if (!meshObj.has("faces")) {
                throw new JsonParseException("Faces missing");
            }

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
            indices.forEach(value -> {
                if (value < 0 || value >= vertices.length) {
                    throw new JsonParseException("Face value out of range: " + value + ", max vertex is " + (vertices.length - 1));
                }
                return true;
            });

            return new BlockMeshPart(vertices, normals, texCoords, indices.toArray());
        }
    }

}
