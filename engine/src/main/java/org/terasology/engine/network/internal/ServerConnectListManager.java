// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.subsystem.DisplayDevice;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides the methods needed to determine if a client is allowed to connect or not,
 * based on the denylist and allowlist files.
 *
 * To account for legacy blacklist and whitelist files on old servers,
 * migrateLegacyFiles() will migrate these files to denylist and allowlist respectively.
 */

public class ServerConnectListManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectListManager.class);
    private static final Gson GSON = new Gson();

    private Context context;
    private Set<String> denylistedIDs;
    private Set<String> allowlistedIDs;
    private final Path denylistPath;
    private final Path allowlistPath;

    @Inject
    public ServerConnectListManager(Context context) {
        denylistPath = PathManager.getInstance().getHomePath().resolve("denylist.json");
        allowlistPath = PathManager.getInstance().getHomePath().resolve("allowlist.json");
        this.context = context;
        loadLists();
    }

    @SuppressWarnings("unchecked")
    private void loadLists() {
        try {
            migrateLegacyFiles();
            if (createFiles()) {
                try (var denylistReader = Files.newBufferedReader(denylistPath);
                     var allowlistReader = Files.newBufferedReader(allowlistPath)) {
                    denylistedIDs = GSON.fromJson(denylistReader, Set.class);
                    allowlistedIDs = GSON.fromJson(allowlistReader, Set.class);
                }
                if (denylistedIDs == null) {
                    denylistedIDs = new HashSet<>();
                }
                if (allowlistedIDs == null) {
                    allowlistedIDs = new HashSet<>();
                }
            }
        } catch (IOException e) {
            logger.error("Allowlist or denylist files not found:", e);
        }
    }

    private void saveLists() {
        try {
            if (createFiles()) {
                try (Writer denylistWriter = Files.newBufferedWriter(denylistPath);
                     Writer allowlistWriter = Files.newBufferedWriter(allowlistPath)) {
                    denylistWriter.write(GSON.toJson(denylistedIDs));
                    allowlistWriter.write(GSON.toJson(allowlistedIDs));
                }
            }
        } catch (IOException e) {
            logger.error("Couldn't save lists: ", e);
        }
    }

    private boolean createFiles() throws IOException {
        DisplayDevice display = context.get(DisplayDevice.class);
        if (display == null || !display.isHeadless()) {
            return false;
        }
        if (!Files.exists(denylistPath)) {
            Files.createFile(denylistPath);
        }
        if (!Files.exists(allowlistPath)) {
            Files.createFile(allowlistPath);
        }
        return true;
    }

    public String getErrorMessage(String clientID) {
        if (isClientDenylisted(clientID)) {
            return "client on denylist";
        }
        if (!isClientAllowlisted(clientID)) {
            return "client not on allowlist";
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isClientAllowedToConnect(String clientID) {
        return !isClientDenylisted(clientID) && isClientAllowlisted(clientID);
    }

    public void addToAllowlist(String clientID) {
        allowlistedIDs.add(clientID);
        saveLists();
    }

    public void removeFromAllowlist(String clientID) {
        allowlistedIDs.remove(clientID);
        saveLists();
    }

    public Set getAllowlist() {
        return Collections.unmodifiableSet(allowlistedIDs);
    }

    public void addToDenylist(String clientID) {
        denylistedIDs.add(clientID);
        saveLists();
    }

    public void removeFromDenylist(String clientID) {
        denylistedIDs.remove(clientID);
        saveLists();
    }

    public Set getDenylist() {
        return Collections.unmodifiableSet(denylistedIDs);
    }

    private boolean isClientDenylisted(String clientID) {
        return denylistedIDs != null && denylistedIDs.contains(clientID);
    }

    private boolean isClientAllowlisted(String clientID) {
        return allowlistedIDs == null || allowlistedIDs.isEmpty() || allowlistedIDs.contains(clientID);
    }

    private void migrateLegacyFiles() {
        Path homePath = PathManager.getInstance().getHomePath();

        try {
            Path legacyDenylist = homePath.resolve("blacklist.json");
            if (Files.exists(legacyDenylist) && !Files.exists(denylistPath)) {
                Files.move(legacyDenylist, denylistPath);
                logger.info("Migrated blacklist.json -> denylist.json");
            }
        } catch (IOException e) {
            logger.error("Failed to migrate blacklist.json to denylist.json: ", e);
        }

        try {
            Path legacyAllowlist = homePath.resolve("whitelist.json");
            if (Files.exists(legacyAllowlist) && !Files.exists(allowlistPath)) {
                Files.move(legacyAllowlist, allowlistPath);
                logger.info("Migrated whitelist.json -> allowlist.json");
            }
        } catch (IOException e) {
            logger.error("Failed to migrate whitelist.json to allowlist.json: ", e);
        }
    }
}
