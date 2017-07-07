/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.behavior.actions.conditions;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;

import java.util.List;

/**
 * Condition leaf node.
 * <p>
 * Checks for specific conditions on Components of a given Entity.
 * If componentPresent is specified, checks that the component is present.
 * If componentAbsent is specified, checks that the component is absent.
 * if the 'values' field is specified, checks the values
 * <p>
 * Returns SUCCESS if all conditions checked against are true; FAILURE if not.
 */
@BehaviorAction(name = "condition")
public class ConditionAction extends BaseAction {
    private static final Logger logger = LoggerFactory.getLogger(ConditionAction.class);
    private String componentPresent;
    private String componentAbsent;
    private String[] values;


    @Override
    public void construct(Actor actor) {

        actor.setValue(getId(), new ConditionData(componentPresent, componentAbsent, values));
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {

        try {
            if (!condition(actor)) {
                return BehaviorState.FAILURE;
            }
        } catch (ClassNotFoundException e) {
            logger.error("Class not found. Does the Component specified exist?", e);
        } catch (NoSuchFieldException e) {
            logger.error("Field not found. Does the field specified in 'values' exist in the Component specified in 'componentPresent'?", e);
        } catch (IllegalAccessException e) {
            logger.error("Illegal access. Do we have access to the Component in question?", e);
        }


        return BehaviorState.SUCCESS;
    }

    private boolean condition(Actor actor) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        boolean passing = true;
        ConditionData data = actor.getValue(getId());
        if (data.componentAbsent != null) {
            if (actor.hasComponent((Class<? extends Component>) Class.forName(data.componentAbsent))) {
                passing = false;
            }
        }

        if (data.componentPresent != null) {
            Component component = actor.getComponent((Class<? extends Component>) Class.forName(data.componentPresent));
            if (component == null) {
                passing = false;
            } else {
                // Check values
                if (data.values != null) {
                    for (int i = 0; i < data.values.length; i += 3) {
                        if (component.getClass().getField(data.values[i]).getFloat(component) < Float.parseFloat(data.values[i + 1])) {
                            passing = true;

                        }
                    }

                }


            }
        }


        return passing;
    }


}
