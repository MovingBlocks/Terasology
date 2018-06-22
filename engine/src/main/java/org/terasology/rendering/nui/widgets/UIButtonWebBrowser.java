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
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Button with predefined action - open the link in default web browser.
 */
public class UIButtonWebBrowser extends UIButton {

    private static final Logger logger = LoggerFactory.getLogger(UIButtonWebBrowser.class);

    private String link = "";

    private Binding<Boolean> confirmed = new DefaultBinding<>();

    private Runnable confirmationProcess;

    public UIButtonWebBrowser() {
        this.subscribe(openInDefaultBrowser);
    }

    /**
     *  Does confirmation and activates default action.
     */
    public void doConfirmation() {
        confirmed.set(true);
        openInDefaultBrowser.onActivated(this);
    }

    private final ActivateEventListener openInDefaultBrowser = button -> {
        if (!hasConfirmation()) {
            logger.debug("You don't have confirmation for opening web browser");
            if (confirmationProcess != null) {
                confirmationProcess.run();
            }
            return;
        }
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(this.link));
            } catch (IOException | URISyntaxException e) {
                logger.warn("Can't open {} in default browser of your system.", this.link);
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
                logger.warn("Can't recognize your system and open the link {}.", this.link);
            }
        }
    };

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public boolean hasConfirmation() {
        return confirmed.get() != null && confirmed.get();
    }

    public void setConfirmationHandler(Runnable runnable) {
        confirmationProcess = runnable;
    }
}
