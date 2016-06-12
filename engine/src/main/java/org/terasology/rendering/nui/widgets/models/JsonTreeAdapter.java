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
 * A utility class that converts a {@link JsonElement} to a {@link Tree} and vice versa.
 */
public class JsonTreeAdapter {
    private JsonTreeAdapter() {
    }

    /**
     * @param json The {@link JsonElement} to be converted to a {@link Tree}.
     * @return A tree representation of the JSON hierarchy.
     */
    public static Tree<JsonNode> serialize(JsonElement json) {
        return serialize(null, json);
    }

    /**
     * @param name The name to be given to this node (if null, is replaced by a default name).
     * @param json The {@link JsonElement} to be converted to a {@link Tree}.
     * @return A tree representation of the JSON hierarchy.
     */
    private static Tree<JsonNode> serialize(String name, JsonElement json) {
        if (json.isJsonPrimitive()) {
            JsonPrimitive primitive = json.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new Tree<>(new JsonNode(name, json.getAsBoolean(), ElementType.PRIMITIVE));
            } else if (primitive.isNumber()) {
                return new Tree<>(new JsonNode(name, json.getAsNumber(), ElementType.PRIMITIVE));
            } else if (primitive.isString()) {
                return new Tree<>(new JsonNode(name, json.getAsString(), ElementType.PRIMITIVE));
            } else {
                return new Tree<>(new JsonNode(name, null, ElementType.PRIMITIVE));
            }
        } else if (json.isJsonArray()) {
            Tree<JsonNode> tree = new Tree<>(new JsonNode(name, null, ElementType.ARRAY));
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                tree.addChild(serialize(array.get(i)));
            }
            return tree;
        } else if (json.isJsonObject()) {
            Tree<JsonNode> tree = new Tree<>(new JsonNode(name, null, ElementType.OBJECT));
            JsonObject object = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                tree.addChild(serialize(entry.getKey(), entry.getValue()));
            }
            return tree;
        } else {
            return new Tree<>(new JsonNode(name, null, ElementType.NULL));
        }
    }

    /**
     * @param tree A tree hierarchy based on a {@link JsonElement}, created by the serialize() method.
     * @return The initial {@link JsonElement} reconstructed from the tree.
     */
    public static JsonElement deserialize(Tree<JsonNode> tree) {
        JsonNode node = tree.getValue();
        if (node.type == ElementType.PRIMITIVE) {
            Object value = node.value;
            if (value instanceof Boolean) {
                return new JsonPrimitive((Boolean) value);
            } else if (value instanceof Number) {
                return new JsonPrimitive((Number) value);
            } else if (value instanceof String) {
                return new JsonPrimitive((String) value);
            } else {
                return JsonNull.INSTANCE;
            }
        } else if (node.type == ElementType.ARRAY) {
            JsonArray array = new JsonArray();
            for (Tree<JsonNode> child : tree.getChildren()) {
                array.add(deserialize(child));
            }
            return array;
        } else if (node.type == ElementType.OBJECT) {
            JsonObject object = new JsonObject();
            for (Tree<JsonNode> child : tree.getChildren()) {
                object.add(child.getValue().getProperty(), deserialize(child));
            }
            return object;
        } else {
            return JsonNull.INSTANCE;
        }
    }

    private enum ElementType {
        PRIMITIVE, ARRAY, OBJECT, NULL
    }

    /**
     * The value type for the tree representation of a {@link JsonElement}.
     */
    public static class JsonNode {
        /**
         * The default name for a JSON object node.
         */
        private static final String OBJECT_STRING = "{}";
        /**
         * The default name for a JSON array node.
         */
        private static final String ARRAY_STRING = "[]";
        /**
         * The default name for a null JSON node.
         */
        private static final String NULL_STRING = "null";
        /**
         * The name of the node.
         */
        private String property;
        /**
         * The value stored within the node.
         */
        private Object value;
        /**
         * The type of the node.
         */
        private ElementType type;

        private JsonNode(String property, Object value, ElementType type) {
            this.property = property;
            this.value = value;
            this.type = type;
        }

        public String getProperty() {
            return property;
        }

        public Object getValue() {
            return value;
        }

        public ElementType getType() {
            return type;
        }

        @Override
        public String toString() {
            if (type == ElementType.PRIMITIVE) {
                if (property != null && value != null) {
                    return property + ": " + value;
                }
                return property == null ? value.toString() : property;
            } else if (type == ElementType.ARRAY) {
                return property != null ? property : ARRAY_STRING;
            } else if (type == ElementType.OBJECT) {
                return property != null ? property : OBJECT_STRING;
            } else {
                return property != null ? property : NULL_STRING;
            }
        }
    }
}
