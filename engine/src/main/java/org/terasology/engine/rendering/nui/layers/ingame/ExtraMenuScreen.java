// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.ingame;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.core.Time;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.nui.WidgetUtil;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.telemetry.TelemetryScreen;

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
