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
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 */
public class ParallelTest {
    @Test
    public void testSuccessRequireAll() {
        Interpreter interpreter = new Interpreter(null);
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireAll, ParallelNode.Policy.RequireOne);

        Node one = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS));
        Node two = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.RUNNING, Status.SUCCESS));
        parallel.children().add(one);
        parallel.children().add(two);

        Task task = interpreter.start(parallel);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.SUCCESS, task.getStatus());
    }

    @Test
    public void testSuccessRequireOne() {
        Interpreter interpreter = new Interpreter(null);
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireOne, ParallelNode.Policy.RequireAll);
        Node one = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.SUCCESS));
        Node two = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.RUNNING));
        parallel.children().add(one);
        parallel.children().add(two);

        Task task = interpreter.start(parallel);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.SUCCESS, task.getStatus());
    }

    @Test
    public void testFailureRequireAll() {
        Interpreter interpreter = new Interpreter(null);
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireOne, ParallelNode.Policy.RequireAll);
        Node one = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.FAILURE));
        Node two = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.RUNNING, Status.FAILURE));
        parallel.children().add(one);
        parallel.children().add(two);

        Task task = interpreter.start(parallel);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.FAILURE, task.getStatus());
    }

    @Test
    public void testFailureRequireOne() {
        Interpreter interpreter = new Interpreter(null);
        ParallelNode parallel = new ParallelNode(ParallelNode.Policy.RequireAll, ParallelNode.Policy.RequireOne);
        Node one = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.FAILURE));
        Node two = create(spy -> when(spy.update(anyInt())).thenReturn(Status.RUNNING, Status.RUNNING));
        parallel.children().add(one);
        parallel.children().add(two);

        Task task = interpreter.start(parallel);
        interpreter.tick(0);
        Assert.assertEquals(Status.RUNNING, task.getStatus());
        interpreter.tick(0);
        Assert.assertEquals(Status.FAILURE, task.getStatus());
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
