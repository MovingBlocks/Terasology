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

import java.util.Iterator;

/**
 * Runs all children, until one finishes with SUCCESS
 */
public class SelectorNode extends CompositeNode {
    private Iterator<BehaviorNode> iterator;
    private BehaviorNode current;

    @Override
    public String getName() {
        return "selector";
    }

    @Override
    public BehaviorNode deepCopy() {
        SelectorNode result = new SelectorNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;

    }

    @Override
    public void construct(Actor actor) {
        iterator = children.iterator();
        nextChild(actor);
    }

    @Override
    public BehaviorState execute(Actor actor) {
        BehaviorState result;
        while (current != null) {
            result = current.execute(actor);
            if (result == BehaviorState.RUNNING) {
                return BehaviorState.RUNNING;
            }
            current.destruct(actor);
            if (result == BehaviorState.SUCCESS) {
                return BehaviorState.SUCCESS;
            } else {
                nextChild(actor);
            }
        }
        return BehaviorState.FAILURE;
    }

    private void nextChild(Actor actor) {
        if (iterator.hasNext()) {
            current = iterator.next();
            current.construct(actor);
        } else {
            current = null;
        }
    }

    @Override
    public void destruct(Actor actor) {
    }

}
