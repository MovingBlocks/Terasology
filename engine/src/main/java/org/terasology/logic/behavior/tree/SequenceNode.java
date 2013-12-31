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
package org.terasology.logic.behavior.tree;

import java.util.Iterator;

/**
 * Evaluates the children one by one. As soon as a child fails, this node will return FAILURE immediatly.
 * When all children are evaluated, this node succeeds.
 *
 * @author synopia
 */
public class SequenceNode extends CompositeNode {
    @Override
    public SequenceTask create() {
        return new SequenceTask(this);
    }

    public static class SequenceTask extends CompositeTask implements Task.Observer {
        private Iterator<Node> iterator;
        private Node current;

        public SequenceTask(SequenceNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            iterator = getNode().children().iterator();
            current = iterator.next();
            interpreter().start(current, this);
        }

        @Override
        public void handle(Status result) {
            if (result == Status.FAILURE) {
                interpreter().stop(this, Status.FAILURE);
                return;
            }

            if (iterator.hasNext()) {
                current = iterator.next();
                interpreter().start(current, this);
            } else {
                interpreter().stop(this, Status.SUCCESS);
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
