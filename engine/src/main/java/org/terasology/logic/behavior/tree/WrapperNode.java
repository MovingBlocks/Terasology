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

/**
 * Always finishes with <b>SUCCESS</b>.<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class WrapperNode extends DecoratorNode {
    @Override
    public Task createTask() {
        return new WrapperTask(this);
    }

    public static class WrapperTask extends DecoratorTask {
        public WrapperTask(Node node) {
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
            stop(Status.SUCCESS);
        }
    }
}
