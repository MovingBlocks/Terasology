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
package org.terasology.logic.behavior.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;
import org.terasology.module.sandbox.API;

/**
 * Logs a message into the console when called and returns SUCCESS
 */
@API
@BehaviorAction(name = "log")
public class LogAction extends BaseAction {
    public static final Logger logger = LoggerFactory.getLogger(LogAction.class
    );

    private String message;

    @Override
    public void construct(Actor actor) {
        actor.setValue(getId(), message);
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        logger.info(String.format("Actor %s logs message: %s ", actor.getEntity().toString(), actor.getValue(getId())));
        return BehaviorState.SUCCESS;
    }


}
