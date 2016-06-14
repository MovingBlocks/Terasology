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

import com.google.api.client.util.Lists;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.List;

/**
 * A tree representation of a JSON hierarchy, constructed from a {@link com.google.gson.JsonElement}.
 */
public class JsonTree extends Tree<JsonTreeNode> {
    private static final String NULL_NODE_ARGUMENT = "node argument is null";
    private static final String NODE_ARGUMENT_INVALID_PARENT = "node argument is not a child of this tree";

    /**
     * The object stored in this tree.
     */
    private JsonTreeNode value;
    /**
     * Whether the tree is expanded.
     */
    private boolean expanded;
    /**
     * The parent of this tree.
     */
    private Tree<JsonTreeNode> parent;
    /**
     * The children of this tree.
     */
    private List<Tree<JsonTreeNode>> children = Lists.newArrayList();

    public JsonTree(JsonTreeNode value) {
        this.setValue(value);
    }

    @Override
    public JsonTreeNode getValue() {
        return this.value;
    }

    @Override
    public void setValue(JsonTreeNode value) {
        this.value = value;
    }

    @Override
    public boolean isExpanded() {
        return this.expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean isRoot() {
        return this.parent == null;
    }

    @Override
    public Tree<JsonTreeNode> getParent() {
        return this.parent;
    }

    @Override
    public void setParent(Tree<JsonTreeNode> tree) {
        this.parent = tree;
    }

    @Override
    public Collection<Tree<JsonTreeNode>> getChildren() {
        return this.children;
    }

    @Override
    public int getIndex(Tree<JsonTreeNode> tree) {
        return this.children.indexOf(tree);
    }

    @Override
    public boolean containsChild(Tree<JsonTreeNode> child) {
        return this.children.contains(child);
    }

    private boolean acceptsChild(Tree<JsonTreeNode> child) {
        // null children are not accepted
        if (child == null) {
            return false;
        }
        // PRIMITIVE or NULL nodes cannot have children
        if (getValue().getType() == JsonTreeNode.ElementType.PRIMITIVE
                || getValue().getType() == JsonTreeNode.ElementType.NULL) {
            return false;
        }
        // ARRAY nodes cannot have PRIMITIVE children
        /*if (getValue().getType() == JsonTreeNode.ElementType.ARRAY
                && child.getValue().getType() == JsonTreeNode.ElementType.PRIMITIVE) {
            return false;
        }*/
        return true;
    }

    @Override
    public void addChild(JsonTreeNode childValue) {
        this.addChild(new JsonTree(childValue));
    }

    @Override
    public void addChild(Tree<JsonTreeNode> child) {
        if (this.acceptsChild(child)) {
            this.children.add(child);
            child.setParent(this);
        }
    }

    @Override
    public void addChild(int index, Tree<JsonTreeNode> child) {
        if (this.acceptsChild(child)) {
            this.children.add(index, child);
            child.setParent(this);
        }
    }

    @Override
    public void removeChild(int childIndex) {
        Tree<JsonTreeNode> child = this.children.remove(childIndex);
        child.setParent(null);
    }

    @Override
    public void removeChild(Tree<JsonTreeNode> child) {
        Preconditions.checkNotNull(child, NULL_NODE_ARGUMENT);
        Preconditions.checkState(child.getParent() == this, NODE_ARGUMENT_INVALID_PARENT);

        this.children.remove(child);
        child.setParent(null);
    }

    @Override
    public Tree<JsonTreeNode> copy() {
        JsonTree copy = new JsonTree(this.value);
        copy.setExpanded(this.expanded);

        for (Tree<JsonTreeNode> child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }
}
