/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.rendering.gui.windows.metricsScreen;

import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.rendering.gui.widgets.UILabel;

import java.util.List;

/**
 * @author Immortius
 */
final class NetworkStatsMode extends MetricsMode {
    private long lastSecond = 0;
    private Time time;
    private NetworkSystem networkSystem;


    public NetworkStatsMode() {
        super("Network", true, false);
        time = CoreRegistry.get(Time.class);
        networkSystem = CoreRegistry.get(NetworkSystem.class);
    }

    public boolean isAvailable() {
        return networkSystem.getMode() != NetworkMode.NONE;
    }

    @Override
    public void updateLines(List<UILabel> lines) {
        long currentTime = time.getGameTimeInMs();
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
