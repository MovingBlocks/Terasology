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
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Map;

/**
 * A utility class that converts a {@link JsonElement} to a {@link Tree} and vice versa.
 */
public class JsonTreeAdapter {
    /**
     * The default name for a JSON object node.
     */
    private static final String JSON_OBJECT = "{}";
    /**
     * The default name for a JSON array node.
     */
    private static final String JSON_ARRAY = "[]";
    /**
     * The default name for a null JSON node.
     */
    private static final String JSON_NULL = "null";

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
                return new Tree<>(new JsonNode(name, json.getAsBigDecimal(), ElementType.PRIMITIVE));
            } else if (primitive.isString()) {
                return new Tree<>(new JsonNode(name, json.getAsString(), ElementType.PRIMITIVE));
            } else {
                return new Tree<>(new JsonNode(name, null, ElementType.PRIMITIVE));
            }
        } else if (json.isJsonArray()) {
            Tree<JsonNode> tree = new Tree<>(new JsonNode(name == null ? JSON_ARRAY : name, null, ElementType.ARRAY));
            JsonArray array = json.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                tree.addChild(serialize(array.get(i)));
            }
            return tree;
        } else if (json.isJsonObject()) {
            Tree<JsonNode> tree = new Tree<>(new JsonNode(name == null ? JSON_OBJECT : name, null, ElementType.OBJECT));
            JsonObject object = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                tree.addChild(serialize(entry.getKey(), entry.getValue()));
            }
            return tree;
        } else {
            return new Tree<>(new JsonNode(JSON_NULL, null, ElementType.NULL));
        }
    }

    public enum ElementType {
        PRIMITIVE, OBJECT, ARRAY, NULL
    }

    /**
     * The value type for the tree representation of a {@link JsonElement}.
     */
    public static class JsonNode {
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

        public JsonNode(String property, Object value, ElementType type) {
            this.property = property;
            this.value = value;
            this.type = type;
        }

        public String getProperty() {
            return this.property;
        }

        public Object getValue() {
            return this.value;
        }

        public ElementType getType() {
            return this.type;
        }

        @Override
        public String toString() {
            if (property != null && value != null) {
                return property + ": " + value;
            }
            return property == null ? value.toString() : property;
        }
    }
}
