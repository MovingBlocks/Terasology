
package org.terasology.engine.config.facade;

import org.terasology.engine.config.Config;
import org.terasology.engine.config.ControllerConfig.ControllerInfo;

public class InputDeviceConfigurationImpl implements InputDeviceConfiguration {
    private Config config;

    public InputDeviceConfigurationImpl(Config config) {
        this.config = config;
    }

    @Override
    public float getMouseSensitivity() {
        return config.getInput().getMouseSensitivity();
    }

    @Override
    public void setMouseSensitivity(float mouseSensitivity) {
        config.getInput().setMouseSensitivity(mouseSensitivity);
    }

    @Override
    public void reset() {
        config.getInput().reset();
    }

    @Override
    public boolean isMouseYAxisInverted() {
        return config.getInput().isMouseYAxisInverted();
    }

    @Override
    public void setMouseYAxisInverted(boolean mouseYAxisInverted) {
        config.getInput().setMouseYAxisInverted(mouseYAxisInverted);
    }

    @Override
    public ControllerInfo getController(String name) {
        return config.getInput().getControllers().getController(name);
    }

}
