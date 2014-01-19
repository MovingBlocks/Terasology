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

import org.terasology.engine.API;
import org.terasology.rendering.nui.properties.OneOf;

/**
 * All children are evaluated in parallel. Policies for success and failure will define when this node finishes and in
 * which state.
 * <p/>
 * Default is to finish with SUCCESS, when all children finish successful. FAILURE as soon as one child fails.
 *
 * @author synopia
 */
@API
public class ParallelNode extends CompositeNode {
    public enum Policy {
        RequireOne,
        RequireAll
    }

    @OneOf.Enum
    private Policy successPolicy;
    @OneOf.Enum
    private Policy failurePolicy;

    public ParallelNode() {
        successPolicy = Policy.RequireAll;
        failurePolicy = Policy.RequireOne;
    }

    public ParallelNode(Policy forSuccess, Policy forFailure) {
        successPolicy = forSuccess;
        failurePolicy = forFailure;
    }

    @Override
    public ParallelTask createTask() {
        return new ParallelTask(this);
    }

    public static class ParallelTask extends CompositeTask implements Task.Observer {
        private int successCount;
        private int failureCount;

        public ParallelTask(ParallelNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            for (Node child : getNode().children()) {
                interpreter().start(child, this);
            }
            successCount = 0;
            failureCount = 0;
        }

        @Override
        public Status update(float dt) {
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            if (result == Status.SUCCESS) {
                successCount++;
                if (getNode().successPolicy == Policy.RequireOne) {
                    interpreter().stop(this, Status.SUCCESS);
                }
            }
            if (result == Status.FAILURE) {
                failureCount++;
                if (getNode().failurePolicy == Policy.RequireOne) {
                    interpreter().stop(this, Status.FAILURE);
                }
            }
            if (getNode().failurePolicy == Policy.RequireAll && failureCount == getNode().children().size()) {
                interpreter().stop(this, Status.FAILURE);
            }
            if (getNode().successPolicy == Policy.RequireAll && successCount == getNode().children().size()) {
                interpreter().stop(this, Status.SUCCESS);
            }
        }

        @Override
        public ParallelNode getNode() {
            return (ParallelNode) super.getNode();
        }
    }
}
