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
    private Set<String> deniedIDs;
    private Set<String> allowedIDs;
    private final Path denylistPath;
    private final Path allowlistPath;

    public ServerConnectListManager(Context context) {
        denylistPath = PathManager.getInstance().getHomePath().resolve("denylist.json");
        allowlistPath = PathManager.getInstance().getHomePath().resolve("allowlist.json");
        this.context = context;
        loadLists();
    }

    @SuppressWarnings("unchecked")
    private void loadLists() {
        try {
            if (createFiles()) {
                deniedIDs = GSON.fromJson(Files.newBufferedReader(denylistPath), Set.class);
                allowedIDs = GSON.fromJson(Files.newBufferedReader(allowlistPath), Set.class);
                if (deniedIDs == null) {
                    deniedIDs = new HashSet<>();
                }
                if (allowedIDs == null) {
                    allowedIDs = new HashSet<>();
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
                denylistWriter.write(GSON.toJson(deniedIDs));
                allowlistWriter.write(GSON.toJson(allowedIDs));
                denylistWriter.close();
                allowlistWriter.close();
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
        if (isClientDenied(clientID)) {
            return "client on denylist";
        }
        if (!isClientAllowed(clientID)) {
            return "client not on allowlist";
        }
        return null;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isClientAllowedToConnect(String clientID) {
        return !isClientDenied(clientID) && isClientAllowed(clientID);
    }

    public void addToAllowlist(String clientID) {
        allowedIDs.add(clientID);
        saveLists();
    }

    public void removeFromAllowlist(String clientID) {
        allowedIDs.remove(clientID);
        saveLists();
    }

    public Set getAllowlist() {
        return Collections.unmodifiableSet(allowedIDs);
    }

    public void addToDenylist(String clientID) {
        deniedIDs.add(clientID);
        saveLists();
    }

    public void removeFromDenylist(String clientID) {
        deniedIDs.remove(clientID);
        saveLists();
    }

    public Set getDenylist() {
        return Collections.unmodifiableSet(deniedIDs);
    }

    private boolean isClientDenied(String clientID) {
        return deniedIDs != null && deniedIDs.contains(clientID);
    }

    private boolean isClientAllowed(String clientID) {
        return allowedIDs == null || allowedIDs.isEmpty() || allowedIDs.contains(clientID);
    }
}
