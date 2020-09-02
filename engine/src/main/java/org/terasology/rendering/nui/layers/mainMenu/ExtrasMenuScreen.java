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
package org.terasology.rendering.nui.layers.mainMenu;

import org.terasology.assets.ResourceUrn;
import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.telemetry.TelemetryScreen;

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
