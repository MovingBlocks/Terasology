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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.asset.BehaviorTreeData;
import org.terasology.logic.behavior.nui.BehaviorDebugger;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.Status;

import java.util.function.Consumer;

public class BehaviorSystemTest {
    private BehaviorSystem system;
    private EntityManager entityManager;
    private EntityRef entity;

    @Before
    public void setup() {
        system = new BehaviorSystem();
        entityManager = new PojoEntityManager();
        entity = entityManager.create(new BehaviorComponent());
    }

    @Test
    public void testStartBehavior() {
        Assert.assertNull(entity.getComponent(BehaviorComponent.class).tree);

        // create a dummy tree and ask the system to start it
        BehaviorTreeData data = new BehaviorTreeData();
        data.setRoot(new RepeatNode());
        BehaviorTree testTree = new BehaviorTree(new ResourceUrn("engine:dummybehaviortree"), new AssetType<>(BehaviorTree.class, BehaviorTree::new), data);
        system.startBehavior(entity, testTree);

        // instrument the debugger to ensure our tree is now active
        TestDebugger debugger = new TestDebugger(null);
        debugger.setCallback((Node node) -> Assert.assertEquals(testTree.getRoot(), node));
        system.getInterpreter(entity).setDebugger(debugger);

        // tick the debugger, assert that the instrumentation was fired
        system.getInterpreter(entity).tick(1.0f);
        Assert.assertTrue(debugger.isDone());
    }

    private class TestDebugger extends BehaviorDebugger {
        boolean done = false;
        Consumer<Node> callback;

        public TestDebugger(BehaviorNodeFactory nodeFactory) {
            super(nodeFactory);
        }

        @Override
        public void nodeUpdated(Node node, Status status) {
            done = true;
            callback.accept(node);
        }

        public boolean isDone() {
            return done;
        }

        public void setCallback(Consumer<Node> callback) {
            this.callback = callback;
        }
    }
}
