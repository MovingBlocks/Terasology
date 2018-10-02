/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.rendering.nui.widgets;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.WebBrowserConfig;
import org.terasology.i18n.TranslationSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.layers.mainMenu.ConfirmUrlPopup;
import org.terasology.rendering.nui.layers.mainMenu.MessagePopup;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Button with predefined action - open the link in default web browser.
 */
public class UIButtonWebBrowser extends UIButton {

    private static final Logger logger = LoggerFactory.getLogger(UIButtonWebBrowser.class);

    private Binding<Boolean> confirmed = new DefaultBinding<>(false);

    /**
     * The link to be opened in web browser.
     */
    private String link = "";

    /**
     * Responsible for creating popups.
     */
    private NUIManager nuiManager;

    /**
     * Responsible for translating messages on popups.
     */
    private TranslationSystem translationSystem;

    /**
     * Responsible for holding all the trusted URLs.
     */
    private WebBrowserConfig webBrowserConfig;

    public UIButtonWebBrowser() {
        Config config = CoreRegistry.get(Config.class);
        this.webBrowserConfig = config.getWebBrowserConfig();
        this.subscribe(openInDefaultBrowser);
    }

    /**
     * Does confirmation and activates default action.
     */
    private void confirm() {
        confirmed.set(true);
        openInDefaultBrowser.onActivated(this);
    }

    private final ActivateEventListener openInDefaultBrowser = button -> {
        if (!hasConfirmation()) {
            logger.debug("Don't have confirmation for opening web browser.");
            showConfirmationPopup();
            return;
        }
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(this.link));
            } catch (IOException | URISyntaxException e) {
                logger.warn("Can't open {} in default browser of your system.", this.link);
                showErrorPopup("Can't open " + this.link + " in default browser of your system.");
            }
        } else {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();
            try {
                if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + this.link);
                } else if (os.contains("mac")) {
                    runtime.exec("open " + this.link);
                } else {
                    runtime.exec("xdg-open " + this.link);
                }
            } catch (IOException e) {
                logger.warn("Can't recognize your OS and open the link {}.", this.link);
                showErrorPopup("Can't recognize your OS and open the link " + this.link);
            }
        }
    };

    private void showConfirmationPopup() {
        if (nuiManager == null || translationSystem == null) {
            logger.error("Can't show confirmation popup!");
            return;
        }

        ConfirmUrlPopup confirmUrlPopup = nuiManager.pushScreen(ConfirmUrlPopup.ASSET_URI, ConfirmUrlPopup.class);
        confirmUrlPopup.setMessage(translationSystem.translate("${engine:menu#button-web-browser-confirmation-title}"), translationSystem.translate("${engine:menu#button-web-browser-confirmation-message}") + "\n" + getLink());
        confirmUrlPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), this::confirm);
        confirmUrlPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> {
        });
        confirmUrlPopup.setCheckbox(webBrowserConfig, getLink());
    }

    private void showErrorPopup(final String message) {
        if (nuiManager != null) {
            nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("", message);
        }
    }

    private boolean hasConfirmation() {
        return confirmed.get() != null && confirmed.get();
    }

    public String getLink() {
        return link;
    }

    public UIButtonWebBrowser setLink(String link) {
        boolean safeURL = webBrowserConfig.isUrlTrusted(link);

        confirmed.set(safeURL);
        this.link = link;
        return this;
    }

    public UIButtonWebBrowser setNuiManager(final NUIManager nuiManager) {
        this.nuiManager = nuiManager;
        return this;
    }

    public UIButtonWebBrowser setTranslationSystem(final TranslationSystem translationSystem) {
        this.translationSystem = translationSystem;
        return this;
    }
}
