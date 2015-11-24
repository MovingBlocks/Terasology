/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.headless;

import org.terasology.context.Context;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.subsystem.common.TimeSubsystem;
import org.terasology.engine.subsystem.headless.device.TimeSystem;

public class HeadlessTimer implements TimeSubsystem {

    private EngineTime time;

    @Override
    public String getName() {
        return "Time";
    }

    @Override
    public void preInitialise(Context context) {
        initTimer(context);
    }

    private void initTimer(Context context) {
        time = new TimeSystem();
        context.put(Time.class, time);
    }

    @Override
    public EngineTime getEngineTime() {
        return time;
    }
}
