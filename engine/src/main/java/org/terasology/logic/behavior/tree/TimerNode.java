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

import org.terasology.rendering.nui.properties.OneOf;
import org.terasology.rendering.nui.properties.Range;

/**
 * Created by synopia on 19.01.14.
 */
public class TimerNode extends Node {
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    @OneOf.List(items = {SUCCESS, FAILURE})
    private String result = SUCCESS;

    @Range(min = 0, max = 20)
    private float time;

    @Override
    public Task createTask() {
        return new TimerTask(this);
    }

    public static class TimerTask extends Task {
        private float remainingTime;

        public TimerTask(Node node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            remainingTime = getNode().time;
        }

        @Override
        public Status update(float dt) {
            remainingTime -= dt;
            if (remainingTime <= 0) {
                return getNode().result == SUCCESS ? Status.SUCCESS : Status.FAILURE;
            }
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
        }

        @Override
        public TimerNode getNode() {
            return (TimerNode) super.getNode();
        }
    }
}
