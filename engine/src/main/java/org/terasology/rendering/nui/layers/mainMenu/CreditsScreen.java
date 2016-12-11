/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.mainMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.widgets.UIScrollingText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CreditsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:creditsScreen");

    @In
    private Config config;

    private UIScrollingText creditsScroll;

    @Override
    @SuppressWarnings("unchecked")
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());

        creditsScroll = find("creditsScroll", UIScrollingText.class);
        if (creditsScroll != null) {
            ClassLoader classloader = getClass().getClassLoader();
            InputStream is = classloader.getResourceAsStream("Credits.txt");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                StringBuilder credits = new StringBuilder();
                String line = br.readLine();
                while (line != null) {
                    credits.append(line);
                    credits.append(System.lineSeparator());
                    line = br.readLine();
                }
                creditsScroll.setText(credits.toString());
                creditsScroll.setAutoReset(false);
                creditsScroll.setScrollingSpeed(1);
                creditsScroll.startScrolling();
            } catch (IOException e) {
                Logger logger = LoggerFactory.getLogger(CreditsScreen.class);
                logger.info("Could not open Credits file");
            }
        }
    }

    @Override
    public void onOpened() {
        super.onOpened();
        if (creditsScroll != null) {
            creditsScroll.resetScrolling();
        }
    }
}
