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

/**
 * A tree representation of a JSON hierarchy, constructed from a {@link com.google.gson.JsonElement}.
 */
public class JsonTree extends Tree<JsonTreeNode> {
    public JsonTree(JsonTreeNode childValue) {
        this.setValue(childValue);
    }

    @Override
    public boolean acceptsChild(Tree<JsonTreeNode> child) {
        // Only non-null children are allowed.
        if (child == null) {
            return false;
        }
        // Only arrays or objects can have children.
        if (getValue().getType() != JsonTreeNode.ElementType.ARRAY
                && getValue().getType() != JsonTreeNode.ElementType.OBJECT) {
            return false;
        }
        // Additionally, only objects can have child key-value pairs.
        if (getValue().getType() == JsonTreeNode.ElementType.ARRAY
                && child.getValue().getType() == JsonTreeNode.ElementType.KEY_VALUE_PAIR) {
            return false;
        }
        return true;
    }

    @Override
    public void addChild(JsonTreeNode childValue) {
        this.addChild(new JsonTree(childValue));
    }

    @Override
    public Tree<JsonTreeNode> copy() {
        Tree<JsonTreeNode> copy = new JsonTree(this.value);
        copy.setExpanded(this.expanded);

        for (Tree<JsonTreeNode> child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }
}
