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
import org.terasology.context.Context;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.DisplayDevice;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides the methods needed to determine if a client is allowed to connect or not,
 * based on the blacklist and whitelist files.
 */

public class ServerConnectListManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectListManager.class);
    private static final Gson gson = new Gson();

    private Context context;
    private Set<String> blacklistedIDs;
    private Set<String> whitelistedIDs;
    private final Path blacklistPath;
    private final Path whitelistPath;

    public ServerConnectListManager(Context context) {
        blacklistPath = PathManager.getInstance().getHomePath().resolve("blacklist.json");
        whitelistPath = PathManager.getInstance().getHomePath().resolve("whitelist.json");
        this.context = context;
        loadLists();
    }

    @SuppressWarnings("unchecked")
    private void loadLists() {
        try {
            if (createFiles()) {
                blacklistedIDs = gson.fromJson(Files.newBufferedReader(blacklistPath), Set.class);
                whitelistedIDs = gson.fromJson(Files.newBufferedReader(whitelistPath), Set.class);
                if (blacklistedIDs == null) {
                    blacklistedIDs = new HashSet<>();
                }
                if (whitelistedIDs == null) {
                    whitelistedIDs = new HashSet<>();
                }
            }
        } catch (IOException e) {
            logger.error("Whitelist or blacklist files not found:", e);
        }
    }

    private void saveLists() {
        try {
            if (createFiles()) {
                Writer blacklistWriter = Files.newBufferedWriter(blacklistPath);
                Writer whitelistWriter = Files.newBufferedWriter(whitelistPath);
                blacklistWriter.write(gson.toJson(blacklistedIDs));
                whitelistWriter.write(gson.toJson(whitelistedIDs));
                blacklistWriter.close();
                whitelistWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean createFiles() throws IOException {
        DisplayDevice display = context.get(DisplayDevice.class);
        if (display == null || !display.isHeadless()) {
            return false;
        }
        if (!Files.exists(blacklistPath)) {
            Files.createFile(blacklistPath);
        }
        if (!Files.exists(whitelistPath)) {
            Files.createFile(whitelistPath);
        }
        return true;
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isClientAllowedToConnect(String clientID) {
        return !isClientBlacklisted(clientID) && isClientWhitelisted(clientID);
    }

    public void addToWhitelist(String clientID) {
        whitelistedIDs.add(clientID);
        saveLists();
    }

    public void removeFromWhitelist(String clientID) {
        whitelistedIDs.remove(clientID);
        saveLists();
    }

    public Set getWhitelist() {
        return Collections.unmodifiableSet(whitelistedIDs);
    }

    public void addToBlacklist(String clientID) {
        blacklistedIDs.add(clientID);
        saveLists();
    }

    public void removeFromBlacklist(String clientID) {
        blacklistedIDs.remove(clientID);
        saveLists();
    }

    public Set getBlacklist() {
        return Collections.unmodifiableSet(blacklistedIDs);
    }

    private boolean isClientBlacklisted(String clientID) {
        return blacklistedIDs != null && blacklistedIDs.contains(clientID);
    }

    private boolean isClientWhitelisted(String clientID) {
        return whitelistedIDs == null || whitelistedIDs.isEmpty() || whitelistedIDs.contains(clientID);
    }
}
