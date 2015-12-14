/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.tree;

import org.terasology.module.sandbox.API;

/**
 * Base class for decorator nodes, which have exactly one child (the decorated node).
 *
 */
@API
public abstract class DecoratorNode extends Node {
    protected Node child;

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        T visit = super.visit(item, visitor);
        if (child != null) {
            child.visit(visit, visitor);
        }
        return visit;
    }

    @Override
    public void insertChild(int index, Node newChild) {
        this.child = newChild;
    }

    @Override
    public void setChild(int index, Node newChild) {
        this.child = newChild;
    }

    @Override
    public Node removeChild(int index) {
        Node old = child;
        child = null;
        return old;
    }

    @Override
    public Node getChild(int index) {
        return child;
    }

    @Override
    public int getChildrenCount() {
        return child == null ? 0 : 1;
    }

    @Override
    public int getMaxChildren() {
        return 1;
    }

    public Node getChild() {
        return child;
    }

    public void setChild(Node child) {
        this.child = child;
    }

    @API
    public abstract static class DecoratorTask extends Task {
        protected DecoratorTask(Node node) {
            super(node);
        }

        @Override
        public DecoratorNode getNode() {
            return (DecoratorNode) super.getNode();
        }
    }
}
