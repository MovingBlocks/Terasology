/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.behavior.core;

import org.terasology.nui.properties.PropertyProvider;

/**
 * Delegates all calls to a delegate node.
 */
public class DelegateNode implements BehaviorNode {
    protected final BehaviorNode delegate;

    public DelegateNode(BehaviorNode delegate) {
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public BehaviorNode deepCopy() {
        return new DelegateNode(delegate.deepCopy());
    }

    @Override
    public PropertyProvider getProperties() {
        return delegate.getProperties();
    }

    @Override
    public <T> T visit(T item, final Visitor<T> visitor) {
        T childItem = visitor.visit(item, this);
        for (int i = 0; i < getChildrenCount(); i++) {
            getChild(i).visit(childItem, visitor);
        }
        return childItem;
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        delegate.insertChild(index, child);
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        delegate.replaceChild(index, child);
    }

    @Override
    public BehaviorNode removeChild(int index) {
        return delegate.removeChild(index);
    }

    @Override
    public BehaviorNode getChild(int index) {
        return delegate.getChild(index);
    }

    @Override
    public int getChildrenCount() {
        return delegate.getChildrenCount();
    }

    @Override
    public int getMaxChildren() {
        return delegate.getMaxChildren();
    }

    @Override
    public void construct(Actor actor) {
        delegate.construct(actor);
    }

    @Override
    public BehaviorState execute(Actor actor) {
        return delegate.execute(actor);
    }

    @Override
    public void destruct(Actor actor) {
        delegate.destruct(actor);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
