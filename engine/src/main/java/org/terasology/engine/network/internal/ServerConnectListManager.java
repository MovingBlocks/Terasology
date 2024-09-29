// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.network.internal;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.subsystem.DisplayDevice;

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
 */

public class ServerConnectListManager {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnectListManager.class);
    private static final Gson GSON = new Gson();

    private Context context;
    private Set<String> denylistedIDs;
    private Set<String> allowlistedIDs;
    private final Path denylistPath;
    private final Path allowlistPath;

    public ServerConnectListManager(Context context) {
        // although this seems redundant, compiler wouldn't accept assigning then checking
        if (Files.exists(PathManager.getInstance().getHomePath().resolve("denylist.json"))) {
            denylistPath = PathManager.getInstance().getHomePath().resolve("denylist.json");
        } else {
            denylistPath = PathManager.getInstance().getHomePath().resolve("blacklist.json");
        }
        // although this seems redundant, compiler wouldn't accept assigning then checking
        if (Files.exists(PathManager.getInstance().getHomePath().resolve("allowlist.json"))) {
            allowlistPath = PathManager.getInstance().getHomePath().resolve("allowlist.json");
        } else {
            allowlistPath = PathManager.getInstance().getHomePath().resolve("whitelist.json");
        }
        this.context = context;
        loadLists();
    }

    @SuppressWarnings("unchecked")
    private void loadLists() {
        try {
            if (createFiles()) {
                denylistedIDs = GSON.fromJson(Files.newBufferedReader(denylistPath), Set.class);
                allowlistedIDs = GSON.fromJson(Files.newBufferedReader(allowlistPath), Set.class);
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
                Writer denylistWriter = Files.newBufferedWriter(denylistPath);
                Writer allowlistWriter = Files.newBufferedWriter(allowlistPath);
                denylistWriter.write(GSON.toJson(denylistedIDs));
                allowlistWriter.write(GSON.toJson(allowlistedIDs));
                denylistWriter.close();
                allowlistWriter.close();
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
}
