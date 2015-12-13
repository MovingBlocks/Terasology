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
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


/**
 */
public class InterpreterTest {
    private Status result = Status.RUNNING;
    private Node node;
    private Task task;

    @Test
    public void test0() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(0);

        interpreter.start(debugNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask first = debugNode.lastTask;
        Assert.assertTrue(first.updateCalled);
        Assert.assertTrue(first.initializeCalled);
        Assert.assertTrue(first.terminateCalled);

        Assert.assertTrue(interpreter.tick(0) == 0);
    }

    @Test
    public void test1() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(1);

        interpreter.start(debugNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask first = debugNode.lastTask;
        Assert.assertTrue(first.updateCalled);
        Assert.assertTrue(first.initializeCalled);
        Assert.assertTrue(!first.terminateCalled);
        first.reset();

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask last = debugNode.lastTask;
        Assert.assertTrue(last.updateCalled);
        Assert.assertTrue(!last.initializeCalled);
        Assert.assertTrue(last.terminateCalled);
        Assert.assertSame(first, last);

        Assert.assertTrue(interpreter.tick(0) == 0);
    }

    @Test
    public void testX() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(10);

        interpreter.start(debugNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask first = debugNode.lastTask;
        Assert.assertTrue(first.updateCalled);
        Assert.assertTrue(first.initializeCalled);
        Assert.assertTrue(!first.terminateCalled);
        first.reset();

        for (int i = 0; i < 9; i++) {
            Assert.assertTrue(interpreter.tick(0) > 0);
            DebugNode.DebugTask current = debugNode.lastTask;
            Assert.assertSame(first, current);
            Assert.assertTrue(current.updateCalled);
            Assert.assertTrue(!current.initializeCalled);
            Assert.assertTrue(!current.terminateCalled);
            current.reset();
        }

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask last = debugNode.lastTask;
        Assert.assertTrue(last.updateCalled);
        Assert.assertTrue(!last.initializeCalled);
        Assert.assertTrue(last.terminateCalled);
        Assert.assertSame(first, last);

        Assert.assertTrue(interpreter.tick(0) == 0);
    }

    @Test
    public void testInit() {
        create();
        Interpreter interpreter = new Interpreter(null);
        interpreter.start(node, null);
        interpreter.tick(0);
        verify(task).onInitialize();
    }

    @Test
    public void testUpdate() {
        create();
        Interpreter interpreter = new Interpreter(null);
        interpreter.start(node, null);
        interpreter.tick(0);
        verify(task).update(anyInt());
    }

    @Test
    public void testNoTerminate() {
        create();
        Interpreter interpreter = new Interpreter(null);
        interpreter.start(node, null);
        interpreter.tick(0);
        verify(task, never()).onTerminate(any(Status.class));
    }

    @Test
    public void testTerminate() {
        create();
        Interpreter interpreter = new Interpreter(null);
        interpreter.start(node, null);
        interpreter.tick(0);
        result = Status.SUCCESS;
        interpreter.tick(0);
        verify(task).onTerminate(Status.SUCCESS);
    }

    private void create() {
        node = new Node() {
            @Override
            public Task createTask() {
                task = spy(new Task(null) {
                    @Override
                    public Status update(float dt) {
                        return result;
                    }

                    @Override
                    public void handle(Status handleResult) {

                    }
                });
                return task;
            }
        };
    }
}
