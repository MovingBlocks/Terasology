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
package org.terasology.logic.behavior.nui;

import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;

/**
 * Created by synopia on 08.01.14.
 */
public class BehaviorDebugger implements Interpreter.Debugger {
    private final BehaviorTree tree;
    private int ticksToRun = -1;

    public BehaviorDebugger(BehaviorTree tree) {
        this.tree = tree;
        started();
    }

    public void pause() {
        ticksToRun = 0;
    }

    public void run() {
        ticksToRun = -1;
    }

    public void step() {
        ticksToRun = 1;
    }

    @Override
    public void nodeFinished(Node node, Status status) {
        RenderableNode renderableNode = tree.getRenderableNode(node);
        renderableNode.setStatus(status);
    }

    @Override
    public void nodeUpdated(Node node, Status status) {
        RenderableNode renderableNode = tree.getRenderableNode(node);
        renderableNode.setStatus(status);
    }

    @Override
    public void started() {
        for (RenderableNode renderableNode : tree.getRenderableNodes()) {
            renderableNode.setStatus(null);
        }
    }

    @Override
    public boolean beforeTick() {
        return ticksToRun != 0;
    }

    @Override
    public void afterTick() {
        if (ticksToRun > 0) {
            ticksToRun--;
        }
    }

}
