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

/**
 *
 */
public class RenderPipelineOptimizer {
    private RenderPipeline renderPipeline;

    public RenderPipelineOptimizer(RenderPipeline renderPipeline) {
        this.renderPipeline = renderPipeline;
    }

    public List<PipelineTask> optimize() {
        List<PipelineTask> taskList = Lists.newArrayList();
        for (Object object : renderPipeline) {
            // TODO: eliminate redundant steps here

            PipelineTask task = generateTask(object);
            if (task != null) {
                taskList.add(task);
            }
        }
        return taskList;
    }

    private PipelineTask generateTask(Object object) {
        PipelineTask task = null;
        if (object instanceof Node) {
            task = new NodeTask((Node) object);
        } else if (object instanceof StateChange) {
            task = generateStateChangeTask((StateChange) object);
        }
        return task;
    }

    private PipelineTask generateStateChangeTask(StateChange object) {
        PipelineTask task = null;
        if (object instanceof WireframeStateChange) {
            task = new WireframeTask(object);
        }

        return task;
    }
}
