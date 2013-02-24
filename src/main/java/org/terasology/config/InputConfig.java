package org.terasology.config;

/**
 * @author Immortius
 */
public class InputConfig {
    private BindsConfig binds = new BindsConfig();
    private float mouseSensitivity = 0.075f;

    public BindsConfig getBinds() {
        return binds;
    }

    public float getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(float mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }
}
