/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.rendering.world.WorldSetupWrapper;

/**
 * A class which stores the universe level properties for a game like whether
 * the game is single-player or multi-player, seed value and the game name.
 */
public class UniverseWrapper {

    private  String seed;
    private  boolean loadingAsServer;
    private  String gameName;
    private WorldSetupWrapper targetWorld;

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public String getSeed() {
        return seed;
    }

    public void setLoadingAsServer(boolean loadingAsServer) {
        this.loadingAsServer = loadingAsServer;
    }

    public boolean getLoadingAsServer() {
        return loadingAsServer;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setTargetWorld(WorldSetupWrapper targetWorld) {
        this.targetWorld = targetWorld;
    }

    public WorldSetupWrapper getTargetWorld() {
        return targetWorld;
    }
}
