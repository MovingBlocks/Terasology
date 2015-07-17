/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.engine;

import com.google.api.client.util.Lists;
import com.google.common.base.Preconditions;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.engine.subsystem.common.TimeSubsystem;

import java.util.List;

/**
 *
 */
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
