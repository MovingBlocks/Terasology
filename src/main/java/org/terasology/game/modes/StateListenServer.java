/*
 * Copyright 2012
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

package org.terasology.game.modes;

import org.terasology.game.GameEngine;

/**
 * @author Immortius
 */
// TODO: Don't extend StateSinglePlayer
public class StateListenServer extends StateSinglePlayer {

    public StateListenServer(String worldName) {
        super(worldName);
    }

    public StateListenServer(String worldName, String seed) {
        super(worldName, seed);
    }

    @Override
    public void init(GameEngine engine) {
        super.init(engine);


    }
}