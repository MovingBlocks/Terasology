/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.widgets.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

/**
 * A utility class that converts a {@link JsonElement} to a {@link JsonTree} and vice versa.
 */
public final class JsonTreeConverter {
    private JsonTreeConverter() {
    }

    /**
     * @param json The {@link JsonElement} to be converted to a {@link JsonTree}.
     * @return A tree representation of the JSON hierarchy.
     */
    public static JsonTree serialize(JsonElement json) {
        return serialize(null, json);
    }

    /**
     * @param name The name to be given to this node (if null, is replaced by a default name).
     * @param json The {@link JsonElement} to be converted to a {@link JsonTree}.
     * @return A tree representation of the JSON hierarchy.
     */
    private static JsonTree serialize(String name, JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new JsonTree(new JsonTreeNode(name, json.getAsBoolean(), JsonTreeNode.ElementType.PRIMITIVE));
            } else if (primitive.isNumber()) {
                return new JsonTree(new JsonTreeNode(name, json.getAsNumber(), JsonTreeNode.ElementType.PRIMITIVE));
            } else if (primitive.isString()) {
                return new JsonTree(new JsonTreeNode(name, json.getAsString(), JsonTreeNode.ElementType.PRIMITIVE));
            } else {
                return new JsonTree(new JsonTreeNode(name, null, JsonTreeNode.ElementType.PRIMITIVE));
            }
        } else if (json.isJsonArray()) {
            JsonTree tree = new JsonTree(new JsonTreeNode(name, null, JsonTreeNode.ElementType.ARRAY));
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                tree.addChild(serialize(array.get(i)));
            }
            return tree;
        } else if (json.isJsonObject()) {
            JsonTree tree = new JsonTree(new JsonTreeNode(name, null, JsonTreeNode.ElementType.OBJECT));
            JsonObject object = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                tree.addChild(serialize(entry.getKey(), entry.getValue()));
            }
            return tree;
        } else {
            return new JsonTree(new JsonTreeNode(name, null, JsonTreeNode.ElementType.NULL));
        }
    }

    /**
     * @param tree A tree hierarchy based on a {@link JsonElement}, created by the serialize() method.
     * @return The initial {@link JsonElement} reconstructed from the tree.
     */
    public static JsonElement deserialize(Tree<JsonTreeNode> tree) {
        JsonTreeNode node = tree.getValue();
        if (node.getType() == JsonTreeNode.ElementType.PRIMITIVE) {
            Object value = node.getValue();
            if (value instanceof Boolean) {
                return new JsonPrimitive((Boolean) value);
            } else if (value instanceof Number) {
                return new JsonPrimitive((Number) value);
            } else if (value instanceof String) {
                return new JsonPrimitive((String) value);
            } else {
                return JsonNull.INSTANCE;
            }
        } else if (node.getType() == JsonTreeNode.ElementType.ARRAY) {
            JsonArray array = new JsonArray();
            for (Tree<JsonTreeNode> child : tree.getChildren()) {
                array.add(deserialize(child));
            }
            return array;
        } else if (node.getType() == JsonTreeNode.ElementType.OBJECT) {
            JsonObject object = new JsonObject();
            for (Tree<JsonTreeNode> child : tree.getChildren()) {
                object.add(child.getValue().getProperty(), deserialize(child));
            }
            return object;
        } else {
            return JsonNull.INSTANCE;
        }
    }
}
