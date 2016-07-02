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
import java.util.Map;
import org.terasology.rendering.dag.states.StateType;
import org.terasology.rendering.dag.states.StateValue;


public abstract class AbstractNode implements Node {
    protected Map<StateType, StateValue> desiredStates;
    private String identifier;

    public AbstractNode(String id) {
        this.identifier = id;
        desiredStates = Maps.newHashMap();
    }

    protected void addDesiredState(StateType stateType, StateValue value) {
        desiredStates.put(stateType, value);
    }

    public Map<StateType, StateValue> getDesiredStates() {
        return desiredStates;
    }

    public String getIdentifier() {
        return identifier;
    }
}
