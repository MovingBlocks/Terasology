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

import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.rendering.nui.properties.OneOf;

/**
 * Node that runs a behavior tree.<br>
 * <br>
 * <b>SUCCESS</b>: when tree finishes with <b>SUCCESS</b>.<br>
 * <b>FAILURE</b>: when tree finishes with <b>FAILURE</b>.<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class LookupNode extends Node {
    @OneOf.Provider(name = "behaviorTrees")
    public BehaviorTree tree;

    @Override
    public Task createTask() {
        return new LookupTask(this);
    }

    public static class LookupTask extends Task {
        public LookupTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            if (getNode().tree != null) {
                start(getNode().tree.getRoot());
            }
        }

        @Override
        public Status update(float dt) {
            return getNode().tree == null ? Status.SUCCESS : Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            stop(result);
        }

        @Override
        public LookupNode getNode() {
            return (LookupNode) super.getNode();
        }
    }
}
