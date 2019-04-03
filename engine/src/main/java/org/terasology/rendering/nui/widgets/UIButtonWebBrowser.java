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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Button with predefined action - open the URL in default web browser.
 */
public class UIButtonWebBrowser extends UIButton {

    private static final Logger logger = LoggerFactory.getLogger(UIButtonWebBrowser.class);
    private static final String NEW_LINE = System.lineSeparator();

    /**
     * If false, a confirmation popup should appear asking for permission before
     * opening the web browser, otherwise don't.
     */
    private Binding<Boolean> confirmed = new DefaultBinding<>(false);

    /**
     * The URL to be opened in web browser.
     */
    private String url;

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

    private final ActivateEventListener openUrlInDefaultBrowser = button -> {
        if (!hasConfirmation()) {
            logger.debug("Don't have confirmation for opening web browser.");
            showConfirmationPopup();
            return;
        }
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(this.url));
            } catch (IOException | URISyntaxException e) {
                logger.warn("Can't open {} in default browser of your system.", this.url);
                showErrorPopup("Can't open " + this.url + " in default browser of your system.");
            }
        } else {
            String os = System.getProperty("os.name").toLowerCase();
            Runtime runtime = Runtime.getRuntime();
            try {
                if (os.contains("win")) {
                    runtime.exec(createCommand("rundll32", "url.dll,FileProtocolHandler", this.url));
                } else if (os.contains("mac")) {
                    runtime.exec(createCommand("open", this.url));
                } else {
                    runtime.exec(createCommand("xdg-open", this.url));
                }
            } catch (IOException e) {
                logger.warn("Can't recognize your OS and open the url {}.", this.url);
                showErrorPopup("Can't recognize your OS and open the url " + this.url);
            }
        }
    };

    public UIButtonWebBrowser() {
        this.url = "";

        Config config = CoreRegistry.get(Config.class);
        if (config != null) {
            this.webBrowserConfig = config.getWebBrowserConfig();
        }

        this.subscribe(openUrlInDefaultBrowser);
    }

    /**
     * Does confirmation and activates default action.
     */
    private void openBrowser() {
        confirmed.set(true);
        openUrlInDefaultBrowser.onActivated(this);
    }

    private void showConfirmationPopup() {
        if (nuiManager == null || translationSystem == null) {
            logger.error("Can't show confirmation popup!");
            return;
        }

        ConfirmUrlPopup confirmUrlPopup = nuiManager.pushScreen(ConfirmUrlPopup.ASSET_URI, ConfirmUrlPopup.class);
        confirmUrlPopup.setMessage(translationSystem.translate("${engine:menu#button-web-browser-confirmation-title}"),
                translationSystem.translate("${engine:menu#button-web-browser-confirmation-message}")
                        + NEW_LINE + this.url);
        confirmUrlPopup.setLeftButton(translationSystem.translate("${engine:menu#dialog-yes}"), this::openBrowser);
        confirmUrlPopup.setRightButton(translationSystem.translate("${engine:menu#dialog-no}"), () -> {});
        try {
            if (webBrowserConfig != null) {
                confirmUrlPopup.setCheckbox(webBrowserConfig, this.url);
            }
        } catch (MalformedURLException e) {
            logger.error(this.url + " is malformed", e);
        }
    }

    private void showErrorPopup(final String message) {
        if (nuiManager != null) {
            nuiManager.pushScreen(MessagePopup.ASSET_URI, MessagePopup.class).setMessage("", message);
        }
    }

    private boolean hasConfirmation() {
        return confirmed.get() != null && confirmed.get();
    }

    private String[] createCommand(String... arguments) {
        return arguments;
    }

    /**
     * Sets the {@link UIButtonWebBrowser#url} value and sets
     * {@link UIButtonWebBrowser#confirmed} to true if the given URL or Hostname is
     * already trusted, otherwise confirmed is false.
     *
     * @throws MalformedURLException
     */
    public UIButtonWebBrowser setUrl(String url) {
        boolean trustedHostName = false;
        boolean trustedUrl = false;

        if (webBrowserConfig != null) {
            try {
                String hostname = new URL(url).getHost();
                trustedHostName = webBrowserConfig.isHostNameTrusted(hostname);
            } catch (MalformedURLException e) {
                logger.error(url + " is malformed", e);
            }

            if (!trustedHostName) {
                trustedUrl = webBrowserConfig.isUrlTrusted(url);
            }
        }

        if (trustedHostName || trustedUrl) {
            confirmed.set(true);
        } else {
            confirmed.set(false);
        }

        this.url = url;
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
