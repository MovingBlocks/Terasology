// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.monitoring.gui;

import javax.swing.JTabbedPane;
import javax.swing.JFrame;
import java.awt.BorderLayout;

@SuppressWarnings("serial")
public class AdvancedMonitor extends JFrame {

    private final ThreadMonitorPanel threadMonitor;
    private final ChunkMonitorPanel chunkMonitor;
    private final PerformanceMonitorPanel performanceMonitor;

    public AdvancedMonitor() {
        this("Advanced Monitoring Tool", 10, 10, 800, 600);
    }

    public AdvancedMonitor(String title, int x, int y, int width, int height) {
        setTitle(title);
        setBounds(x, y, width, height);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();

        threadMonitor = new ThreadMonitorPanel();
        threadMonitor.setVisible(true);

        chunkMonitor = new ChunkMonitorPanel();
        chunkMonitor.setVisible(true);

        performanceMonitor = new PerformanceMonitorPanel();
        performanceMonitor.setVisible(true);

        tabs.add("Threads", threadMonitor);
        tabs.add("Chunks", chunkMonitor);
        tabs.add("Performance", performanceMonitor);

        add(tabs, BorderLayout.CENTER);
    }

    /**
     * Closes advanced monitor panel.
     *
     * This method goes through three steps:
     *  1. Hides this windows.
     *  2. Releases all of the native screen resources used by this windows.
     *  3. Stops monitoring threads gracefully.
     */
    public void close() {
        setVisible(false);
        dispose();

        threadMonitor.stopThread();
        chunkMonitor.stopThread();
        performanceMonitor.stopThread();
    }
}
