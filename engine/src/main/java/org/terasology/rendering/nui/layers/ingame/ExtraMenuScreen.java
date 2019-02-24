/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.nui.layers.ingame;

import org.terasology.assets.ResourceUrn;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.Time;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.telemetry.TelemetryScreen;

/**
 * Handles the "Extras" button from the game's pause menu screen.
 */
public class ExtraMenuScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:extraMenuScreen");

    @In
    private Time time;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        WidgetUtil.trySubscribe(this, "telemetry", button -> triggerForwardAnimation(TelemetryScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "devTools", widget -> triggerForwardAnimation(DevToolsMenuScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "crashReporter", widget -> CrashReporter.report(new Throwable("There is no error."),
                LoggingContext.getLoggingPath(), CrashReporter.MODE.ISSUE_REPORTER));
        WidgetUtil.trySubscribe(this, "close", widget -> triggerBackAnimation());
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
