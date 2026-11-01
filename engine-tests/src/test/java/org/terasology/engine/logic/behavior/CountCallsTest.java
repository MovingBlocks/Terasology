// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BehaviorNode;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.logic.behavior.core.BehaviorTreeBuilder;
import org.terasology.engine.logic.behavior.core.DelegateNode;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CountCallsTest {
    public int nextId2;
    private final List<Integer> constructCalled = Lists.newArrayList();
    private final List<Integer> destructCalled = Lists.newArrayList();
    private final List<Integer> executeCalled = Lists.newArrayList();
    private GsonBuilder gsonBuilder;

    @BeforeEach
    public void init() {
        constructCalled.clear();
        destructCalled.clear();
        executeCalled.clear();
        nextId2 = 1;

        gsonBuilder = new GsonBuilder();
        BehaviorTreeBuilder builder = new BehaviorTreeBuilder() {
            @Override
            public BehaviorNode createNode(BehaviorNode node) {
                return new CountDelegate(node);
            }
        };
        gsonBuilder.registerTypeAdapter(BehaviorNode.class, builder);
        //        gsonBuilder.registerTypeAdapter(Action.class, new InheritanceAdapter<Action>("delay", Delay.class));
    }

    public void assertBT(String tree, List<BehaviorState> result, List<Integer> executed) {
        assertBT(tree, result, executed, false);
        constructCalled.clear();
        destructCalled.clear();
        executeCalled.clear();
        nextId2 = 1;
        assertBT(tree, result, executed, true);
    }

    // TODO check why ignoredStep is there and tested.
    public void assertBT(String tree, List<BehaviorState> result, List<Integer> executed, boolean ignoredStep) {
        BehaviorNode node = fromJson(tree);

        node.construct(null);
        List<BehaviorState> actualStates =
                IntStream.range(0, result.size()).mapToObj(i -> node.execute(null)).collect(Collectors.toList());
        node.destruct(null);

        assertEquals(result, actualStates);
        assertEquals(executed, executeCalled);
    }

    public BehaviorNode fromJson(String json) {
        return gsonBuilder.create().fromJson(json, BehaviorNode.class);
    }

    private class CountDelegate extends DelegateNode {
        private final int id;

        CountDelegate(BehaviorNode delegate) {
            super(delegate);
            id = nextId2;
            nextId2++;
        }

        @Override
        public BehaviorState execute(Actor actor) {
            executeCalled.add(id);
            return super.execute(actor);
        }

        @Override
        public void construct(Actor actor) {
            super.construct(actor);
            constructCalled.add(id);
        }

        @Override
        public void destruct(Actor actor) {
            destructCalled.add(id);
            super.destruct(actor);
        }
    }
}
