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

import org.terasology.rendering.nui.properties.Range;

/**
 * Starts the decorated node.<br>
 * <br>
 * <b>SUCCESS</b>: as soon as decorated node finishes with <b>SUCCESS</b>.<br>
 * <b>FAILURE</b>: after x seconds.<br>
 * <br>
 * Auto generated javadoc - modify README.markdown instead!
 */
public class TimerNode extends DecoratorNode {
    @Range(min = 0, max = 20)
    private float time;

    @Override
    public Task createTask() {
        return new TimerTask(this);
    }

    public static class TimerTask extends DecoratorTask {
        private float remainingTime;

        public TimerTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            remainingTime = getNode().time;
            if (getNode().child != null) {
                start(getNode().child);
            }
        }

        @Override
        public Status update(float dt) {
            remainingTime -= dt;
            if (remainingTime <= 0) {
                return Status.FAILURE;
            }
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            if (result == Status.SUCCESS) {
                stop(result);
            }
        }

        @Override
        public TimerNode getNode() {
            return (TimerNode) super.getNode();
        }
    }
}
