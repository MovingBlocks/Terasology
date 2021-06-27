// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.settings;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SecurityConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.identity.ClientIdentity;
import org.terasology.engine.identity.PublicIdentityCertificate;
import org.terasology.engine.identity.storageServiceClient.BigIntegerBase64Serializer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.mainMenu.ConfirmPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.FilePickerPopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.MessagePopup;
import org.terasology.engine.rendering.nui.layers.mainMenu.ThreeButtonPopup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Map;

public final class IdentityIOHelper {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(BigInteger.class, BigIntegerBase64Serializer.getInstance())
            .enableComplexMapKeySerialization()
            .disableHtmlEscaping()
            .create();
    private static final Type MAP_TYPE = new TypeToken<Map<PublicIdentityCertificate, ClientIdentity>>() { }.getType();

    private final SecurityConfig securityConfig;
    private final NUIManager nuiManager;
    private final TranslationSystem translationSystem;
    private final String importPopupTitle;
    private final String exportPopupTitle;

    public IdentityIOHelper(Context context) {
        securityConfig = context.get(Config.class).getSecurity();
        nuiManager = context.get(NUIManager.class);
        translationSystem = context.get(TranslationSystem.class);
        importPopupTitle = translationSystem.translate("${engine:menu#identity-import}");
        exportPopupTitle = translationSystem.translate("${engine:menu#identity-export}");
    }

    public void importIdentities() {
        FilePickerPopup filePicker = nuiManager.pushScreen(FilePickerPopup.ASSET_URI, FilePickerPopup.class);
        filePicker.setTitle(importPopupTitle);
        filePicker.setOkHandler(path -> {
            Map<PublicIdentityCertificate, ClientIdentity> newIdentities;
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                newIdentities = GSON.fromJson(reader, MAP_TYPE);
            } catch (IOException | JsonIOException | JsonSyntaxException ex) {
                nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage(translationSystem.translate("${engine:menu#identity-import-failed}"), ex.toString());
                return;
            }
            checkNextConflict(newIdentities.entrySet().iterator(), () -> {
                newIdentities.forEach(securityConfig::addIdentity);
                nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                        .setMessage(importPopupTitle, newIdentities.isEmpty()
                                ? translationSystem.translate("${engine:menu#identity-import-no-new}")
                                : String.format(translationSystem.translate("${engine:menu#identity-import-ok}"), newIdentities.size()));
            });
        });
    }

    private void checkNextConflict(Iterator<Map.Entry<PublicIdentityCertificate, ClientIdentity>> newIdentities, Runnable onCompletion) {
        Runnable next = () -> checkNextConflict(newIdentities, onCompletion);
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
                    popup.setMessage(importPopupTitle, String.format(translationSystem.translate("${engine:menu#identity-import-conflict}"),
                            server.getId(), oldClient.getPlayerPublicCertificate().getId(), newClient.getPlayerPublicCertificate().getId()));

                    popup.setLeftButton(translationSystem.translate("${engine:menu#identity-import-overwrite}"), next);
                    popup.setCenterButton(translationSystem.translate("${engine:menu#identity-import-skip}"), skip);
                    popup.setRightButton(translationSystem.translate("${engine:menu#identity-import-cancel}"),
                            () -> nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                                    .setMessage(importPopupTitle, translationSystem.translate("${engine:menu#identity-import-cancelled}")));
                }
            } else {
                next.run();
            }
        } else {
            onCompletion.run();
        }
    }

    public void exportIdentities() {
        FilePickerPopup filePicker = nuiManager.pushScreen(FilePickerPopup.ASSET_URI, FilePickerPopup.class);
        filePicker.setTitle(exportPopupTitle);
        filePicker.setOkHandler(path -> {
            Runnable action = () -> {
                Map<PublicIdentityCertificate, ClientIdentity> identities = securityConfig.getAllIdentities();
                try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
                    GSON.toJson(identities, MAP_TYPE, writer);
                    nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage(exportPopupTitle,
                            String.format(translationSystem.translate("${engine:menu#identity-export-ok}"), identities.size(),
                                    path.toString()));
                } catch (IOException | JsonIOException ex) {
                    nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class)
                            .setMessage(translationSystem.translate("${engine:menu#identity-export-fail}"), ex.toString());
                }
            };

            if (Files.exists(path)) {
                ConfirmPopup confirm = nuiManager.pushScreen(ConfirmPopup.ASSET_URI, ConfirmPopup.class);
                confirm.setMessage(exportPopupTitle, String.format(translationSystem.translate("${engine:menu#existing-file-warning}"),
                        path.toString()));
                confirm.setOkHandler(action);
            } else {
                action.run();
            }
        });
    }
}
