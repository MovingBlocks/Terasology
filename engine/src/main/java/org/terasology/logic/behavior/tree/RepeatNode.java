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

/**
 * Repeats the child node forever.<br>
 * <br>
 * <b>SUCCESS</b>: Never.<br>
 * <b>FAILURE</b>: as soon as decorated node finishes with <b>FAILURE</b>.<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
@API
public class RepeatNode extends DecoratorNode {
    public RepeatNode() {
    }

    public RepeatNode(Node child) {
        this.child = child;
    }

    @Override
    public RepeatTask createTask() {
        return new RepeatTask(this);
    }

    public static class RepeatTask extends DecoratorTask {
        public RepeatTask(RepeatNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            if (getNode().child != null) {
                start(getNode().child);
            }
        }

        @Override
        public Status update(float dt) {
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            if (result == Status.FAILURE) {
                stop(Status.FAILURE);
                return;
            }

            if (getNode().child != null) {
                start(getNode().child);
            }
        }

        @Override
        public RepeatNode getNode() {
            return (RepeatNode) super.getNode();
        }
    }

}
