package org.terasology.rendering.gui.components;

import com.google.gson.internal.Pair;

import javax.vecmath.Vector2f;

import org.terasology.rendering.gui.framework.IStateButtonAction;

import java.util.LinkedList;

/**
 * This class extends the UIButton and adds functionality to add states to a button. Each state will be assigned a label and a action to execute as the button enters the state.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 */
public class UIStateButton extends UIButton {
	private final LinkedList<Pair<String, IStateButtonAction>> _states = new LinkedList<Pair<String, IStateButtonAction>>();
    private int _currentState = -1;
    
    public UIStateButton(Vector2f size) {
		super(size);
	}

    /**
     * Add a new state to the button.
     * @param state The label which will be set as the button enters this state.
     * @param action The action which will be executed as the button enters this state.
     * @return Returns the state ID.
     */
    public int addState(String state, IStateButtonAction action) {
    	_states.add(new Pair<String, IStateButtonAction>(state, action));
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
