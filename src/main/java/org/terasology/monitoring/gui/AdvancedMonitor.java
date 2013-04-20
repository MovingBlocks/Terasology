package org.terasology.monitoring.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class AdvancedMonitor extends JFrame {
    
    private final JTabbedPane tabs;
    
    private final ThreadMonitorPanel threadMonitor;
    private final ChunkMonitorPanel chunkMonitor;
    private final PerformanceMonitorPanel perfMonitor;
    
    public AdvancedMonitor() {
        this("Advanced Monitoring Tool", 10, 10, 800, 600);
    }
    
    public AdvancedMonitor(String title, int x, int y, int width, int height) {
        setTitle(title);
        setBounds(x, y, width, height);
        setLayout(new BorderLayout());
        
        tabs = new JTabbedPane();
        
        threadMonitor = new ThreadMonitorPanel();
        threadMonitor.setVisible(true);
        
        chunkMonitor = new ChunkMonitorPanel();
        chunkMonitor.setVisible(true);
        
        perfMonitor = new PerformanceMonitorPanel();
        perfMonitor.setVisible(true);
        
        tabs.add("Threads", threadMonitor);
        tabs.add("Chunks", chunkMonitor);
        tabs.add("Performance", perfMonitor);

        add(tabs, BorderLayout.CENTER);
    }
}
