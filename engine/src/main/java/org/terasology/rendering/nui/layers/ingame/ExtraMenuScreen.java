package org.terasology.rendering.nui.layers.ingame;

import org.terasology.crashreporter.CrashReporter;
import org.terasology.engine.GameEngine;
import org.terasology.engine.LoggingContext;
import org.terasology.engine.Time;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.telemetry.TelemetryScreen;

/**
 */
public class ExtraMenuScreen extends CoreScreenLayer {

    @In
    private Time time;

    @In
    private NetworkSystem networkSystem;

    @Override
    public void initialise() {
        WidgetUtil.trySubscribe(this, "close", widget -> getManager().closeScreen(ExtraMenuScreen.this));
        WidgetUtil.trySubscribe(this, "crashReporter", widget -> CrashReporter.report(new Throwable("There is no error."), LoggingContext.getLoggingPath(), CrashReporter.MODE.ISSUE_REPORTER));
        WidgetUtil.trySubscribe(this, "devTools", widget -> getManager().pushScreen("devToolsMenuScreen"));
        WidgetUtil.trySubscribe(this, "telemetry", button -> triggerForwardAnimation(TelemetryScreen.ASSET_URI));
    }

    @Override
    public void onScreenOpened() {
        getManager().removeOverlay("engine:onlinePlayersOverlay");
    }

    @Override
    public void onClosed() {
        if (networkSystem.getMode() == NetworkMode.NONE) {
            time.setPaused(false);
        }
    }
}