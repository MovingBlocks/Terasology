
package org.terasology.config;

import org.terasology.config.ControllerConfig.ControllerInfo;
import org.terasology.context.Context;

/**
 * Facade for {@link Config#getInput()}
 */
public interface InputDeviceConfig {
    float getMouseSensitivity();

    void setMouseSensitivity(float mouseSensitivity);

    void reset(Context context);

    boolean isMouseYAxisInverted();

    void setMouseYAxisInverted(boolean mouseYAxisInverted);

    ControllerInfo getController(String name);
}
