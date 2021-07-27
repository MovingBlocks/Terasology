// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.config.Config;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.nui.WidgetUtil;
import org.terasology.nui.widgets.UIScrollingText;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CreditsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:creditsScreen");

    @In
    private Config config;

    @In
    private TranslationSystem translationSystem;

    private UIScrollingText creditsScroll;

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());

        creditsScroll = find("creditsScroll", UIScrollingText.class);
        if (creditsScroll != null) {
            StringBuilder credits = new StringBuilder();

            ClassLoader classloader = getClass().getClassLoader();
            InputStream is = classloader.getResourceAsStream("Credits.md");
            if (is == null) {
                credits.append(translationSystem.translate("${engine:menu#error-credits-not-found}"));
            } else {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.replaceAll("\\[([^]]*)]\\(([^)]+)\\)", "$1").trim();
                        if (line.startsWith("* ")) {
                            if (line.endsWith(":")) {
                                credits.append(System.lineSeparator());
                                credits.append(line, 2, line.length() - 1);
                                credits.append(System.lineSeparator());
                                credits.append(System.lineSeparator());
                            } else {
                                credits.append(line, 2, line.length());
                                credits.append(System.lineSeparator());
                            }
                        } else {
                            credits.append(line);
                            credits.append(System.lineSeparator());
                        }
                    }
                } catch (IOException e) {
                    Logger logger = LoggerFactory.getLogger(CreditsScreen.class);
                    logger.info("Could not open Credits file");

                    credits = new StringBuilder(translationSystem.translate("${engine:menu#error-credits-open"));
                }
            }

            creditsScroll.setText(credits.toString());
            creditsScroll.setAutoReset(false);
            creditsScroll.setScrollingSpeed(1);
            creditsScroll.startScrolling();
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();
        if (creditsScroll != null) {
            creditsScroll.resetScrolling();
        }
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
