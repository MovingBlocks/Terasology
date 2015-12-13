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
 * Base class for nodes in a behavior tree. Each node must implement the create() method to create tasks, that are
 * evaluated by an interpreter.
 * <br><br>
 * Node properties may be stored in this class, while state properties for a specific interpreter run be placed in the
 * task class (i.e. actual/next children to evaluate).
 *
 */
@API
public abstract class Node implements TreeAccessor<Node> {
    public abstract Task createTask();

    @Override
    public String toString() {
        String name = getClass().getSimpleName();
        return name.substring(0, name.length() - 4);
    }

    @Override
    public void insertChild(int index, Node child) {
        throw new IllegalStateException("Not allowed");
    }

    @Override
    public void setChild(int index, Node child) {
        throw new IllegalStateException("Not allowed");
    }

    @Override
    public Node removeChild(int index) {
        throw new IllegalStateException("Not allowed");
    }

    @Override
    public Node getChild(int index) {
        throw new IllegalStateException("Not allowed");
    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

    @Override
    public int getMaxChildren() {
        return 0;
    }

    /**
     * Visitor pattern to visit the complete tree.
     */
    public <T> T visit(T item, Visitor<T> visitor) {
        return visitor.visit(item, this);
    }

    public interface Visitor<T> {
        T visit(T item, Node node);
    }
}
