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

import org.terasology.config.Config;
import org.terasology.engine.modes.GameState;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.input.InputSystem;
import org.terasology.registry.CoreRegistry;

public class HeadlessInput implements EngineSubsystem {

    @Override
    public void initializeEarly() {
    }

    @Override
    public void initializeLate(Config config) {
        initControls();
    }

    @Override
    public void updateEarly(GameState currentState, float delta) {
    }

    @Override
    public void updateLate(GameState currentState, float delta) {
    }

    @Override
    public void shutdown(Config config) {
    }

    @Override
    public void dispose() {
    }

    private void initControls() {
        InputSystem inputSystem = new InputSystem();
        CoreRegistry.putPermanently(InputSystem.class, inputSystem);
    }

}
