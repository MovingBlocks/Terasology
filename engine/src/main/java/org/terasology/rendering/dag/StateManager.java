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
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_LINE;

/**
 * TODO: add javadocs
 */
public class StateManager {

    private Map<String, Map<State, Boolean>> stateChanges; // TODO: better name?

    public StateManager() {
        stateChanges = Maps.newHashMap();
    }

    void findStateChanges(Collection<Node> nodes) {
        Map<State, Boolean> states = Maps.newHashMap();

        for (Node node : nodes) {
            Map<State, Boolean> plannedStateChanges = Maps.newHashMap();
            for (Map.Entry<State, Boolean> entry : node.getDesiredStates().entrySet()) {
                State state = entry.getKey();
                boolean value = entry.getValue();

                // TODO: investigate options for simplifying this if-clause
                if (states.get(state) == null) {
                    plannedStateChanges.put(state, value);
                } else if (states.get(state) != null && states.get(state) != value) {
                    plannedStateChanges.put(state, value);
                }

            }

            stateChanges.put(node.getIdentifier(), plannedStateChanges);
        }
    }


    public void prepareFor(Node node) {
        Map<State, Boolean> plannedStateChanges = stateChanges.get(node.getIdentifier());
        for (Map.Entry<State, Boolean> entry : plannedStateChanges.entrySet()) {
            changeState(entry.getKey(), entry.getValue());
        }
    }

    private void changeState(State state, Boolean value) { // TODO: Moving this method into a separate class?
        switch (state) {
            case WIREFRAME:
                if (value) {
                    GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                } else {
                    GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                }
                break;
        }
    }


}
