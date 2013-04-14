package org.terasology.monitoring.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

@SuppressWarnings("serial")
public class TerasologyMonitor extends JFrame {
    
    private static TerasologyMonitor instance;
    
    private final JTabbedPane tabs;
    
    private final ThreadMonitorPanel threadMonitor;
    private final ChunkMonitorPanel chunkMonitor;
    private final PerformanceMonitorPanel perfMonitor;
    
    public TerasologyMonitor() {
        this("Terasology Monitor", 10, 10, 800, 600);
    }
    
    public TerasologyMonitor(String title, int x, int y, int width, int height) {
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
    
    public static boolean isMonitorVisible() {
        return instance != null && instance.isShowing();
    }

    public static void setMonitorVisible(boolean value) {
        if (value != isMonitorVisible()) {
            if (instance == null) instance = new TerasologyMonitor();
            instance.setVisible(value);
        }
    }
    
    public static TerasologyMonitor getInstance() {
        return instance;
    }
}
