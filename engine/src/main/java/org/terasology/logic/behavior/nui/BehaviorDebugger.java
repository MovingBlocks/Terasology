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

import org.terasology.logic.behavior.BehaviorNodeFactory;
import org.terasology.logic.behavior.asset.BehaviorTree;
import org.terasology.logic.behavior.tree.Interpreter;
import org.terasology.logic.behavior.tree.Node;
import org.terasology.logic.behavior.tree.Status;

/**
 */
public class BehaviorDebugger implements Interpreter.Debugger {
    private BehaviorTree tree;
    private int ticksToRun = -1;

    private BehaviorNodeFactory nodeFactory;

    public BehaviorDebugger(BehaviorNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
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
        if (tree == null) {
            return;
        }
        RenderableNode renderableNode = tree.getRenderableNode(node);
        if (renderableNode != null) {
            renderableNode.setStatus(status);
        }
    }

    @Override
    public void nodeUpdated(Node node, Status status) {
        if (tree == null) {
            return;
        }
        RenderableNode renderableNode = tree.getRenderableNode(node);
        if (renderableNode != null) {
            renderableNode.setStatus(status);
        }
    }

    @Override
    public void started() {
        if (tree == null) {
            return;
        }
        for (RenderableNode renderableNode : tree.getRenderableNodes(nodeFactory)) {
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

    public void setTree(BehaviorTree selectedTree) {
        tree = selectedTree;
        started();
    }
}
