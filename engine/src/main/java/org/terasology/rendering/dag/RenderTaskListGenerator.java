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
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

/**
 * TODO: Add javadocs
 */
public final class RenderTaskListGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RenderTaskListGenerator.class);
    private List<Object> intermediateList;
    private List<RenderPipelineTask> taskList;
    private List<Node> nodeList;

    public RenderTaskListGenerator() {
        taskList = Lists.newArrayList();
        intermediateList = Lists.newArrayList();
    }

    public List<RenderPipelineTask> generateFrom(List<Node> orderedNodes) {
        nodeList = orderedNodes;
        generateIntermediateList(orderedNodes);
        generateTaskList();
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

        StateChange stateChange;
        StateChange persistentStateChange;
        Map persistentStateChanges = Maps.newHashMap();  // assuming we can't make it a private field for the time being
        Map intranodesStateChanges = Maps.newHashMap();

        taskList.clear();

        for (Object object : intermediateList) {

            if (object instanceof StateChange) {
                intranodesStateChanges.put(object.getClass(), object);

            } else {
                for (Object preCastStateChange : intranodesStateChanges.values()) {
                    stateChange = (StateChange) preCastStateChange;

                    persistentStateChange = (StateChange) persistentStateChanges.get(stateChange.getClass());
                    if (persistentStateChange == null) {
                        if (!stateChange.isTheDefaultInstance()) { // yep, new (convenience) method, just for readability
                            // defensive programming here: the check is probably unnecessary as for every default
                            // instance of a state change subType there should be a non-default one already in the
                            // persistentStateChangeS map, falling within cases handled below.
                            taskList.add(stateChange.generateTask());
                            persistentStateChanges.put(stateChange.getClass(), stateChange);

                        } // else {
                        // do nothing: we do not want a back-to-default state change if there was no known
                        // non-default state change already taking place.
                        // }
                    } else {
                        if (stateChange.isTheDefaultInstance()) { // I know, I'm a sucker for almost plain-english code
                            // non redundant default state change, eliminates subType entry in the map, to keep map small
                            taskList.add(stateChange.generateTask());
                            persistentStateChanges.remove(stateChange.getClass());

                        } else if (!stateChange.isEqualTo(persistentStateChange)) { // another new method, just for readability
                            // non-redundant state change of the same subType but different value, becomes new map entry
                            taskList.add(stateChange.generateTask());
                            persistentStateChanges.put(stateChange.getClass(), stateChange);

                        } // else {
                        // the non-redundant state change being examined is not a default one and is identical
                        // in value to the one stored in the persistentStateChange map: it is redundant after all
                        // and we ignore it
                        // }
                    }

                }

                intranodesStateChanges.clear();
                taskList.add(((Node) object).generateTask());
            }
        }
        logList("-- Task List --", taskList); // TODO: remove in the future or turn it into debug
    }

    private void generateIntermediateList(List<Node> orderedNodes) {
        intermediateList.clear();
        for (Node node : orderedNodes) {
            node.setTaskListGenerator(this);
            intermediateList.addAll(node.getDesiredStateChanges());
            intermediateList.add(node);
            // Add state changes to reset all desired state changes back to default.
            for (StateChange stateChange : node.getDesiredStateChanges()) {
                StateChange defaultInstance = stateChange.getDefaultInstance();
                intermediateList.add(defaultInstance);
            }
        }
        logList("-- Intermediate List --", intermediateList); // TODO: remove in the future or turn it into debug
    }

    public void refresh() {
        generateIntermediateList(nodeList);
        generateTaskList();
    }

}
