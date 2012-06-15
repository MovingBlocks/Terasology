/*
 * Copyright 2012
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

import com.google.common.collect.Lists;
import com.google.gson.*;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import org.terasology.math.Side;
import org.terasology.model.structures.AABB;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

/**
 * @author Immortius
 */
public class JsonBlockShapePersister {
    private Gson gson;
    private String currentShapeName;

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

            List<AABB> colliders = Lists.newArrayList();
            if (shapeObj.has("colliders") && shapeObj.get("colliders").isJsonArray()) {
                JsonArray colliderArray = shapeObj.get("colliders").getAsJsonArray();
                for (JsonElement item : colliderArray) {
                    if (item.isJsonObject()) {
                        JsonObject collider = item.getAsJsonObject();
                        Vector3f pos = context.deserialize(collider.get("position"), Vector3f.class);
                        Vector3f extent = context.deserialize(collider.get("extents"), Vector3f.class);
                        if (pos == null) throw new JsonParseException("Collider missing position");
                        if (extent == null) throw new JsonParseException("Collider missing extents");
                        colliders.add(new AABB(pos, extent));
                    }
                }
            } else {
                colliders.add(new AABB(new Vector3f(), new Vector3f(0.5f, 0.5f, 0.5f)));
            }
            shape.setColliders(colliders);
            return shape;
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
