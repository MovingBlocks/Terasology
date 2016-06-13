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

import com.google.gson.JsonElement;

/**
 * The value type for the tree representation of a {@link JsonElement}.
 */
public class JsonTreeNode {
    public enum ElementType {
        PRIMITIVE, ARRAY, OBJECT, NULL
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
    private String property;
    /**
     * The value stored within the node.
     */
    private Object value;
    /**
     * The type of the node.
     */
    private ElementType type;

    public JsonTreeNode(String property, Object value, ElementType type) {
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
