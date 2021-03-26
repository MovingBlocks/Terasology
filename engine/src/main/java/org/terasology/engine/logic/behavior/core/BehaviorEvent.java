// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

/**
 * WIP - part of the event handling solution, will be rewritten
 * TODO maybe change naming not to confuse with the engine's Events?
 */
public class BehaviorEvent {
    private final String name;

    public BehaviorEvent(String name) {
        this.name = name;
    }
}
