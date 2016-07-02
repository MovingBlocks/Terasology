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


import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.terasology.rendering.dag.states.AbstractState;
import org.terasology.rendering.dag.states.StateType;
import org.terasology.rendering.dag.states.StateValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StateManagerTest {
    @Test
    public void testOneStateTwoNodes() {
        StateManager stateManager = new StateManager();
        DummyState dummyStateA = new DummyState();
        stateManager.addState(DummyStateType.A, dummyStateA);
        DummyNode node1 = instantiateDummyNodeWithState("node1", DummyStateType.A, StateValue.ENABLED);
        DummyNode node2 = instantiateDummyNodeWithState("node2", DummyStateType.A, StateValue.ENABLED);

        Collection<Node> dummyPipeline = createDummyPipeline(node1, node2);
        stateManager.findStateChanges(dummyPipeline);

        // run pipeline
        for (Node node : dummyPipeline) {
            stateManager.prepareFor(node);
        }

        assertEquals(StateValue.ENABLED, getScheduleChange(stateManager, node1, DummyStateType.A));
        assertNull(getScheduleChange(stateManager, node2, DummyStateType.A));
        assertEquals("1", dummyStateA.getHistory());
    }

    @Test
    public void testOneStateFourNodes() {
        StateManager stateManager = new StateManager();
        DummyState dummyStateA = new DummyState();
        stateManager.addState(DummyStateType.A, dummyStateA);
        DummyNode node1 = instantiateDummyNodeWithState("node1", DummyStateType.A, StateValue.ENABLED);
        DummyNode node2 = instantiateDummyNodeWithState("node2", DummyStateType.A, StateValue.ENABLED);
        DummyNode node3 = new DummyNode("node3");
        DummyNode node4 = new DummyNode("node4");

        Collection<Node> dummyPipeline = createDummyPipeline(node1, node2, node3, node4);
        stateManager.findStateChanges(dummyPipeline);

        // run pipeline
        for (Node node : dummyPipeline) {
            stateManager.prepareFor(node);
        }

        assertEquals("10", dummyStateA.getHistory());
    }


    @Test
    public void testOneStateSixNodes() {
        StateManager stateManager = new StateManager();
        DummyState dummyStateA = new DummyState();
        stateManager.addState(DummyStateType.A, dummyStateA);
        DummyNode node1 = instantiateDummyNodeWithState("node1", DummyStateType.A, StateValue.ENABLED);
        DummyNode node2 = instantiateDummyNodeWithState("node2", DummyStateType.A, StateValue.ENABLED);
        DummyNode node3 = new DummyNode("node3");
        DummyNode node4 = new DummyNode("node4");
        DummyNode node5 = instantiateDummyNodeWithState("node5", DummyStateType.A, StateValue.ENABLED);
        DummyNode node6 = instantiateDummyNodeWithState("node6", DummyStateType.A, StateValue.ENABLED);

        Collection<Node> dummyPipeline = createDummyPipeline(node1, node2, node3, node4, node5, node6);
        stateManager.findStateChanges(dummyPipeline);

        // run pipeline
        for (Node node : dummyPipeline) {
            stateManager.prepareFor(node);
        }

        assertEquals("101", dummyStateA.getHistory());
    }

    private DummyNode instantiateDummyNodeWithState(String identifier, StateType stateType, StateValue stateValue) {
        DummyNode node1 = new DummyNode(identifier);
        node1.addDependentState(stateType, stateValue);
        return node1;
    }


    private StateValue getScheduleChange(StateManager stateManager, Node node, StateType stateType) {
        return stateManager.getScheduledStateChanges().get(node.getIdentifier()).get(stateType);
    }

    private Collection<Node> createDummyPipeline(Node... nodes) {
        return Arrays.asList(nodes);
    }

    enum DummyStateType implements StateType {
        A
    }

    class DummyState extends AbstractState {
        private StringBuilder stringBuilder = new StringBuilder();

        DummyState() {
        }

        @Override
        public void set(StateValue stateValue) {
            super.set(stateValue);
            switch (stateValue) {
                case DISABLED:
                    stringBuilder.append('0');
                    break;
                case ENABLED:
                    stringBuilder.append('1');
                    break;
            }
        }

        public String getHistory() {
            return stringBuilder.toString();
        }
    }


    class DummyNode extends AbstractNode {

        DummyNode(String id) {
            super(id);
        }

        @Override
        public void initialise() {

        }

        @Override
        public void process() {

        }

        public void addDependentState(StateType stateType, StateValue value) {
            addDesiredState(stateType, value);
        }
    }
}
