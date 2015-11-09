/*
 * Copyright 2014 MovingBlocks
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
import org.terasology.logic.behavior.tree.CounterNode;
import org.terasology.logic.behavior.tree.Interpreter;

/**
 * Created by synopia on 11.01.14.
 */
public class CounterTest {

    @Test
    public void test00() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(0);
        CounterNode counterNode = new CounterNode(0, debugNode);
        interpreter.start(counterNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        Assert.assertNull(debugNode.lastTask);
        Assert.assertTrue(interpreter.tick(0) == 0);
    }

    @Test
    public void test01() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(0);
        CounterNode counterNode = new CounterNode(1, debugNode);

        interpreter.start(counterNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        Assert.assertTrue(debugNode.lastTask.updateCalled);
        Assert.assertTrue(debugNode.lastTask.initializeCalled);
        Assert.assertTrue(debugNode.lastTask.terminateCalled);
        debugNode.lastTask = null;

        Assert.assertTrue(interpreter.tick(0) == 0);
    }

    @Test
    public void test11() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(1);
        CounterNode counterNode = new CounterNode(1, debugNode);

        interpreter.start(counterNode);

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
}
