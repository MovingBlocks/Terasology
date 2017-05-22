
package org.terasology.config.facade;

import org.terasology.config.Config;
import org.terasology.config.ControllerConfig.ControllerInfo;
import org.terasology.context.Context;

/**
 * Facade for {@link Config#getInput()}
 */
public interface InputDeviceConfiguration {
    float getMouseSensitivity();

    void setMouseSensitivity(float mouseSensitivity);

    void reset(Context context);

    boolean isMouseYAxisInverted();

    void setMouseYAxisInverted(boolean mouseYAxisInverted);

    ControllerInfo getController(String name);
}
