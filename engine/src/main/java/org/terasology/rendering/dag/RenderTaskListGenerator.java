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

import com.google.api.client.util.Maps;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO: Add javadocs
 */
public final class RenderTaskListGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RenderTaskListGenerator.class);
    private List<Object> intermediateList;
    private List<RenderPipelineTask> taskList;

    public RenderTaskListGenerator() {
        taskList = Lists.newArrayList();
        intermediateList = Lists.newArrayList();
    }

    public List<RenderPipelineTask> generateFrom(List<Node> orderedNodes) {
        generateIntermediateList(orderedNodes);
        logList("-- Intermediate List --", intermediateList); // TODO: remove in the future or turn it into debug
        generateTaskList();
        logList("-- Task List --", taskList); // TODO: remove in the future or turn it into debug
        return taskList;
    }

    private void logList(String title, List<?> list) {
        logger.info(title);
        for (Object o : list) {
            logger.info(o.toString());
        }
    }

    private void generateTaskList() {
        // TODO: Optimization task: verify if we can avoid clearing the whole list
        // TODO: whenever changes in the render graph or in the intermediate list arise
        // TODO: think about refactoring (a heavy method)
        taskList.clear();

        List<Object> semiReducedStateChanges = Lists.newArrayList(); // TODO: a better name, any suggestions?
        Map intranodesStateChanges = Maps.newHashMap();

        for (Object object : intermediateList) {
            if (object instanceof StateChange) {
                intranodesStateChanges.put(object.getClass(), object);
            } else {
                Iterator iterator = intranodesStateChanges.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();

                    semiReducedStateChanges.add(entry.getValue());
                    iterator.remove();
                }

                semiReducedStateChanges.add(object);
            }
        }

        // Add remaining state changes
        for (Object stateChange : intranodesStateChanges.values()) {
            semiReducedStateChanges.add(stateChange);
        }

        Map persistentStateChanges = Maps.newHashMap();
        for (Object object : semiReducedStateChanges) {
            if (object instanceof StateChange) {
                StateChange stateChange = (StateChange) object;
                if (!persistentStateChanges.containsKey(object.getClass())) {
                    persistentStateChanges.put(object.getClass(), object);
                    taskList.add(stateChange.generateTask());
                } else {
                    StateChange persistenStateChange = (StateChange) persistentStateChanges.get(object.getClass());
                    if (!persistenStateChange.compare(stateChange)) {
                        persistentStateChanges.put(stateChange.getClass(), stateChange);
                        taskList.add(stateChange.generateTask());
                    }
                }
            } else {
                taskList.add(((Node) object).generateTask());
            }
        }


    }

    private void generateIntermediateList(List<Node> orderedNodes) {
        intermediateList.clear();
        for (Node node : orderedNodes) {
            intermediateList.addAll(node.getDesiredStateChanges());
            intermediateList.add(node);
            // Add state changes to reset all desired state changes back to default.
            for (StateChange stateChange : node.getDesiredStateChanges()) {
                intermediateList.add(stateChange.getDefaultInstance());
            }
        }
    }
}
