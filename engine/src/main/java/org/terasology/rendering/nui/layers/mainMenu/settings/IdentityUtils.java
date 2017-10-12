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
package org.terasology.rendering.nui.layers.mainMenu.settings;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.terasology.config.SecurityConfig;
import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.mainMenu.ConfirmPopup;
import org.terasology.rendering.nui.layers.mainMenu.FilePickerPopup;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.rendering.nui.layers.mainMenu.ThreeButtonPopup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;

public final class IdentityUtils {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .create();
    private static final Type MAP_TYPE = new TypeToken<Map<PublicIdentityCertificate, ClientIdentity>>() { }.getType();

    private IdentityUtils() {
    }

    public static void importIdentities(SecurityConfig securityConfig, NUIManager nuiManager) {
        FilePickerPopup filePicker = nuiManager.pushScreen(FilePickerPopup.ASSET_URI, FilePickerPopup.class);
        filePicker.setTitle("Import multiplayer identities");
        filePicker.setOkHandler(path -> {
            Map<PublicIdentityCertificate, ClientIdentity> newIdentities;
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                newIdentities = GSON.fromJson(reader, MAP_TYPE);
            } catch (IOException | JsonIOException | JsonSyntaxException ex) {
                nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage("Failed to import identities", ex.toString());
                return;
            }
            checkNextConflict(securityConfig, nuiManager, newIdentities.entrySet().iterator(), () -> {
                newIdentities.forEach(securityConfig::addIdentity);
                nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("Import multiplayer identities",
                        newIdentities.isEmpty() ? "The selected file does not contain any new identity." : "Successfully imported " + newIdentities.size() + " identities.");
            });
        });
    }

    private static void checkNextConflict(SecurityConfig securityConfig, NUIManager nuiManager,
                                          Iterator<Map.Entry<PublicIdentityCertificate, ClientIdentity>> newIdentities, Runnable onCompletion) {
        Runnable next = () -> checkNextConflict(securityConfig, nuiManager, newIdentities, onCompletion);
        if (newIdentities.hasNext()) {
            Map.Entry<PublicIdentityCertificate, ClientIdentity> entry = newIdentities.next();
            PublicIdentityCertificate server = entry.getKey();
            ClientIdentity newClient = entry.getValue();
            ClientIdentity oldClient = securityConfig.getIdentity(server);
            if (oldClient != null) {
                Runnable skip = () -> {
                    newIdentities.remove();
                    next.run();
                };

                if (newClient.getPlayerPublicCertificate().equals(oldClient.getPlayerPublicCertificate())) {
                    skip.run();
                } else {
                    ThreeButtonPopup popup = nuiManager.pushScreen(ThreeButtonPopup.ASSET_URI, ThreeButtonPopup.class);
                    popup.setMessage("Conflict importing multiplayer identities", "For the server with ID " + server.getId()
                            + ", a local identity with client ID " + oldClient.getPlayerPublicCertificate().getId() + " exists, "
                            + "but the file which is being imported contains an identity for the same server with client ID " + newClient.getPlayerPublicCertificate().getId()
                            + ". Please choose an option.");

                    popup.setLeftButton("Import (overwrite local)", next);
                    popup.setCenterButton("Skip (keep local)", skip);
                    popup.setRightButton("Cancel import", () -> nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                            .setMessage("Import multiplayer identities", "Operation has been cancelled, no identities have been imported."));
                }
            } else {
                next.run();
            }
        } else {
            onCompletion.run();
        }
    }

    public static void exportIdentities(SecurityConfig securityConfig, NUIManager nuiManager) {
        String popupTitle = "Export multiplayer identities";
        FilePickerPopup filePicker = nuiManager.pushScreen(FilePickerPopup.ASSET_URI, FilePickerPopup.class);
        filePicker.setTitle(popupTitle);
        filePicker.setOkHandler(path -> {
            Runnable action = () -> {
                try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
                    GSON.toJson(securityConfig.getAllIdentities(), MAP_TYPE, writer);
                    nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                            .setMessage(popupTitle, "Exported identities to " + path.toString());
                } catch (IOException | JsonIOException ex) {
                    nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                            .setMessage("Failed to export identities", ex.toString());
                }
            };

            if (Files.exists(path)) {
                ConfirmPopup confirm = nuiManager.pushScreen(ConfirmPopup.ASSET_URI, ConfirmPopup.class);
                confirm.setMessage(popupTitle, "File " + path.toString() + " already exists. Do you want to overwrite it?");
                confirm.setOkHandler(action);
            } else {
                action.run();
            }
        });
    }
}
