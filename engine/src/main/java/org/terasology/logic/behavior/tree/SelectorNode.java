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

import org.terasology.engine.API;

import java.util.Iterator;

/**
 * <strong>Selector</strong> <code>Composite</code>
 * <p/>
 * Evaluates the children one by one.
 * Starts next child, if previous child finishes with <code>FAILURE</code>.
 * <p/>
 * <code>SUCCESS</code>: as soon as a child finishes <code>SUCCESS</code>.
 * <code>FAILURE</code>: when all children finished with <code>FAILURE</code>.
 * <p/>
 * Auto generated javadoc - modify README.markdown instead!
 */
@API
public class SelectorNode extends CompositeNode {
    @Override
    public SelectorTask createTask() {
        return new SelectorTask(this);
    }

    public static class SelectorTask extends CompositeTask {
        private Iterator<Node> iterator;
        private Node current;

        public SelectorTask(SelectorNode node) {
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
            if (result == Status.SUCCESS) {
                stop(Status.SUCCESS);
                return;
            }

            if (iterator.hasNext()) {
                current = iterator.next();
                start(current);
            } else {
                stop(Status.FAILURE);
            }
        }

        @Override
        public Status update(float dt) {
            return Status.RUNNING;
        }

        @Override
        public SelectorNode getNode() {
            return (SelectorNode) super.getNode();
        }
    }
}
