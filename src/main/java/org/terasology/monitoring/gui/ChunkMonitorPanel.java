package org.terasology.monitoring.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class ChunkMonitorPanel extends JPanel {

    protected final ChunkMonitorDisplay display;
    
    public ChunkMonitorPanel() {
        setLayout(new BorderLayout());
        
        display = new ChunkMonitorDisplay(500, 8);
        display.setVisible(true);
        
        add(display, BorderLayout.CENTER);
    }

}
