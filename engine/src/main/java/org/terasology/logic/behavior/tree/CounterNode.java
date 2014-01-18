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
import org.terasology.rendering.nui.properties.OneOf;
import org.terasology.rendering.nui.properties.Range;

/**
 * Counter node. Returns RUNNING as long as not counted down to zero.
 *
 * @author synopia
 */
@API
public class CounterNode extends DecoratorNode {
    @Range(min = 0, max = 100)
    private int limit;

    public CounterNode() {
    }

    public CounterNode(int limit, Node child) {
        this.limit = limit;
        this.child = child;
    }

    @Override
    public CounterTask create() {
        return new CounterTask(this);
    }

    public static class CounterTask extends DecoratorTask implements Task.Observer {
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
            } else if( count==0 ) {
                if( getNode().child!=null ) {
                    interpreter().start(getNode().child, this);
                }
                count--;
            }
            return Status.RUNNING;
        }

        @Override
        public void handle(Status result) {
            interpreter().stop(this, result);
        }

        @Override
        public CounterNode getNode() {
            return (CounterNode) super.getNode();
        }

    }
}
