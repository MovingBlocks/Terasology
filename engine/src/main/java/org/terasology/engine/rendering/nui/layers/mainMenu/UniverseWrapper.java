// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldGenerator;

/**
 * A class which stores the universe level properties for a game like whether
 * the game is single-player or multi-player, seed value, the game name, server address, and network mode.
 */
public class UniverseWrapper {

    private String seed = "";
    private boolean loadingAsServer;
    private String gameName;
    private String serverAddress = "localhost";
    private NetworkMode networkMode = NetworkMode.NONE;
    private WorldConfigurator worldConfigurator;
    private WorldGenerator worldGenerator;

    public void setSeed(String seed) {
        this.seed = seed == null ? "" : seed;
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

    public void setServerAddress(String address) {
        this.serverAddress = address;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setNetworkMode(NetworkMode mode) {
        this.networkMode = mode;
    }

    public NetworkMode getNetworkMode() {
        return networkMode;
    }

    public void setWorldConfigurator(WorldConfigurator worldConfigurator) {
        this.worldConfigurator = worldConfigurator;
    }

    public WorldConfigurator getWorldConfigurator() {
        return worldConfigurator;
    }

    public void setWorldGenerator(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    public WorldGenerator getWorldGenerator() {
        return worldGenerator;
    }
}
