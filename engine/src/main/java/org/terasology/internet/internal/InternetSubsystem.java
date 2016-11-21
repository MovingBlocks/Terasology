/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.internet.internal;

import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.internet.InternetManager;

/**
 * @author soniex2
 */
public class InternetSubsystem implements EngineSubsystem {
    @Override
    public String getName() {
        return "Internet";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        rootContext.put(InternetManager.class, new InternetManagerImpl());
    }
}
