// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class ChunkMonitorPanel extends JPanel {

    protected final ChunkMonitorDisplay display;

    public ChunkMonitorPanel() {
        setLayout(new BorderLayout());

        display = new ChunkMonitorDisplay(500, 8);
        display.setVisible(true);

        add(display, BorderLayout.CENTER);
    }

    public void stopThread() {
        display.stopThread();
    }
}
