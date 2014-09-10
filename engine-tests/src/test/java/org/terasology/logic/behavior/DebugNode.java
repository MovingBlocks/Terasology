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

import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;
import org.terasology.logic.behavior.tree.Task;

/**
 * Created by synopia on 18.01.14.
 */
public class DebugNode extends Node {
    public DebugTask lastTask;
    public DebugTask lastTask2;
    private int limit;


    public DebugNode(int limit) {
        this.limit = limit;
    }

    @Override
    public Task createTask() {
        lastTask2 = lastTask;
        lastTask = new DebugTask(this);
        return lastTask;
    }

    public static class DebugTask extends Task {
        public boolean updateCalled;
        public boolean terminateCalled;
        public boolean initializeCalled;
        private int count;


        public DebugTask(Node node) {
            super(node);
        }

        public void reset() {
            terminateCalled = false;
            initializeCalled = false;
            updateCalled = false;
        }

        @Override
        public void onInitialize() {
            initializeCalled = true;
            count = getNode().limit;
        }

        @Override
        public Status update(float dt) {
            updateCalled = true;
            if (count > 0) {
                count--;
                return Status.RUNNING;
            } else {
                return Status.SUCCESS;
            }
        }

        @Override
        public void handle(Status result) {

        }

        @Override
        public void onTerminate(Status result) {
            terminateCalled = true;
        }

        @Override
        public DebugNode getNode() {
            return (DebugNode) super.getNode();
        }
    }
}
