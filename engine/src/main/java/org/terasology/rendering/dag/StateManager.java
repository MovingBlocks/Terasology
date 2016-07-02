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
import java.util.Collection;
import java.util.Map;
import org.terasology.rendering.dag.states.State;
import org.terasology.rendering.dag.states.StateType;
import org.terasology.rendering.dag.states.StateTypeImpl;
import org.terasology.rendering.dag.states.StateValue;
import org.terasology.rendering.dag.states.Wireframe;

/**
 * TODO: add javadocs
 */
public class StateManager {

    private Map<String, Map<StateType, StateValue>> stateChanges; // TODO: better name?
    private Map<StateType, State> availableStates;


    public StateManager() {
        stateChanges = Maps.newHashMap();
        availableStates = Maps.newHashMap();
    }

    public static StateManager createDefault() {
        StateManager stateManager = new StateManager();
        stateManager.addAvailableStates();
        return stateManager;
    }


    public void addState(StateType stateType, State state) {
        availableStates.put(stateType, state);
    }

    public void findStateChanges(Collection<Node> nodes) {
        Map<StateType, StateValue> states = Maps.newHashMap();

        for (Node node : nodes) {
            Map<StateType, StateValue> plannedStateChange = Maps.newHashMap();

            Map<StateType, StateValue> desiredStates = node.getDesiredStates();
            // handle desired states
            for (Map.Entry<StateType, StateValue> entry : desiredStates.entrySet()) {
                StateType stateType = entry.getKey();
                StateValue value = entry.getValue();

                // TODO: investigate options for simplifying this if-clause
                if (states.get(stateType) == null) {
                    plannedStateChange.put(stateType, value);
                    states.put(stateType, value);
                } else {
                    if (states.get(stateType) != value) {
                        plannedStateChange.put(stateType, value);
                        states.put(stateType, value);
                    }
                }
            }

            // disable unconsidered but enabled states
            for (StateType stateType : states.keySet()) {
                if (desiredStates.get(stateType) == null) {
                    if (states.get(stateType) == StateValue.ENABLED) {
                        states.put(stateType, StateValue.DISABLED);
                        plannedStateChange.put(stateType, StateValue.DISABLED);
                    }
                }
            }


            stateChanges.put(node.getIdentifier(), plannedStateChange);
        }
    }

    public Map<String, Map<StateType, StateValue>> getScheduledStateChanges() {
        return stateChanges;
    }


    public void prepareFor(Node node) {
        Map<StateType, StateValue> plannedStateChanges = stateChanges.get(node.getIdentifier());
        for (Map.Entry<StateType, StateValue> entry : plannedStateChanges.entrySet()) {
            changeState(entry.getKey(), entry.getValue());
        }
    }

    private void addAvailableStates() {
        addState(StateTypeImpl.WIREFRAME, new Wireframe());
    }

    private void changeState(StateType stateType, StateValue value) { // TODO: Moving this method into a separate class?
        availableStates.get(stateType).set(value);
    }
}
