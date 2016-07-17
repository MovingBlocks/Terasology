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

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Add javadocs
 */
public final class RenderTaskListGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RenderTaskListGenerator.class);

    public RenderTaskListGenerator() {

    }

    public List<RenderPipelineTask> generateFrom(List<Node> orderedNodes) {
        logger.info("Task List: "); // TODO: remove in the future or turn it into debug
        List<RenderPipelineTask> taskList = Lists.newArrayList();
        for (Node node : orderedNodes) {

            // TODO: add state changes to reset all desired state changes back to default.

            // TODO: eliminate redundant steps here
            for (StateChange stateChange : node.getStateChanges()) {
                taskList.add(generateTask(stateChange));
            }

            RenderPipelineTask task = generateTask(node);
            // TODO: remove the null check when the system does not allow for StateChange implementations
            // TODO: without corresponding RenderPipelineTask implementation.
            taskList.add(task);
        }

        return taskList;
    }

    private RenderPipelineTask generateTask(Object object) {
        logger.info(object.getClass().getSimpleName()); // TODO: remove in the future or turn it into debug
        if (object instanceof StateChange) {
            return ((StateChange) object).generateTask();
        } else {
            return new NodeTask((Node) object);
        }
    }

}
