/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.dag;

import java.util.List;

/**
 * TODO: Add javadocs
 */
public final class RenderTaskListGenerator {

    public RenderTaskListGenerator() {

    }

    public List<RenderPipelineTask> generateFrom(List<Node> orderedNodes) {
        List<RenderPipelineTask> taskList = com.google.common.collect.Lists.newArrayList();
        for (Node node : orderedNodes) {
            // TODO: add "renderPipeline.addAll(node.getDesiredStateChanges())" here
            // TODO: add state changes to reset all desired state changes back to default.

            // TODO: eliminate redundant steps here

            RenderPipelineTask task = generateTask(node);
            // TODO: remove the null check when the system does not allow for StateChange implementations
            // TODO: without corresponding RenderPipelineTask implementation.
            if (task != null) {
                taskList.add(task);
            }
        }

        return taskList;
    }

    private RenderPipelineTask generateTask(Object object) {
        if (object instanceof StateChange) {
            return ((StateChange) object).generateTask();
        } else {
            return new NodeTask((Node) object);
        }
    }
}
