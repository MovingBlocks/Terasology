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
import org.terasology.rendering.dag.tasks.MarkerTask;

import java.util.List;
import java.util.Map;

/**
 * Instances of this class are responsible for generating the the list of tasks
 * the renderer executes to (eventually) generate the image shown to the user.
 *
 * Tasks are generated out of an ordered list of nodes in the render graph. Each node provides the set
 * of state changes it needs. Tasks are generated for each state change unless they are redundant.
 * State changes from a node are redundant if the previous node want the exact same state change.
 *
 * After tasks for the non-redundant state changes desired by a node have been generated a task
 * is appended to the list, to execute the node's process() method, which is where any rendering
 * actually happens.
 *
 * Finally, each node also provides a list of state change resets: state changes bringing a property
 * back to its default. Tasks are generated also these unless they'd be redundant. A state change
 * reset is redundant if the next node needs to set that property to a non-default value.
 *
 * It should be noted that nodes are skipped if they are disabled.
 *
 */
public final class RenderTaskListGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RenderTaskListGenerator.class);
    private List<RenderPipelineTask> taskList;
    private List<Node> nodeList;

    public RenderTaskListGenerator() {
        taskList = Lists.newArrayList();
    }

    private void logIntermediateRendererListForDebugging(List<Node> orderedNodes) {

        for (Node node : orderedNodes) {
            if (node.isEnabled()) {

                // printing out node name
                logger.info(String.format(("----- %s"), node.getClass().getSimpleName()));

                // printing out individual desired state changes
                for (StateChange desiredStateChange : node.getDesiredStateChanges()) {
                    logger.info(desiredStateChange.toString());
                }

                // printing out process() statement
                logger.info(String.format("%s: process()", node.toString()));

                // printing out individual state resets
                for (StateChange desiredStateReset : node.getDesiredStateResets()) {
                    logger.info(desiredStateReset.toString());
                }
            }
        }
    }

    /**
     * See the RenderTaskListGenerator class Javadoc for an overview of what this method does.
     *
     * @param orderedNodes a list of Node instances, ordered to reflect the dependencies between them,
     *                     i.e. Node A must be processed before Node B to work correctly.
     * @return an optimized list of RenderPipelineTask instances,
     *         ready to be iterated over to execute a frame worth of rendering
     */

    public List<RenderPipelineTask> generateFrom(List<Node> orderedNodes) {

        long startTimeInNanoSeconds = System.nanoTime();

        // TODO: Optimization task: verify if we can avoid clearing the whole list
        // TODO: whenever changes in the render graph or in the intermediate list arise
        // TODO: think about refactoring (a heavy method)

        nodeList = orderedNodes;
        taskList.clear();
        Node nextEnabledNode;
        StateChange persistentStateChange;
        Map persistentStateChanges = Maps.newHashMap();  // assuming we can't make it a private field for the time being

        int enabledNodes = 0;
        int potentialTasks = 0;
        int currentIndex = 0;

        for (Node node : orderedNodes) {
            if (node.isEnabled()) {

                if (logger.isInfoEnabled()) {
                    // Marker tasks just add a dividing line to the logger output
                    taskList.add(new MarkerTask(node.getClass().getSimpleName()));
                    enabledNodes++; // we count them only for statistical purposes
                    potentialTasks += node.getDesiredStateChanges().size() + 1 + node.getDesiredStateResets().size();
                }

                // generating tasks for the desired state changes
                for (StateChange currentStateChange : node.getDesiredStateChanges()) {

                    // State changes persist beyond the node that request them if following nodes
                    // require the exact same change.
                    persistentStateChange = (StateChange) persistentStateChanges.get(currentStateChange.getClass());

                    // for a state change to be necessary there can't be an identical one already persisting
                    if (persistentStateChange == null || !currentStateChange.equals(persistentStateChange)) {
                        // no persistent state change of this subType found: the state change is necessary (not redundant)
                        taskList.add(currentStateChange.generateTask());
                        persistentStateChanges.put(currentStateChange.getClass(), currentStateChange);

                    } // else: the state change is redundant - we don't generate a task for it
                }

                // task executing the node.process() method
                taskList.add(node.generateTask());

                // generating tasks for the desired state resets
                nextEnabledNode = findNextEnabledNode(orderedNodes, currentIndex + 1);
                if (nextEnabledNode != null) {

                    // if there is one enabled node after this one we must check its desired state changes,
                    // to make sure we don't reset to default something it would set again.
                    // For example: no point binding the display (the default FBO) if the next enabled node
                    // will bind another FBO.
                    for (StateChange currentStateReset : node.getDesiredStateResets()) {
                        if (sameClassStateChangeNotFoundInThe(nextEnabledNode, currentStateReset)) {
                            taskList.add(currentStateReset.generateTask());
                            persistentStateChanges.remove(currentStateReset.getClass());
                        }
                    }

                } else {
                    // there are no enabled nodes after this one: we must generate all the tasks necessary
                    // to reset the effects of any persisting state change.
                    for (StateChange currentStateReset : node.getDesiredStateResets()) {
                        taskList.add(currentStateReset.generateTask());
                        persistentStateChanges.remove(currentStateReset.getClass());
                    }
                }
            }

            currentIndex++;
        }

        long endTimeInNanoSeconds = System.nanoTime();

        if (logger.isInfoEnabled()) {
            logger.info("===== INTERMEDIATE RENDERER LIST =========================");
            logIntermediateRendererListForDebugging(orderedNodes);
            logger.info("===== RENDERER TASK LIST =================================");
            logList(taskList);
            logger.info("----------------------------------------------------------");
            logger.info(String.format("Task list generated in %.3f ms", (endTimeInNanoSeconds - startTimeInNanoSeconds) / 1000000f));
            logger.info(String.format("%s nodes, %s enabled - %s tasks (excluding marker tasks) out of %s potential tasks.",
                    nodeList.size(), enabledNodes, taskList.size() - enabledNodes, potentialTasks));
            logger.info("----------------------------------------------------------");
        }

        return taskList;
    }

    private boolean sameClassStateChangeNotFoundInThe(Node nextEnabledNode, StateChange stateChangeReset) {

        for (StateChange stateChange : nextEnabledNode.getDesiredStateChanges()) {
            if (stateChange.getClass() == stateChangeReset.getClass()) {
                return false; // we did find it! And yes, returning false is correct, see method name.

                // note: we don't worry about the details of the two state changes (i.e. if they are value-identical)
                // as this is something that will be checked while iterating over next node's desired state changes,
                // in the first part of method generateTaskList.
            }
        }

        return true; // we didn't find a state change of the same class

    }

    private Node findNextEnabledNode(List<Node> orderedNodeList, int startIndex) {

        for (int index = startIndex; index < orderedNodeList.size(); index++) {
            Node currentNode = orderedNodeList.get(index);
            if (currentNode.isEnabled()) {
                return currentNode;
            }
        }

        return null;
    }

    private void logList(List<?> list) {
        for (Object object : list) {
            logger.info(object.toString());
        }
    }

    /**
     * Forces a refresh of the task list using the latest node list provided to the generateFrom method.
     *
     * A refresh is useful when one of the nodes has been enabled or disabled, as the tasks associated
     * with it need to be added to the task list or removed from it. Tasks "downstream" of the change
     * need to be re-evaluated then, as they might have become redundant.
     *
     * At this stage the refresh uses a brute-force approach: the whole task list is cleared and regenerated.
     * Eventually it will be useful to make sure that only tasks affected by a change are regenerated.
     */
    public void refresh() {
        generateFrom(nodeList);
    }
}
