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
import org.terasology.rendering.nui.properties.Checkbox;
import org.terasology.rendering.nui.properties.Range;

/**
 * Counter node. Returns RUNNING as long as not counted down to zero.
 *
 * @author synopia
 */
@API
public class CounterNode extends Node {
    @Range(min = 0, max = 100)
    private int limit;

    public CounterNode() {
    }

    public CounterNode(int limit) {
        this.limit = limit;
    }

    @Override
    public CounterTask create() {
        return new CounterTask(this);
    }

    public static class CounterTask extends Task {
        private int count;

        public CounterTask(CounterNode node) {
            super(node);
        }

        @Override
        public void onInitialize() {
            count = getNode().limit;
        }

        @Override
        public Status update(float dt) {
            if (count > 0) {
                count--;
                return Status.RUNNING;
            } else {
                return Status.SUCCESS;
            }
        }

        @Override
        public CounterNode getNode() {
            return (CounterNode) super.getNode();
        }

    }
}
