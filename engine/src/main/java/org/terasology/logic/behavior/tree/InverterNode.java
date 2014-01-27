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
package org.terasology.logic.behavior.tree;

/**
 * Inverts the decorated node.
 * <p/>
 * Finishes with SUCCESS, when decorated node finishes FAILURE
 * Finishes with FAILURE, when decorated node finishes SUCCESS
 *
 * @author synopia
 */
public class InverterNode extends DecoratorNode {
    @Override
    public Task createTask() {
        return new InverterTask(this);
    }

    public static class InverterTask extends DecoratorTask {
        public InverterTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            start(getNode().child);
        }

        @Override
        public void handle(Status result) {
            Status invert = result == Status.SUCCESS ? Status.FAILURE : result == Status.FAILURE ? Status.SUCCESS : null;
            stop(invert);
        }

        @Override
        public Status update(float dt) {
            return Status.RUNNING;
        }
    }
}
