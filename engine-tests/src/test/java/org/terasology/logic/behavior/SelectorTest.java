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
import org.terasology.logic.behavior.tree.SelectorNode;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 */
public class SelectorTest {
    @Test
    public void testTwoChildrenSucceeds() {
        final Task[] spies = new Task[2];
        Interpreter interpreter = new Interpreter(null);
        Node one = create(spy -> {
            when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS);
            spies[0] = spy;
        });
        Node two = create(spy -> spies[1] = spy);
        SelectorNode node = new SelectorNode();
        node.children().add(one);
        node.children().add(two);

        Task selector = interpreter.start(node);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, selector.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.SUCCESS, selector.getStatus());
        verify(spies[0]).onTerminate(Status.SUCCESS);
        Assert.assertNull(spies[1]);
    }

    @Test
    public void testTwoContinues() {
        final Task[] spies = new Task[2];
        Interpreter interpreter = new Interpreter(null);
        Node one = create(spy -> {
            when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.FAILURE);
            spies[0] = spy;
        });
        Node two = create(spy -> {
            when(spy.update(anyInt())).thenReturn(Status.RUNNING);
            spies[1] = spy;
        });
        SelectorNode node = new SelectorNode();
        node.children().add(one);
        node.children().add(two);

        Task selector = interpreter.start(node);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, selector.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, selector.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, selector.getStatus());

        verify(spies[0]).onTerminate(Status.FAILURE);
        verify(spies[1]).onInitialize();
    }

    @Test
    public void testOnePassThrough() {
        final Task[] spies = new Task[1];
        Status[] stats = new Status[]{Status.SUCCESS, Status.FAILURE};
        for (final Status status : stats) {
            Interpreter interpreter = new Interpreter(null);
            Node mock = create(spy -> {
                when(spy.update(0)).thenReturn(Status.RUNNING, status);
                spies[0] = spy;
            });
            SelectorNode node = new SelectorNode();

            node.children().add(mock);

            Task task = interpreter.start(node);
            interpreter.tick(0);
            Assert.assertEquals(Status.RUNNING, task.getStatus());
            interpreter.tick(0);
            Assert.assertEquals(status, task.getStatus());
            verify(spies[0]).onTerminate(status);
        }
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
