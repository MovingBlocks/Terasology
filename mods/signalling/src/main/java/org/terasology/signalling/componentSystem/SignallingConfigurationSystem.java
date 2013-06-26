package org.terasology.signalling.componentSystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.Vector3i;
import org.terasology.signalling.components.SignalProducerComponent;
import org.terasology.signalling.components.SignalTimeDelayComponent;
import org.terasology.signalling.gui.UIDelayConfiguration;
import org.terasology.world.block.BlockComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(whenHeadless = false)
public class SignallingConfigurationSystem implements ComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SignallingConfigurationSystem.class);

    @Override
    public void initialise() {
        CoreRegistry.get(GUIManager.class).registerWindow("signalling:delayConfiguration", UIDelayConfiguration.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {BlockComponent.class, SignalTimeDelayComponent.class})
    public void openDelayConfiguration(ActivateEvent event, EntityRef entity) {
        UIDelayConfiguration delayConfigurationScreen = (UIDelayConfiguration) CoreRegistry.get(GUIManager.class).openWindow("signalling:delayConfiguration");
        delayConfigurationScreen.attachToEntity("Delay configuration", entity);
    }
}
