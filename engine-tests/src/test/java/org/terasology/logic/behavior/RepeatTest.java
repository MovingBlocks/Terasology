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

import junit.framework.Assert;
import org.junit.Test;
import org.terasology.logic.behavior.tree.CounterNode;
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
 * @author synopia
 */
public class RepeatTest {
    @Test
    public void testRepeatEndless() {
        Interpreter interpreter = new Interpreter(null);
        RepeatNode repeat = new RepeatNode(create(new Mocker() {
            @Override
            public void mock(Task spy) {
                when(spy.update(anyInt())).thenReturn(Status.SUCCESS);
            }
        }));

        Task task = repeat.create();

        interpreter.start(task);
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
        RepeatNode repeat = new RepeatNode(create(new Mocker() {
            @Override
            public void mock(Task spy) {
                when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS);
            }
        }));

        Task task = repeat.create();

        interpreter.start(task);
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
    public void testFilter() {
        Interpreter interpreter = new Interpreter(null);
        Node mock = create(new Mocker() {
            @Override
            public void mock(Task spy) {
                when(spy.update(anyInt())).thenReturn(Status.RUNNING);
            }
        });

        ParallelNode move = new ParallelNode(ParallelNode.Policy.RequireOne, ParallelNode.Policy.RequireOne);

        move.children().add(new CounterNode(3));
        move.children().add(mock);
        Task task = move.create();

        interpreter.start(task);
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
        return new Node() {
            @Override
            public Task create() {
                Task spy = spy(new Task(null) {
                    @Override
                    public Status update(float dt) {
                        return null;
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
