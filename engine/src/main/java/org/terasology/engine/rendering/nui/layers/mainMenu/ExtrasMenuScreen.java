// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.LoggingContext;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.nui.WidgetUtil;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.telemetry.TelemetryScreen;

public class ExtrasMenuScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:extrasMenuScreen");

    @In
    private GameEngine engine;

    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());

        RecordScreen recordScreen = getManager().createScreen(RecordScreen.ASSET_URI, RecordScreen.class);
        ReplayScreen replayScreen = getManager().createScreen(ReplayScreen.ASSET_URI, ReplayScreen.class);

        WidgetUtil.trySubscribe(this, "record", button -> {
            recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.PREPARING_RECORD);
            RecordAndReplayUtils recordAndReplayUtils = engine.createChildContext().get(RecordAndReplayUtils.class);
            recordScreen.setRecordAndReplayUtils(recordAndReplayUtils);
            triggerForwardAnimation(recordScreen);
        });
        WidgetUtil.trySubscribe(this, "replay", button -> {
            RecordAndReplayUtils recordAndReplayUtils = engine.createChildContext().get(RecordAndReplayUtils.class);
            replayScreen.setRecordAndReplayUtils(recordAndReplayUtils);
            recordAndReplayCurrentStatus.setStatus(RecordAndReplayStatus.PREPARING_REPLAY);
            triggerForwardAnimation(replayScreen);
        });
        WidgetUtil.trySubscribe(this, "credits", button -> triggerForwardAnimation(CreditsScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "telemetry", button -> triggerForwardAnimation(TelemetryScreen.ASSET_URI));
        WidgetUtil.trySubscribe(this, "crashReporter", widget -> CrashReporter.report(new Throwable("There is no error."),
                LoggingContext.getLoggingPath(), CrashReporter.MODE.ISSUE_REPORTER));
        WidgetUtil.trySubscribe(this, "close", button -> triggerBackAnimation());
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }
}
