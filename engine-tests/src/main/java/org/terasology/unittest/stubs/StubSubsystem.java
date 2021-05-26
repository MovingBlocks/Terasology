// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.unittest.stubs;

import org.terasology.engine.core.subsystem.EngineSubsystem;

public class StubSubsystem implements EngineSubsystem {
    final String name;

    StubSubsystem(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
