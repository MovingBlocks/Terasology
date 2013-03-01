package org.terasology.rendering.gui.windows.metricsScreen;

import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.NetworkSystemImpl;
import org.terasology.rendering.gui.widgets.UILabel;

import java.util.List;

/**
 * @author Immortius
 */
final class NetworkStatsMode extends MetricsMode {
    private long lastSecond = 0;
    private Timer timer;
    private NetworkSystem networkSystem;


    public NetworkStatsMode() {
        super("Network", true, false);
        timer = CoreRegistry.get(Timer.class);
        networkSystem = CoreRegistry.get(NetworkSystem.class);
    }

    public boolean isAvailable() {
        return networkSystem.getMode() != NetworkMode.NONE;
    }

    @Override
    public void updateLines(List<UILabel> lines) {
        long currentTime = timer.getTimeInMs();
        long currentSecond = currentTime / 1000;
        if (currentSecond - lastSecond > 1) {
            networkSystem.getIncomingBytesDelta();
            networkSystem.getIncomingMessagesDelta();
            lines.get(0).setText("In Msg: 0");
            lines.get(1).setText("In Bytes: 0");
            lines.get(2).setText("Out Msg: 0");
            lines.get(3).setText("Out Bytes: 0");
        } else if (currentSecond - lastSecond == 1) {
            lines.get(0).setText(String.format("In Msg: %d", networkSystem.getIncomingMessagesDelta()));
            lines.get(1).setText(String.format("In Bytes: %d", networkSystem.getIncomingBytesDelta()));
            lines.get(2).setText(String.format("Out Msg: %d", networkSystem.getOutgoingMessagesDelta()));
            lines.get(3).setText(String.format("Out Bytes: %d", networkSystem.getOutgoingBytesDelta()));
        } else {
            // No change
        }
        lastSecond = currentSecond;
        int line = 0;
        for (; line < 4; line++) {
            lines.get(line).setVisible(true);
        }
        for (; line < lines.size(); line++) {
            lines.get(line).setVisible(false);
        }
    }
}
