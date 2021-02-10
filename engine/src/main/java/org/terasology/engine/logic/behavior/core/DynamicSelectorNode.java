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

import java.util.BitSet;

/**
 * Works like a normal selector but each update tick, all children are checked for state changes.
 */
public class DynamicSelectorNode extends CompositeNode {
    private BitSet constructed;

    @Override
    public String getName() {
        return "dynamic";
    }

    @Override
    public BehaviorNode deepCopy() {
        DynamicSelectorNode result = new DynamicSelectorNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;
    }

    @Override
    public void construct(Actor actor) {
        constructed = new BitSet(children.size());
    }

    @Override
    public BehaviorState execute(Actor actor) {
        BehaviorState result;
        for (int i = 0; i < children.size(); i++) {
            BehaviorNode child = children.get(i);
            if (!constructed.get(i)) {
                child.construct(actor);
                constructed.set(i);
            }
            result = child.execute(actor);
            if (result == BehaviorState.RUNNING) {
                return BehaviorState.RUNNING;
            }
            child.destruct(actor);
            constructed.clear(i);
            if (result == BehaviorState.SUCCESS) {
                return BehaviorState.SUCCESS;
            }
        }
        return BehaviorState.FAILURE;
    }

    @Override
    public void destruct(Actor actor) {
    }

}
