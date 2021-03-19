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
package org.terasology.logic.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.DefaultBehaviorTreeRunner;
import org.terasology.engine.logic.behavior.actions.Print;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.behavior.core.BehaviorTreeBuilder;

public final class Example {

    private static final Logger logger = LoggerFactory.getLogger(Example.class);

    private Example() {
    }

    public static void main(String[] args) {
        BehaviorTreeBuilder treeBuilder = new BehaviorTreeBuilder();

        BehaviorNode node = treeBuilder.fromJson("{ sequence:[ success, success, failure ] }");
        logger.info("{}", new DefaultBehaviorTreeRunner(node, null).step());

        treeBuilder.registerAction("print", Print.class);
        node = treeBuilder.fromJson("{ sequence:[ success, { print:{msg:world} } ] }");
        logger.info("{}", new DefaultBehaviorTreeRunner(node, null).step());

        treeBuilder.registerAction("delay", Delay.class);
        Actor actor = new Actor(null);
        actor.setDelta(0.1f);
        node = treeBuilder.fromJson("{ sequence:[ success, { delay:{duration:1}}, { print:{msg:Hello} }, { delay:{duration:1}}, { print:{msg:World} } ] }");
        DefaultBehaviorTreeRunner tree = new DefaultBehaviorTreeRunner(node, actor);
        for (int i = 0; i < 100; i++) {
            tree.step();
        }

        treeBuilder.registerDecorator("repeat", Repeat.class);
        actor = new Actor(null);
        actor.setDelta(0.1f);
        node = treeBuilder.fromJson("{ sequence:[ " +
                                    "{repeat :{ count:5, child:{print:{msg:x}}}}, success, { delay:{duration:1}}, { print:{msg:Hello} }, { delay:{duration:1}}, { print:{msg:World} } "
                                    +
                                    "] }");
        tree = new DefaultBehaviorTreeRunner(node, actor);
        for (int i = 0; i < 100; i++) {
            tree.step();
        }

    }

    @BehaviorAction(name = "delay")
    public static class Delay extends BaseAction {
        private float duration;

        @Override
        public void construct(Actor actor) {
            actor.setValue(getId(), duration);
        }

        @Override
        public BehaviorState modify(Actor actor, BehaviorState result) {
            logger.info(".");
            float timeRemaining = actor.getValue(getId());
            timeRemaining -= actor.getDelta();
            actor.setValue(getId(), timeRemaining);
            return timeRemaining >= 0 ? BehaviorState.RUNNING : BehaviorState.SUCCESS;
        }

        @Override
        public void destruct(Actor actor) {

        }
    }

    @BehaviorAction(name = "repeat")
    public static class Repeat extends BaseAction {
        private int count;

        @Override
        public void construct(Actor actor) {
            actor.setValue(getId(), count);
        }

        @Override
        public BehaviorState modify(Actor actor, BehaviorState result) {
            if (result == BehaviorState.SUCCESS) {
                int remaining = actor.getValue(getId());
                remaining--;
                actor.setValue(getId(), remaining);
                return remaining > 0 ? BehaviorState.RUNNING : BehaviorState.SUCCESS;
            }
            return result;
        }
    }

}
