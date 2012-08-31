/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import java.util.LinkedList;

import javax.vecmath.Vector2f;

import org.terasology.rendering.gui.framework.events.StateButtonAction;

import com.google.gson.internal.Pair;

/**
 * This class extends the UIButton and adds functionality to add states to a button. Each state will be assigned a label and a action to execute as the button enters the state.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStateButton extends UIButton {
    private final LinkedList<Pair<String, StateButtonAction>> _states = new LinkedList<Pair<String, StateButtonAction>>();
    private int _currentState = -1;
    
    public UIStateButton(Vector2f size) {
        super(size, UIButton.eButtonType.NORMAL);
    }

    /**
     * Add a new state to the button.
     * @param state The label which will be set as the button enters this state.
     * @param action The action which will be executed as the button enters this state.
     * @return Returns the state ID.
     */
    public int addState(String state, StateButtonAction action) {
        _states.add(new Pair<String, StateButtonAction>(state, action));
        return _states.size() - 1;
    }
    
    /**
     * Remove a specific state. This will change the current state to the next state in the list.
     * @param stateID The id of the state.
     */
    public void removeState(int stateID) {
        if (_states.size() > 0)
        {
            if (stateID < 0)
                stateID = 0;
            if (stateID >= _states.size())
                stateID = _states.size() - 1;
            
            _states.remove(stateID);
            
            if (_states.size() == 0)
            {
                _currentState = -1;
                getLabel().setText("");
            } else {
                nextState();
            }
        }
    }
    
    /**
     * Changes the state to the given state ID.
     * @param stateID The ID of the state.
     */
    public void setState(int stateID) {
        if (_states.size() > 0)
        {
            if (stateID < 0)
                stateID = 0;
            if (stateID >= _states.size())
                stateID = _states.size() - 1;
            
            getLabel().setText(_states.get(stateID).first);
            _currentState = stateID;
            
            if (_states.get(stateID).second != null)
                _states.get(stateID).second.action(this);
        }
    }
    
    /**
     * Get the current state.
     * @return Returns the current state ID or -1 if button isn't in a state.
     */
    public int getState() {
        return _currentState;
    }
    
    /**
     * Change state to next state ID in the state list. As the end is reached the next state will be the first state in the list.
     */
    public void nextState() {
        if (_states.size() > 0)
        {
            int nextState = _currentState + 1;
            
            if (nextState >= _states.size())
                nextState = 0;
            
            setState(nextState);
        }
    }
    
    /**
     * Change state to previous state ID in the state list. As the beginning is reached the next state will be the last state in the list.
     */
    public void previousState() {
        if (_states.size() > 0)
        {
            int prevState = _currentState - 1;
            
            if (prevState < 0)
                prevState = _states.size() - 1;
            
            setState(prevState);
        }
    }
}
