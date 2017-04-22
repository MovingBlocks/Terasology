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

import java.util.Iterator;
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

        /* While examining the StateChanges for each Node, we follow the following approach:
                - Add all StateChanges for first Node to the list and mark them as relevant
                - For the remaining Nodes, examine the StateChanges.
                    ~ If the exact same StateChange was requested by last Node, just mark the same
                        StateChange as relevant and move on.
                    ~ If a StateChange of same type but having a different value was requested by last Node,
                        replace the old one with a new StateChange and mark it as relevant.
                - Remove all the StateChanges that are no longer relevant (requested by older Nodes, no longer
                    required by the current Node).
                - For the remaining StateChanges, add them to the final TaskList.
                - Mark all the StateChanges as irrelevant, so that they can be removed by next Node if they
                    are not explicitly requested there.
        */
        Map<Class<?>, StateChange> persistentStateChanges = Maps.newHashMap();
        Map<Class<?>, Boolean> persistentStateChangesRelevant = Maps.newHashMap();

        List<StateChange> stateChangesToAdd = Lists.newArrayList();

        int enabledNodes = 0;
        int potentialTasks = 0;

        for (Node node : orderedNodes) {
            if (node.isEnabled()) {
                if (logger.isInfoEnabled()) {
                    // Marker tasks just add a dividing line to the logger output
                    taskList.add(new MarkerTask(node.getClass().getSimpleName()));
                    enabledNodes++; // we count them only for statistical purposes
                    potentialTasks += 2 * node.getDesiredStateChanges().size() + 1;
                }

                // Examine the StateChanges requested by current Node, and insert/update them in persistentStateChanges.
                for (StateChange currentStateChange : node.getDesiredStateChanges()) {
                    StateChange persistentStateChange = persistentStateChanges.get(currentStateChange.getClass());

                    // This exact StateChange was not requested (or was requested with a different value by the last Node),
                    // so this StateChange is required and we insert it (or replace the old StateChange with it).
                    if (!currentStateChange.equals(persistentStateChange)) {
                        stateChangesToAdd.add(currentStateChange);
                        persistentStateChanges.put(currentStateChange.getClass(), currentStateChange);
                    }
                    // Else: The exact same StateChange is already persisting, so don't add it again.

                    // Mark the StateChange as relevant for this iteration.
                    persistentStateChangesRelevant.put(currentStateChange.getClass(), true);
                }

                // Iterate through all StateChanges
                Iterator<Map.Entry<Class<?>, StateChange>> iterator = persistentStateChanges.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Class<?>, StateChange> entry = iterator.next();

                    if (persistentStateChangesRelevant.get(entry.getKey())) {
                        // This StateChange is relevant, so mark it as irrelevant for next Node and move on.
                        persistentStateChangesRelevant.put(entry.getKey(), false);
                    } else {
                        // This StateChange is irrelevant, so add its reset task to the TaskList and remove it from persistentStateChanges.
                        persistentStateChangesRelevant.remove(entry.getKey());
                        taskList.add(entry.getValue().getDefaultInstance());
                        iterator.remove();
                    }
                }

                // Finally add all the desirable state changes for this iteration into the task list.
                // The delay ensures that the state resets take place before new state sets, which can
                // be necessary when dealing with State Changes like LookThrough and LookThoughNormalized
                // that are essentially the same, but not according to the TaskListGenerator.
                taskList.addAll(stateChangesToAdd);
                stateChangesToAdd.clear();

                // Now that all the StateChanges required by the Node have been added, we can finally add the Node to the TaskList.
                taskList.add(node);
            }
        }

        // Since we've reached the end of the rendering pipeline, reset all the persisting State Changes.
        for (Map.Entry<Class<?>, StateChange> stateChangeEntry : persistentStateChanges.entrySet()) {
            taskList.add(stateChangeEntry.getValue().getDefaultInstance());
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

    /**
     * Instances of this class are intended to be inserted in the Render Task List.
     *
     * If the content of the task list is printed out by the logger, instances of this class
     * visually separate the tasks releated to a node from those of the previous one.
     */
    private class MarkerTask implements RenderPipelineTask {

        private String message;

        /**
         * Instantiate a MarkerTask.
         *
         * @param message A string used by the toString() method.
         */
        private MarkerTask(String message) {
            this.message = message;
        }

        @Override
        public void process() { }

        /**
         * Returns a string description of the instance.
         *
         * @return A string in the form: "----- <message>",
         *         where <message> is the string passed to the constructor.
         */
        public String toString() {
            return String.format("----- %s", message);
        }

    }
}
