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
import org.terasology.i18n.TranslationSystem;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

/**
 * This class provides the methods needed to determine if a client is allowed to connect or not,
 * based on the blacklist and whitelist files.
 */

public class ServerConnectListManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectListManager.class);
    private static final Gson gson = new Gson();

    private Context context;
    private Set blacklistedIDs;
    private Set whitelistedIDs;
    private final Path blacklistPath;
    private final Path whitelistPath;

    public ServerConnectListManager(Context context) {
        blacklistPath = PathManager.getInstance().getHomePath().resolve("blacklist.json");
        whitelistPath = PathManager.getInstance().getHomePath().resolve("whitelist.json");
        this.context = context;
        loadLists();
    }

    private void loadLists() {
        try {
            createFiles();
            blacklistedIDs = gson.fromJson(Files.newBufferedReader(blacklistPath), Set.class);
            whitelistedIDs = gson.fromJson(Files.newBufferedReader(whitelistPath), Set.class);
        } catch (IOException e) {
            logger.error("Whitelist or blacklist files not found:", e);
        }
    }

    private void saveLists() {
        try {
            createFiles();
            Writer blacklistWriter = Files.newBufferedWriter(blacklistPath);
            Writer whitelistWriter = Files.newBufferedWriter(whitelistPath);
            blacklistWriter.write(gson.toJson(blacklistedIDs));
            whitelistWriter.write(gson.toJson(whitelistedIDs));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFiles() throws IOException {
        DisplayDevice display = context.get(DisplayDevice.class);
        if (!display.isHeadless()) {
            return;
        }
        if (!Files.exists(blacklistPath)) {
            Files.createFile(blacklistPath);
        }
        if (!Files.exists(whitelistPath)) {
            Files.createFile(whitelistPath);
        }
    }

    public String getErrorMessage(String clientID) {
        TranslationSystem translationSystem = context.get(TranslationSystem.class);
        if (isClientBlacklisted(clientID)) {
            return translationSystem.translate("${engine:menu#client-on-blacklist}");
        }
        if (!isClientWhitelisted(clientID)) {
            return translationSystem.translate("${engine:menu#client-not-on-whitelist}");
        }
        return null;
    }

    public boolean isClientAllowedToConnect(String clientID) {
        return !isClientBlacklisted(clientID) && isClientWhitelisted(clientID);
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
        return whitelistedIDs == null || whitelistedIDs.contains(clientID);
    }
}
