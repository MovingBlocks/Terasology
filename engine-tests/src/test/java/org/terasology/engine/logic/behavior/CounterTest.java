// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.logic.behavior.actions.CounterAction;
import org.terasology.engine.logic.behavior.actions.Print;
import org.terasology.engine.logic.behavior.actions.TimeoutAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.engine.logic.behavior.core.BehaviorTreeRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CounterTest {
    private BehaviorTreeBuilder treeBuilder;

    @Test
    public void test() {
        assertRun("{ sequence:[ { print:{msg:A} } ] }", 1, "[A]");
        assertRun("{ sequence:[ { print:{msg:A} }, { print:{msg:B} }  ] }", 1, "[A][B]");
        assertRun("{ sequence:[ { print:{msg:A} }, failure, { print:{msg:B} }  ] }", 1, "[A]");

        assertRun("{ sequence:[ { counter:{ count=1, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }",
                1, "[A][B]");
        assertRun("{ sequence:[ { counter:{ count=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }",
                1, "[A]");
        assertRun("{ sequence:[ { counter:{ count=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }",
                2, "[A][A][B]");
        assertRun("{ sequence:[ { counter:{ count=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }",
                4, "[A][A][B][A][A][B]");

        assertRun("{ sequence:[ { counter:{ count=2, child:{ counter:{ count=2, child:{ print:{msg:A} } } } } },{ print:{msg:B} } ] }",
                4, "[A][A][A][A][B]");
        assertRun("{ sequence:[ { timeout:{ time=1, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }",
                2, "[A][B][A][B]");
        assertRun("{ sequence:[ { timeout:{ time=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }",
                4, "[A][B][A][B][A][B][A][B]");
        assertRun("{ sequence:[ { timeout:{ time=1, child:{ timeout:{ time=2, child:{ print:{msg:A} } } } } },{ print:{msg:B} } ] }",
                2, "[A][B][A][B]");
    }

    @BeforeEach
    public void setup() {

        treeBuilder = new BehaviorTreeBuilder(new ContextImpl());
        treeBuilder.registerAction("print", Print.class);
        treeBuilder.registerDecorator("counter", CounterAction.class);
        treeBuilder.registerDecorator("timeout", TimeoutAction.class);

    }

    private void assertRun(String tree, int executions, String expectedOutput) {
        Print.output = new StringBuilder();
        BehaviorNode node = treeBuilder.fromJson(tree);
        String json = treeBuilder.toJson(node);
        BehaviorNode n2 = treeBuilder.fromJson(json);
        String json2 = treeBuilder.toJson(n2);
        assertEquals(json, json2);
        Actor actor = new Actor(null);
        actor.setDelta(0.5f);
        BehaviorTreeRunner runner = new DefaultBehaviorTreeRunner(node, actor);
        for (int i = 0; i < executions; i++) {
            runner.step();
        }

        assertEquals(expectedOutput, Print.output.toString());
    }
}
