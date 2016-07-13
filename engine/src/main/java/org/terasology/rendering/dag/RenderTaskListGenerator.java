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

import com.google.common.collect.Maps;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Add javadocs
 */
public final class RenderTaskListGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RenderTaskListGenerator.class);
    private Map<Class<? extends StateChange>, Class<? extends RenderPipelineTask>> stateChangeTaskMap;

    public RenderTaskListGenerator() {
        stateChangeTaskMap = Maps.newHashMap();
        stateChangeTaskMap.put(WireframeStateChange.class, WireframeTask.class);
        stateChangeTaskMap.put(FBOStateChange.class, FBOTask.class);

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
            return (RenderPipelineTask) constructor.newInstance(value);

        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            logger.error("Failed to instantiate task class: {}", taskClass, e);
            return null;
        }
    }
}
