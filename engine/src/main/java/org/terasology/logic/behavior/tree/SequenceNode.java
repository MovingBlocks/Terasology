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

import java.util.Iterator;

/**
 * Evaluates the children one by one.<br>
 * Starts next child, if previous child finishes with <b>SUCCESS</b>.<br>
 * <br>
 * <b>SUCCESS</b>: when all children finishes <b>SUCCESS</b>.<br>
 * <b>FAILURE</b>: as soon as a child finished with <b>FAILURE</b>.<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
@API
public class SequenceNode extends CompositeNode {
    @Override
    public SequenceTask createTask() {
        return new SequenceTask(this);
    }

    public static class SequenceTask extends CompositeTask {
        private Iterator<Node> iterator;
        private Node current;

        public SequenceTask(SequenceNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            iterator = getNode().children().iterator();
            if (iterator.hasNext()) {
                current = iterator.next();
                start(current);
            }
        }

        @Override
        public void handle(Status result) {
            if (result == Status.FAILURE) {
                stop(Status.FAILURE);
                return;
            }

            if (iterator.hasNext()) {
                current = iterator.next();
                start(current);
            } else {
                stop(Status.SUCCESS);
            }
        }


        @Override
        public Status update(float dt) {
            return Status.RUNNING;
        }

        @Override
        public SequenceNode getNode() {
            return (SequenceNode) super.getNode();
        }

    }

}
