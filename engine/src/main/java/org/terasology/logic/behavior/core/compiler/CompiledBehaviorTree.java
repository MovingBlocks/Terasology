/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.behavior.core.compiler;

import com.google.common.collect.Maps;
import org.terasology.logic.behavior.core.Action;
import org.terasology.logic.behavior.core.ActionNode;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BehaviorNode;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.logic.behavior.core.BehaviorTreeRunner;

import java.util.Map;

/**
 * Base class for all compiled behavior trees.
 */
public abstract class CompiledBehaviorTree implements BehaviorTreeRunner {
    public Actor actor;
    private Map<Integer, Action> actionMap = Maps.newHashMap();
    private BehaviorState result = BehaviorState.UNDEFINED;

    @Override
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Override
    public Actor getActor() {
        return actor;
    }

    public void bind(BehaviorNode node) {
        if (node instanceof ActionNode) {
            ActionNode actionNode = (ActionNode) node;
            actionMap.put(actionNode.getAction().getId(), actionNode.getAction());
        }
        for (int i = 0; i < node.getChildrenCount(); i++) {
            BehaviorNode behaviorNode = node.getChild(i);
            bind(behaviorNode);
        }
    }

    public abstract int run(int state);

    @Override
    public BehaviorState step() {
        result = BehaviorState.values()[run(result.ordinal())];
        return result;
    }

    public void setAction(int id, Action action) {
        actionMap.put(id, action);
    }

    public Action getAction(int id) {
        return actionMap.get(id);
    }

    public Map<Integer, Action> getActionMap() {
        return actionMap;
    }
}
