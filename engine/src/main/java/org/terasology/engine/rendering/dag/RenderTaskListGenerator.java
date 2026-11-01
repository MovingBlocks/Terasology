// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.dag;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @SuppressWarnings("PMD.GuardLogStatement")
    private void logIntermediateRendererListForDebugging(List<Node> orderedNodes) {

        for (Node node : orderedNodes) {
            if (node.isEnabled()) {

                // printing out node name
                logger.info("----- {}", node.getClass().getSimpleName());

                // printing out individual desired state changes
                for (StateChange desiredStateChange : node.getDesiredStateChanges()) {
                    logger.info("{}", desiredStateChange);
                }

                // printing out process() statement
                logger.info("{}: process()", node);
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

        Map<Class<?>, StateChange> persistentStateChanges = Maps.newHashMap();
        Set<Class<?>> requestedStateChanges = Sets.newHashSet();

        List<StateChange> stateChangesToAdd = Lists.newArrayList();

        // The following variables have been declared here to make the code clearer.
        // P.S. manu3d made me do it!
        StateChange persistentStateChange;
        Iterator<Map.Entry<Class<?>, StateChange>> iterator;
        Map.Entry<Class<?>, StateChange> entry;
        Class<?> key;
        StateChange stateChange;

        int enabledNodes = 0;
        int potentialTasks = 0;

        for (Node node : orderedNodes) {
            if (node.isEnabled()) {
                if (logger.isDebugEnabled()) {
                    // Marker tasks just add a dividing line to the logger output
                    taskList.add(new MarkerTask(node.getUri() + " (" + node.getClass().getSimpleName() + ")"));
                    enabledNodes++; // we count them only for statistical purposes
                    potentialTasks += 2 * node.getDesiredStateChanges().size() + 1;
                }

                for (StateChange currentStateChange : node.getDesiredStateChanges()) {
                    // A persistentStateChange is one that persists across the processing of two or more consecutive nodes.
                    // For instance, if consecutive nodes A and B request the exact same StateChange, the StateChange will
                    // take place during the processing of node A and will be reset to the default only after the processing of node B.
                    persistentStateChange = persistentStateChanges.get(currentStateChange.getClass());

                    // currentStateChange is different from persistentStateChange in two circumstances:
                    // 1. The previous node did not request a StateChange of the class of currentStateChange
                    // 2. The previous node requested a StateChange of the same class as currentStateChange, but with a different value
                    // Either way, currentStateChange is added to the list of things to do during rendering and is persisted to the next node.
                    if (!currentStateChange.equals(persistentStateChange)) {
                        stateChangesToAdd.add(currentStateChange);
                        persistentStateChanges.put(currentStateChange.getClass(), currentStateChange);
                    }
                    // Else: The exact same StateChange is already persisting, so don't add it again.

                    // A requestedStateChange is one that was requested by the current Node. This property, along with
                    // persistentStateChange, determines when the reset StateChange corresponding to a StateChange is added.
                    requestedStateChanges.add(currentStateChange.getClass());
                }

                // Reset all the persistentStateChanges that are not requestedStateChanges.
                iterator = persistentStateChanges.entrySet().iterator();
                while (iterator.hasNext()) {
                    entry = iterator.next();
                    key = entry.getKey();
                    stateChange = entry.getValue();

                    if (!requestedStateChanges.contains(key)) {
                        // This StateChange was not requested by the current Node, so we reset it.
                        requestedStateChanges.remove(key);
                        StateChange resetTask = stateChange.getDefaultInstance();
                        if (resetTask != null) {
                            taskList.add(resetTask);
                        }
                        iterator.remove();
                    }
                    // Else: The StateChange was requested by the current Node, so do nothing.

                    requestedStateChanges.remove(key);
                }

                // Finally add all the desirable state changes for this iteration into the task list.
                // The delay ensures that the state resets take place before new state sets, which can
                // be necessary when dealing with State Changes like LookThrough and LookThoughNormalized
                // that are essentially the same, but not according to the TaskListGenerator.
                taskList.addAll(stateChangesToAdd);
                stateChangesToAdd.clear();

                // Now that all the StateChanges requested by the Node have been added, we can finally schedule the node itself for processing.
                taskList.add(node);
            }
        }

        // Since we've reached the end of the rendering pipeline, reset all the persisting State Changes.
        for (Map.Entry<Class<?>, StateChange> stateChangeEntry : persistentStateChanges.entrySet()) {
            taskList.add(stateChangeEntry.getValue().getDefaultInstance());
        }

        long endTimeInNanoSeconds = System.nanoTime();

        if (logger.isDebugEnabled()) {
            logger.debug("===== INTERMEDIATE RENDERER LIST =========================");
            logIntermediateRendererListForDebugging(orderedNodes);
            logger.debug("===== RENDERER TASK LIST =================================");
            logList(taskList);
            logger.debug("----------------------------------------------------------");
            logger.debug(String.format("Task list generated in %.3f ms", (endTimeInNanoSeconds - startTimeInNanoSeconds) / 1000000f));
            logger.debug(String.format("%s nodes, %s enabled - %s tasks (excluding marker tasks) out of %s potential tasks.",
                    nodeList.size(), enabledNodes, taskList.size() - enabledNodes, potentialTasks));
            logger.debug("----------------------------------------------------------");
        }

        return taskList;
    }

    private void logList(List<?> list) {
        for (Object object : list) {
            logger.debug("{}", object);
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
     * visually separate the tasks related to a node from those of the previous one.
     */
    private final class MarkerTask implements RenderPipelineTask {

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
         * @return A string in the form: "{@code ----- <message>}",
         *         where {@code <message>} is the string passed to the constructor.
         */
        public String toString() {
            return String.format("----- %s", message);
        }

    }
}
