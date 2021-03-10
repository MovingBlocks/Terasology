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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.logic.behavior.DefaultBehaviorTreeRunner;
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

        assertRun("{ sequence:[ { counter:{ count=1, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }", 1, "[A][B]");
        assertRun("{ sequence:[ { counter:{ count=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }", 1, "[A]");
        assertRun("{ sequence:[ { counter:{ count=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }", 2, "[A][A][B]");
        assertRun("{ sequence:[ { counter:{ count=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }", 4, "[A][A][B][A][A][B]");

        assertRun("{ sequence:[ { counter:{ count=2, child:{ counter:{ count=2, child:{ print:{msg:A} } } } } },{ print:{msg:B} } ] }", 4, "[A][A][A][A][B]");
        assertRun("{ sequence:[ { timeout:{ time=1, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }", 2, "[A][B][A][B]");
        assertRun("{ sequence:[ { timeout:{ time=2, child:{ print:{msg:A} } } },{ print:{msg:B} } ] }", 4, "[A][B][A][B][A][B][A][B]");
        assertRun("{ sequence:[ { timeout:{ time=1, child:{ timeout:{ time=2, child:{ print:{msg:A} } } } } },{ print:{msg:B} } ] }", 2, "[A][B][A][B]");
    }

    @BeforeEach
    public void setup() {

        treeBuilder = new BehaviorTreeBuilder();
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
