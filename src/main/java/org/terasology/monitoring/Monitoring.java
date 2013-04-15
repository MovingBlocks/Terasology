package org.terasology.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.AdvancedConfig;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.monitoring.gui.AdvancedMonitor;

public class Monitoring {

    private static final Logger logger = LoggerFactory.getLogger(Monitoring.class);
    
    private static boolean initialized = false;
    private static boolean advancedMonitoringEnabled = false, advancedMonitorVisibleAtStartup = false;
    
    private static AdvancedMonitor instance;
    
    private static void init() {
        if (initialized) return;
        initialized = true;
        final Config config = CoreRegistry.get(Config.class);
        if (config == null) {
            logger.error("Cannot obtain global configuration object, advanced monitoring will be disabled");
            initialized = true;
            return;
        }
        final AdvancedConfig aconfig = config.getAdvanced();
        advancedMonitoringEnabled = aconfig.isAdvancedMonitoringEnabled();
        advancedMonitorVisibleAtStartup = aconfig.isAdvancedMonitorVisibleAtStartup();
    }
    
    private Monitoring() {}

    public static boolean isAdvancedMonitoringEnabled() {
        init();
        return advancedMonitoringEnabled;
    }
    
    public static boolean isAdvancedMonitorVisibleAtStartup() {
        init();
        return advancedMonitorVisibleAtStartup;
    }
    
    public static boolean isAdvancedMonitorShowing() {
        return instance != null && instance.isShowing();
    }

    public static AdvancedMonitor createAdvancedMonitor() {
        if (instance == null && isAdvancedMonitoringEnabled())
            instance = new AdvancedMonitor();
        return instance;
    }
    
    public static AdvancedMonitor createAndShowAdvancedMonitor() {
        if (instance == null && isAdvancedMonitoringEnabled())
            instance = new AdvancedMonitor();
        if (instance != null) 
            instance.setVisible(true);
        return instance;
    }
    
    public static AdvancedMonitor createAndShowAtStartup() {
        if (instance == null && isAdvancedMonitoringEnabled() && isAdvancedMonitorVisibleAtStartup()) {
            instance = new AdvancedMonitor();
            instance.setVisible(true);
        }
        return instance;
    }
    
    public static AdvancedMonitor getAdvancedMonitor() {
        return instance;
    }
}
