/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior;

import org.junit.Assert;
import org.junit.Test;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.ParallelNode;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 */
public class RepeatTest {
    @Test
    public void test0() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(0);
        RepeatNode repeatNode = new RepeatNode(debugNode);
        interpreter.start(repeatNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask first = debugNode.lastTask2;
        Assert.assertTrue(first.updateCalled);
        Assert.assertTrue(first.initializeCalled);
        Assert.assertTrue(first.terminateCalled);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask second = debugNode.lastTask2;
        Assert.assertTrue(second.updateCalled);
        Assert.assertTrue(second.initializeCalled);
        Assert.assertTrue(second.terminateCalled);
        Assert.assertNotSame(first, second);

        Assert.assertTrue(interpreter.tick(0) > 0);
    }

    @Test
    public void test1() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(1);
        RepeatNode repeatNode = new RepeatNode(debugNode);

        interpreter.start(repeatNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask first = debugNode.lastTask;
        Assert.assertTrue(first.updateCalled);
        Assert.assertTrue(first.initializeCalled);
        Assert.assertTrue(!first.terminateCalled);
        first.reset();

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask last = debugNode.lastTask2;
        Assert.assertTrue(last.updateCalled);
        Assert.assertTrue(!last.initializeCalled);
        Assert.assertTrue(last.terminateCalled);
        Assert.assertSame(first, last);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask second = debugNode.lastTask;
        Assert.assertTrue(second.updateCalled);
        Assert.assertTrue(second.initializeCalled);
        Assert.assertTrue(!second.terminateCalled);
        Assert.assertNotSame(first, second);

        Assert.assertTrue(interpreter.tick(0) > 0);
    }

    @Test
    public void testRepeatEndless() {
        Interpreter interpreter = new Interpreter(null);
        RepeatNode repeat = new RepeatNode(create(spy -> when(spy.update(anyInt())).thenReturn(Status.SUCCESS)));

        Task task = interpreter.start(repeat);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
    }

    @Test
    public void testRepeat() {
        Interpreter interpreter = new Interpreter(null);
        RepeatNode repeat = new RepeatNode(create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS)));

        Task task = interpreter.start(repeat);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
    }

    @Test
    public void testFilter() {
        Interpreter interpreter = new Interpreter(null);
        Node mock = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING));

        ParallelNode move = new ParallelNode(ParallelNode.Policy.RequireOne, ParallelNode.Policy.RequireOne);

        move.children().add(new DebugNode(3));
        move.children().add(mock);
        Task task = interpreter.start(move);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.SUCCESS, task.getStatus());
    }


    private Node create(final Mocker mocker) {
        final Node node = new DebugNode(1);
        return new Node() {
            @Override
            public Task createTask() {
                Task spy = spy(new Task(null) {
                    @Override
                    public Status update(float dt) {
                        return null;
                    }

                    @Override
                    public Node getNode() {
                        return node;
                    }

                    @Override
                    public void handle(Status result) {

                    }
                });
                mocker.mock(spy);
                return spy;
            }
        };
    }

    private interface Mocker {
        void mock(Task spy);
    }
}
