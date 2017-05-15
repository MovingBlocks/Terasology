

package org.terasology.engine.subsystem.config;

import org.terasology.config.BindsConfig;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.InputSystem;

public interface BindsManager {

    BindsConfig getBindsConfig();
    
    BindsConfig createDefault(Context context);
    
    void updateForChangedMods(Context context);
    
    void applyBinds(InputSystem inputSystem, ModuleManager moduleManager);
    
    void saveBindsConfig();
}
