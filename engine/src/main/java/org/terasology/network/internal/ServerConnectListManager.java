/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.network.internal;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 *
 */

public class ServerConnectListManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectListManager.class);
    private static final Gson gson = new Gson();
    private static ServerConnectListManager instance;

    private Set blacklistedIDs;
    private Set whitelistedIDs;
    private final Path blacklistPath;
    private final Path whitelistPath;

    private ServerConnectListManager(Path blacklistFilePath, Path whitelistFilePath) {
        blacklistPath = blacklistFilePath;
        whitelistPath = whitelistFilePath;
        loadLists();
    }

    public void loadLists() {
        try {
            if (!Files.exists(blacklistPath)) {
                Files.createFile(blacklistPath);
            }
            if (!Files.exists(whitelistPath)) {
                Files.createFile(whitelistPath);
            }
            blacklistedIDs = gson.fromJson(Files.newBufferedReader(blacklistPath), Set.class);
            whitelistedIDs = gson.fromJson(Files.newBufferedReader(whitelistPath), Set.class);
        } catch (IOException e) {
            logger.error("Whitelist or blacklist files not found:", e);
        }
    }

    public String getErrorMessage(String clientID) {
        if (isClientBlacklisted(clientID)) {
            return "client on blacklist";
        }
        if (!isClientWhitelisted(clientID)) {
            return "client not on whitelist";
        }
        return null;
    }

    public static ServerConnectListManager getInstance() {
        if (instance == null) {
            Path defaultBlacklistPath = PathManager.getInstance().getHomePath().resolve("blacklist.json");
            Path defaultWhitelistPath = PathManager.getInstance().getHomePath().resolve("whitelist.json");
            instance = new ServerConnectListManager(defaultBlacklistPath, defaultWhitelistPath);
        }
        return instance;
    }

    public boolean isClientAllowedToConnect(String clientID) {
        return !isClientBlacklisted(clientID) && isClientWhitelisted(clientID);
    }

    public void addToWhitelist(String clientID) {
        whitelistedIDs.add(clientID);
    }

    public void removeFromWhitelist(String clientID) {
        whitelistedIDs.remove(clientID);
    }

    public Set<String> getWhitelist() {
        return whitelistedIDs;
    }

    public void addToBlacklist(String clientID) {
        blacklistedIDs.add(clientID);
    }

    public void removeFromBlacklist(String clientID) {
        blacklistedIDs.remove(clientID);
    }

    public Set<String> getBlacklist() {
        return blacklistedIDs;
    }

    private boolean isClientBlacklisted(String clientID) {
        return blacklistedIDs != null && blacklistedIDs.contains(clientID);
    }

    private boolean isClientWhitelisted(String clientID) {
        return whitelistedIDs == null || whitelistedIDs.contains(clientID);
    }
}
