// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.actions.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.engine.logic.behavior.core.Actor;

/**
 * Condition decorator node.
 * <p>
 * Checks for specific conditions on Components of a given Entity.
 * If componentPresent is specified, checks that the component is present.
 * If componentAbsent is specified, checks that the component is absent.
 * if the 'values' field is specified, checks the values
 * <p>
 * If all conditions checked against are true, runs child and passes on its state;
 * If a condition doesn't hold, returns FAILURE and doesn't run the child.
 */
@BehaviorAction(name = "guard", isDecorator = true)
public class GuardAction extends ConditionAction {

    private static final Logger logger = LoggerFactory.getLogger(GuardAction.class);

    @Override
    public boolean prune(Actor actor) {

        try {
            return !condition(actor);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found. Does the Component specified exist?", e);
        } catch (NoSuchFieldException e) {
            logger.error("Field not found. Does the field specified in 'values' exist in the Component specified in 'componentPresent'?", e);
        } catch (IllegalAccessException e) {
            logger.error("Illegal access. Do we have access to the Component in question?", e);
        }
        return false;
    }

}
