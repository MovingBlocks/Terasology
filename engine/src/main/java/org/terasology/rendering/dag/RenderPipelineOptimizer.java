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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
class RenderPipelineOptimizer {
    private static final Logger logger = LoggerFactory.getLogger(RenderPipelineOptimizer.class);
    private List<Object> renderPipeline;
    private Map<Class<? extends StateChange>, Class<? extends RenderPipelineTask>> stateChangeTaskMap;
    private DefaultStateChangeStorage defaultStateChangeStorage; // TODO: use default storage

    RenderPipelineOptimizer(List<Object> renderPipeline) {
        this.renderPipeline = renderPipeline;
        stateChangeTaskMap = Maps.newHashMap();
        stateChangeTaskMap.put(WireframeStateChange.class, WireframeTask.class);
        defaultStateChangeStorage = new DefaultStateChangeStorage();
    }

    List<RenderPipelineTask> optimize() {
        List<RenderPipelineTask> taskList = Lists.newArrayList();

        for (Object object : renderPipeline) {
            // TODO: eliminate redundant steps here

            RenderPipelineTask task = generateTask(object);
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
            return generateStateChangeTask((StateChange) object);
        } else {
            return new NodeTask((Node) object);
        }
    }

    private RenderPipelineTask generateStateChangeTask(StateChange object) {
        Class<? extends RenderPipelineTask> taskClass = stateChangeTaskMap.get(object.getClass());

        try {
            Constructor constructor = taskClass.getConstructor(Object.class);
            Object value = object.getValue();
            RenderPipelineTask task = (RenderPipelineTask) constructor.newInstance(value);

            return task;
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            logger.error("Failed to instantiate task class: {}", taskClass, e);
            return null;
        }
    }
}
