// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.common;

import org.terasology.engine.core.EngineTime;
import org.terasology.engine.core.subsystem.EngineSubsystem;

public interface TimeSubsystem extends EngineSubsystem {

    EngineTime getEngineTime();
}
