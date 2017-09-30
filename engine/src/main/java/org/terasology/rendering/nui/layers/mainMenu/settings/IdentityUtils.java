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
import org.terasology.config.SecurityConfig;
import org.terasology.identity.ClientIdentity;
import org.terasology.identity.PublicIdentityCertificate;
import org.terasology.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.mainMenu.ConfirmPopup;
import org.terasology.rendering.nui.layers.mainMenu.FilePickerPopup;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
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

        });
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
                } catch (IOException ex) {
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
