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
package org.terasology.logic.behavior.core;

import org.terasology.logic.behavior.BehaviorAction;
import org.terasology.module.sandbox.API;

/**
 * BaseAction that uses BehaviorAction annotation as its name.
 */
@API
public abstract class BaseAction implements Action {
    private transient int id;
    protected transient boolean constructed;

    @Override
    public void setup() {
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return getClass().getAnnotation(BehaviorAction.class).name();
    }

    @Override
    public void construct(Actor actor) {
    }

    @Override
    public boolean prune(Actor actor) {
        return false;
    }

    @Override
    public BehaviorState modify(Actor actor, BehaviorState result) {
        return BehaviorState.UNDEFINED;
    }

    @Override
    public void destruct(Actor actor) {
    }
}
