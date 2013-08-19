/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.gui.widgets;

import org.terasology.rendering.gui.framework.events.StateButtonAction;

import javax.vecmath.Vector2f;
import java.util.LinkedList;

/**
 * This class extends the UIButton and adds functionality to add states to a button.
 * Each state will be assigned a label and a action to execute as the button enters the state.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         <p/>
 *         TODO integrate into UIButton
 */
public class UIStateButton extends UIButton {
    private final LinkedList<ButtonState> states = new LinkedList<>();
    private int currentState = -1;

    public UIStateButton(Vector2f size) {
        super(size, ButtonType.NORMAL);
    }

    /**
     * Add a new state to the button.
     *
     * @param state  The label which will be set as the button enters this state.
     * @param action The action which will be executed as the button enters this state.
     * @return Returns the state ID.
     */
    public int addState(String state, StateButtonAction action) {
        states.add(new ButtonState(state, action));
        return states.size() - 1;
    }

    /**
     * Remove a specific state. This will change the current state to the next state in the list.
     *
     * @param stateId The id of the state.
     */
    public void removeState(int stateId) {
        int stateToRemove = stateId;
        if (states.size() > 0) {
            if (stateToRemove < 0) {
                stateToRemove = 0;
            }
            if (stateToRemove >= states.size()) {
                stateToRemove = states.size() - 1;
            }

            states.remove(stateToRemove);

            if (states.size() == 0) {
                currentState = -1;
                getLabel().setText("");
            } else {
                nextState();
            }
        }
    }

    /**
     * Changes the state to the given state ID.
     *
     * @param stateId The ID of the state.
     */
    public void setState(int stateId) {
        int stateToSet = stateId;
        if (states.size() > 0) {
            if (stateToSet < 0) {
                stateToSet = 0;
            }
            if (stateToSet >= states.size()) {
                stateToSet = states.size() - 1;
            }

            getLabel().setText(states.get(stateToSet).name);
            currentState = stateToSet;

            if (states.get(stateId).action != null) {
                states.get(stateId).action.action(this);
            }
        }
    }

    /**
     * Get the current state.
     *
     * @return Returns the current state ID or -1 if button isn't in a state.
     */
    public int getState() {
        return currentState;
    }

    /**
     * Change state to next state ID in the state list. As the end is reached the next state will be the first state in the list.
     */
    public void nextState() {
        if (states.size() > 0) {
            int nextState = currentState + 1;

            if (nextState >= states.size()) {
                nextState = 0;
            }

            setState(nextState);
        }
    }

    /**
     * Change state to previous state ID in the state list. As the beginning is reached the next state will be the last state in the list.
     */
    public void previousState() {
        if (states.size() > 0) {
            int prevState = currentState - 1;

            if (prevState < 0) {
                prevState = states.size() - 1;
            }

            setState(prevState);
        }
    }

    private class ButtonState {
        String name;
        StateButtonAction action;

        public ButtonState(String name, StateButtonAction action) {
            this.name = name;
            this.action = action;
        }

    }
}
