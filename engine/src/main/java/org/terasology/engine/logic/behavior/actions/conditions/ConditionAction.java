// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;
import org.terasology.engine.logic.behavior.core.BaseAction;
import org.terasology.engine.logic.behavior.core.BehaviorState;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.Collection;

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
    private transient ModuleManager moduleManager;

    @In
    private transient ComponentLibrary componentLibrary;

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
            logger.error("Field not found. "
                    + "Does the field specified in 'values' (publicly) exist in the Component specified in 'componentPresent'?", e);
        } catch (IllegalAccessException e) {
            logger.error("Illegal access. Do we have access to the Component in question?", e);
        }
        return BehaviorState.FAILURE;
    }

    protected boolean condition(Actor actor) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        boolean passing = true;

        if (componentAbsent != null && actor.hasComponent(componentLibrary.resolve(componentAbsent).getType())) {
            passing = false;
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
                        Object fieldValue = component.getClass().getDeclaredField(tokens[1]).get(component);

                        String secondValue;
                        switch (tokens[0]) {
                            // Read second value from the definition
                            case "V":
                                secondValue = tokens[3];
                                break;
                            // Read second value from a field of the component
                            case "F":
                                secondValue = component.getClass().getDeclaredField(tokens[3]).get(component).toString();
                                break;
                            // No second value needed.
                            case "N":
                                secondValue = "";
                                break;
                            default:
                                logger.error("Unsupported guard value type: {}", tokens[0]); //NOPMD
                                secondValue = "";

                        }

                        // Can't use a switch for this :(
                        if (fieldValue instanceof Boolean) {
                            switch (tokens[2]) {
                                case "=":
                                case "==":
                                    passing = (Boolean) fieldValue == Boolean.parseBoolean(secondValue);
                                    break;
                                case "!":
                                case "!=":
                                    passing = (Boolean) fieldValue != Boolean.parseBoolean(secondValue);
                                    break;
                                default:
                                    logger.error("Unsupported operation for boolean values: {}", tokens[2]); //NOPMD

                            }

                        } else if (fieldValue instanceof Number) {
                            switch (tokens[2]) {
                                case "=":
                                case "==":
                                    passing = (Double) fieldValue == Double.parseDouble(secondValue);
                                    break;
                                case "!":
                                case "!=":
                                    passing = (Double) fieldValue != Double.parseDouble(secondValue);
                                    break;
                                case "<=":
                                    passing = ((Number) fieldValue).doubleValue() <= Double.parseDouble(secondValue);
                                    break;
                                case ">=":
                                    passing = ((Number) fieldValue).doubleValue() >= Double.parseDouble(secondValue);
                                    break;
                                case ">":
                                    passing = ((Number) fieldValue).doubleValue() > Double.parseDouble(secondValue);
                                    break;
                                case "<":
                                    passing = ((Number) fieldValue).doubleValue() < Double.parseDouble(secondValue);
                                    break;
                                default:
                                    logger.error("Unsupported operation for numeric values: {}", tokens[2]); //NOPMD

                            }

                        } else if (fieldValue instanceof String) {
                            switch (tokens[2]) {
                                case "=":
                                case "==":
                                    passing = fieldValue.equals(secondValue);
                                    break;
                                case "!":
                                case "!=":
                                    passing = !fieldValue.equals(secondValue);
                                    break;
                                default:
                                    logger.error("Unsupported operation for strings: {}", tokens[2]); //NOPMD

                            }
                        } else {
                            // Assume it's a nullable Object

                            if (fieldValue == null) {
                                if (!tokens[2].equals("null")) {
                                    // If a more complex check is requested and the field is null, fail
                                    passing = false;
                                }
                            } else {
                                switch (tokens[2]) {

                                    // Null check
                                    case "exists":
                                        if (fieldValue instanceof EntityRef && fieldValue == EntityRef.NULL) {
                                            passing = false;
                                        }
                                        break;
                                    // Collection checks
                                    case "empty":
                                        if (fieldValue instanceof Collection) {
                                            passing = ((Collection<?>) fieldValue).isEmpty();
                                        }
                                        break;
                                    case "nonEmpty":
                                        if (fieldValue instanceof Collection) {
                                            passing = !((Collection<?>) fieldValue).isEmpty();
                                        }
                                        break;

                                    default:
                                        logger.error("Unknown field type or operation: {} {}", fieldValue.getClass(), tokens[2]); //NOPMD
                                }
                            }
                        }

                    }

                }

            }
        }
        return passing;
    }

}
