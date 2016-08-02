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
package org.terasology.rendering.nui.widgets.treeView;

import com.google.gson.JsonElement;

/**
 * The value type for the tree representation of a {@link JsonElement}.
 */
public class JsonTreeValue {
    /**
     * The type of the JSON node.
     */
    public enum Type {
        /**
         * Primitive data type (string, boolean, array).
         */
        VALUE,
        /**
         * A primitive data type paired with a key string.
         */
        KEY_VALUE_PAIR,
        /**
         * An ordered list of zero or more values.
         */
        ARRAY,
        /**
         * An unordered list of name/value pairs.
         */
        OBJECT,
        /**
         * An empty value.
         */
        NULL
    }

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
    private String key;

    /**
     * The value stored within the node.
     */
    private Object value;
    /**
     * The type of the node.
     */
    private Type type;

    public JsonTreeValue() {
        this(null, null, Type.NULL);
    }

    public JsonTreeValue(String key, Object value, Type type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        if (type == Type.KEY_VALUE_PAIR) {
            if (key != null && value != null) {
                return key + ": " + value;
            }
            return key == null ? value.toString() : key;
        } else if (type == Type.VALUE) {
            return value.toString();
        } else if (type == Type.ARRAY) {
            return key != null ? key : ARRAY_STRING;
        } else if (type == Type.OBJECT) {
            return key != null ? key : OBJECT_STRING;
        } else {
            return key != null ? key : NULL_STRING;
        }
    }

    public JsonTreeValue copy() {
        return new JsonTreeValue(key, value, type);
    }
}
