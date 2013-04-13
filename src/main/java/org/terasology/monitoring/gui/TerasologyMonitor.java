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
        
        tabs.add("Thread Monitor", threadMonitor);
        tabs.add("Chunk Monitor", chunkMonitor);

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
