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

import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.logic.behavior.core.Actor;
import org.terasology.logic.behavior.core.BaseAction;
import org.terasology.logic.behavior.core.BehaviorState;

/*
  This is a test-related action.
  TODO move this to engine-tests once a cleaner Gestalt option is available
 */
@BehaviorAction(name = "print")
public class Print extends BaseAction {
    public static StringBuilder output = new StringBuilder();

    private String msg;

    @Override
    public void construct(Actor actor) {
        output.append("[");
    }

    @Override
    public void destruct(Actor actor) {
        output.append("]");
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        output.append(msg);
        return BehaviorState.SUCCESS;
    }
}
