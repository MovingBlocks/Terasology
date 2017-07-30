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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.registry.In;

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
    protected String componentPresent;
    protected String componentAbsent;
    protected String[] values;

    @In
    ModuleManager moduleManager;

    @In
    ComponentLibrary componentLibrary;

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        try {
            if (!condition(actor)) {
                return BehaviorState.FAILURE;
            }
            return BehaviorState.SUCCESS;
        } catch (ClassNotFoundException e) {
            logger.error("Class not found. Does the Component specified exist?", e);
        } catch (NoSuchFieldException e) {
            logger.error("Field not found. Does the field specified in 'values' (publicly) exist in the Component specified in 'componentPresent'?", e);
        } catch (IllegalAccessException e) {
            logger.error("Illegal access. Do we have access to the Component in question?", e);
        }
        return BehaviorState.FAILURE;
    }

    protected boolean condition(Actor actor) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        boolean passing = true;

        if (componentAbsent != null) {
            if (actor.hasComponent(componentLibrary.resolve(componentAbsent).getType())) {
                passing = false;
            }
        }
        if (componentPresent != null) {
            Component component = actor.getComponent(componentLibrary.resolve(componentPresent).getType());
            if (component == null) {
                passing = false;
            } else {
                // Check values
                if (values != null) {
                    for (String value : values) {
                        String[] tokens = value.split(" ");
                        Object fieldVal = component.getClass().getDeclaredField(tokens[0]).get(component);

                        // Can't use a switch for this :(
                        if (fieldVal instanceof Boolean) {
                            switch (tokens[1]) {
                                case "=":
                                case "==":
                                    passing = (Boolean) fieldVal == Boolean.parseBoolean(tokens[2]);
                                    break;
                                case "!":
                                case "!=":
                                    passing = (Boolean) fieldVal != Boolean.parseBoolean(tokens[2]);
                                    break;
                                default:
                                    logger.error("Unsupported operation for boolean values: {}", tokens[1]);

                            }

                        } else if (fieldVal instanceof Number) {
                            switch (tokens[1]) {
                                case "=":
                                case "==":
                                    passing = (Double) fieldVal == Double.parseDouble(tokens[2]);
                                    break;
                                case "!":
                                case "!=":
                                    passing = (Double) fieldVal == Double.parseDouble(tokens[2]);
                                    break;
                                case "<=":
                                    passing = ((Number) fieldVal).doubleValue() <= Double.parseDouble(tokens[2]);
                                    break;
                                case ">=":
                                    passing = ((Number) fieldVal).doubleValue() >= Double.parseDouble(tokens[2]);
                                    break;
                                case ">":
                                    passing = ((Number) fieldVal).doubleValue() > Double.parseDouble(tokens[2]);
                                    break;
                                case "<":
                                    passing = ((Number) fieldVal).doubleValue() < Double.parseDouble(tokens[2]);
                                    break;
                                default:
                                    logger.error("Unsupported operation for numeric values: {}", tokens[1]);

                            }

                        } else if (fieldVal instanceof String) {
                            switch (tokens[1]) {
                                case "=":
                                case "==":
                                    passing = fieldVal.equals(tokens[2]);
                                    break;
                                case "!":
                                case "!=":
                                    passing = !fieldVal.equals(tokens[2]);
                                    break;
                                default:
                                    logger.error("Unsupported operation for strings: {}", tokens[1]);

                            }
                        }

                    }

                }

            }
        }
        return passing;
    }

}
