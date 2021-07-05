// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        node = treeBuilder.fromJson("{ sequence:[ success, { delay:{duration:1}}, { print:{msg:Hello} }, " +
                "{ delay:{duration:1}}, { print:{msg:World} } ] }");
        DefaultBehaviorTreeRunner tree = new DefaultBehaviorTreeRunner(node, actor);
        for (int i = 0; i < 100; i++) {
            tree.step();
        }

        treeBuilder.registerDecorator("repeat", Repeat.class);
        actor = new Actor(null);
        actor.setDelta(0.1f);
        node = treeBuilder.fromJson(
                "{ sequence:[ "
                        + "{repeat :{ count:5, child:{print:{msg:x}}}}, success, { delay:{duration:1}}, " +
                        "{ print:{msg:Hello} }, { delay:{duration:1}}, { print:{msg:World} } "
                        + "] }");
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
