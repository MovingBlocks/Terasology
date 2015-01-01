/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.game;

import org.terasology.engine.TerasologyEngine;
import org.terasology.engine.EngineTime;

/**
 * @author Immortius
 */
public class Game {

    private EngineTime time;

    private String name = "";
    private String seed = "";

    private TerasologyEngine terasologyEngine;

    public Game(TerasologyEngine terasologyEngine, EngineTime time) {
        this.terasologyEngine = terasologyEngine;
        this.time = time;
    }

    public void load(GameManifest manifest) {
        this.name = manifest.getTitle();
        this.seed = manifest.getSeed();
        time.setGameTime(manifest.getTime());
    }

    public String getName() {
        return name;
    }

    public EngineTime getTime() {
        return time;
    }

    public String getSeed() {
        return seed;
    }
}
