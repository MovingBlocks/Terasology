/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.behavior.tree;

import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.properties.Range;

/**
 * Starts child a limit number of times.<br>
 * <br>
 * <b>SUCCESS</b>: when child finished with <b>SUCCESS</b>n times.<br>
 * <b>FAILURE</b>: as soon as child finishes with <b>FAILURE</b>.<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
@API
public class CounterNode extends DecoratorNode {
    @Range(min = 0, max = 100)
    private int limit;

    public CounterNode() {
    }

    public CounterNode(int limit, Node child) {
        this.limit = limit;
        this.child = child;
    }

    @Override
    public CounterTask createTask() {
        return new CounterTask(this);
    }

    public static class CounterTask extends DecoratorTask {
        private int count;

        public CounterTask(CounterNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            count = 0;
            if (count < getNode().limit && getNode().child != null) {
                start(getNode().child);
            }
        }

        @Override
        public Status update(float dt) {
            if (getNode().child != null) {
                return getNode().limit > 0 ? Status.RUNNING : Status.SUCCESS;
            } else {
                return count < getNode().limit ? Status.RUNNING : Status.SUCCESS;
            }
        }

        @Override
        public void handle(Status result) {
            if (result == Status.FAILURE) {
                stop(Status.FAILURE);
                return;
            }

            count++;
            if (count < getNode().limit) {
                if (getNode().child != null) {
                    start(getNode().child);
                }
            } else {
                stop(Status.SUCCESS);
            }
        }

        @Override
        public CounterNode getNode() {
            return (CounterNode) super.getNode();
        }

    }
}
