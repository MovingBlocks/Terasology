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

/**
 * A general purpose, data agnostic implementation of {@link Tree}.
 * <p>
 * Stores a single object. May have a single parent and an arbitrary amount of children.
 *
 * @param <T> Type of objects stored in the tree.
 */
public class GenericTree<T> extends Tree<T> {
    public GenericTree(T value) {
        this.setValue(value);
    }

    @Override
    public boolean acceptsChild(Tree<T> child) {
        return child != null;
    }

    @Override
    public void addChild(T childValue) {
        this.addChild(new GenericTree<>(childValue));
    }

    @Override
    public Tree<T> copy() {
        Tree<T> copy = new GenericTree<>(this.value);
        copy.setExpanded(this.isExpanded());

        for (Tree<T> child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }
}
