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
 * A tree representation of a JSON hierarchy, constructed from a {@link JsonElement}.
 */
public class JsonTree extends Tree<JsonTreeValue> {
    public JsonTree(JsonTreeValue childValue) {
        this.setValue(childValue);
    }

    @Override
    public boolean acceptsChild(Tree<JsonTreeValue> child) {
        if (!super.acceptsChild(child)) {
            return false;
        }

        // Only arrays or objects can have children.
        if (getValue().getType() != JsonTreeValue.Type.ARRAY
            && getValue().getType() != JsonTreeValue.Type.OBJECT) {
            return false;
        }

        // Objects cannot have empty object children.
        if (getValue().getType() == JsonTreeValue.Type.OBJECT
            && child.getValue() != null
            && child.getValue().getType() == JsonTreeValue.Type.OBJECT
            && child.getValue().getKey() == null) {
            return false;
        }

        // Only objects can have child key-value pairs.
        if (getValue().getType() == JsonTreeValue.Type.ARRAY
            && (child.getValue().getType() == JsonTreeValue.Type.KEY_VALUE_PAIR)) {
            return false;
        }
        return true;
    }

    @Override
    public void addChild(JsonTreeValue childValue) {
        this.addChild(new JsonTree(childValue));
    }

    public JsonTree getChildAt(int index) {
        return (JsonTree) children.toArray()[index];
    }

    public boolean hasChildWithKey(String key) {
        for (Tree<JsonTreeValue> child : getChildren()) {
            if (child.getValue().getKey() != null && child.getValue().getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public JsonTree getChildWithKey(String key) {
        for (Tree<JsonTreeValue> child : getChildren()) {
            if (child.getValue().getKey().equals(key)) {
                return (JsonTree) child;
            }
        }
        return null;
    }

    public boolean hasSiblingWithKey(String key) {
        for (Tree<JsonTreeValue> child : parent.getChildren()) {
            if (child.getValue().getKey() != null && child.getValue().getKey().equals(key) && child != this) {
                return true;
            }
        }
        return false;
    }

    public JsonTree getSiblingWithKey(String key) {
        for (Tree<JsonTreeValue> child : parent.getChildren()) {
            if (child.getValue().getKey().equals(key) && child != this) {
                return (JsonTree) child;
            }
        }
        return null;
    }

    @Override
    public Tree<JsonTreeValue> copy() {
        Tree<JsonTreeValue> copy = new JsonTree(this.value.copy());
        copy.setExpanded(this.isExpanded());

        for (Tree<JsonTreeValue> child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }
}
