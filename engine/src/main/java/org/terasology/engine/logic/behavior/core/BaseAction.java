// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.terasology.engine.logic.behavior.BehaviorAction;
import org.terasology.context.annotation.API;

/**
 * BaseAction that uses BehaviorAction annotation as its name.
 */
@API
public abstract class BaseAction implements Action {
    protected transient boolean constructed;
    private transient int id;

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
