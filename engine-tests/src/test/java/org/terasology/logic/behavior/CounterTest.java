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

import junit.framework.Assert;
import org.junit.Test;
import org.terasology.logic.behavior.tree.DelayNode;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.RepeatNode;
import org.terasology.logic.behavior.tree.SequenceNode;

/**
 * Created by synopia on 11.01.14.
 */
public class CounterTest {

    @Test
    public void test0_0() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(0);
        DelayNode delayNode = new DelayNode(0, debugNode);
        interpreter.start(delayNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask first = debugNode.lastTask;
        Assert.assertTrue(first.updateCalled);
        Assert.assertTrue(first.initializeCalled);
        Assert.assertTrue(first.terminateCalled);

        Assert.assertTrue(interpreter.tick(0) == 0);
    }

    @Test
    public void test0_1() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(0);
        DelayNode delayNode = new DelayNode(1, debugNode);

        interpreter.start(delayNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        Assert.assertNull(debugNode.lastTask);

        Assert.assertTrue(interpreter.tick(0) > 0);
        DebugNode.DebugTask first = debugNode.lastTask;
        Assert.assertTrue(first.updateCalled);
        Assert.assertTrue(first.initializeCalled);
        Assert.assertTrue(first.terminateCalled);
        first.reset();

        Assert.assertTrue(interpreter.tick(0) == 0);
    }

    @Test
    public void test1_1() {
        Interpreter interpreter = new Interpreter(null);
        DebugNode debugNode = new DebugNode(1);
        DelayNode delayNode = new DelayNode(1, debugNode);

        interpreter.start(delayNode);

        Assert.assertTrue(interpreter.tick(0) > 0);
        Assert.assertNull(debugNode.lastTask);

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
    public void test() {
        Interpreter interpreter = new Interpreter(null);
        SequenceNode first = new SequenceNode();
        SequenceNode left = new SequenceNode();
        first.setChild(0, left);
        RepeatNode root = new RepeatNode(first);
        root.setChild(0, first);
        first.setChild(1, new DebugNode(1));

        left.setChild(0, new DebugNode(1));
        left.setChild(1, new DebugNode(1));

        interpreter.start(root);

        for (int i = 0; i < 100; i++) {
            interpreter.tick(0);
        }
    }
}
