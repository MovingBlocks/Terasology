/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.mainMenu.loadingScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.widgets.UILabel;

/**
 * @author Immortius
 */
public class LoadingScreen extends UIScreenLayer {

    private static final Logger logger = LoggerFactory.getLogger(LoadingScreen.class);

    private UILabel messageLabel;
    private UILoadBar fillBar;

    @Override
    public void initialise() {
        messageLabel = find("statusLabel", UILabel.class);
        fillBar = find("progressBar", UILoadBar.class);
    }

    public void updateStatus(String message, float v) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
        if (fillBar != null) {
            fillBar.setValue(v);
        }
    }

    @Override
    public boolean isEscapeToCloseAllowed() {
        return false;
    }
}
