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
import org.terasology.rendering.nui.properties.OneOf;

/**
 * All children are evaluated in parallel. Policies for success and failure will define when this node finishes and in which state.<br>
 * <br>
 * <b>SUCCESS</b>: when success policy is fulfilled (one or all children <b>SUCCESS</b>).<br>
 * <b>FAILURE</b>, when failure policy is fulfilled (one or all children <b>FAILURE</b>).<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
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

    public static class ParallelTask extends CompositeTask {
        private int successCount;
        private int failureCount;

        public ParallelTask(ParallelNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            getNode().children().forEach(this::start);
            successCount = 0;
            failureCount = 0;
        }

        @Override
        public Status update(float dt) {
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            if (this.getStatus() != Status.RUNNING) {
                // this happens, when this task is already stopped, because of a previously finished child task
                // currently its not simple to correctly stop child tasks, so parallel children may continue (and finish)
                // its work, even if this parallel is already finished
                return;
            }
            if (result == Status.SUCCESS) {
                successCount++;
                if (getNode().successPolicy == Policy.RequireOne) {
                    stop(Status.SUCCESS);
                }
            }
            if (result == Status.FAILURE) {
                failureCount++;
                if (getNode().failurePolicy == Policy.RequireOne) {
                    stop(Status.FAILURE);
                }
            }
            if (getNode().failurePolicy == Policy.RequireAll && failureCount == getNode().children().size()) {
                stop(Status.FAILURE);
            }
            if (getNode().successPolicy == Policy.RequireAll && successCount == getNode().children().size()) {
                stop(Status.SUCCESS);
            }
        }

        @Override
        public ParallelNode getNode() {
            return (ParallelNode) super.getNode();
        }
    }
}
