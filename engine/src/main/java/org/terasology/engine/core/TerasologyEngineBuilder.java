// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.core.subsystem.common.TimeSubsystem;

import java.util.List;

public class TerasologyEngineBuilder {

    private TimeSubsystem timeSubsystem;

    private List<EngineSubsystem> otherSubsystems = Lists.newArrayList();

    public TerasologyEngineBuilder add(TimeSubsystem time) {
        this.timeSubsystem = time;
        return this;
    }

    public TerasologyEngineBuilder add(EngineSubsystem other) {
        otherSubsystems.add(other);
        return this;
    }


    public TerasologyEngine build() {
        Preconditions.checkState(timeSubsystem != null, "TimeSubsystem is required");
        return new TerasologyEngine(timeSubsystem, otherSubsystems);
    }
}
