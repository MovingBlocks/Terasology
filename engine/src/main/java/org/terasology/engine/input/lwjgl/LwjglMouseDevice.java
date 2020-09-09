// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.lwjgl;

import com.google.common.collect.Queues;
import org.joml.Vector2i;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.nui.input.ButtonState;
import org.terasology.nui.input.InputType;
import org.terasology.nui.input.device.MouseAction;
import org.terasology.nui.input.device.MouseDevice;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Queue;

/**
 *
 */
public class LwjglMouseDevice implements MouseDevice, PropertyChangeListener {
    private final RenderingConfig renderingConfig;
    private float uiScale;
    private boolean mouseGrabbed;

    public LwjglMouseDevice(Context context) {
        this.renderingConfig = context.get(Config.class).getRendering();
        this.uiScale = this.renderingConfig.getUiScale() / 100f;
        this.renderingConfig.subscribe(RenderingConfig.UI_SCALE, this);
    }

    public Vector2i getPosition() {
        return new Vector2i((int) (Mouse.getX() / this.uiScale),
                (int) ((Display.getHeight() - Mouse.getY()) / this.uiScale));
    }

    @Override
    public Vector2i getDelta() {
        return new Vector2i(Mouse.getDX(), -Mouse.getDY());
    }

    @Override
    public boolean isButtonDown(int button) {
        return Mouse.isButtonDown(button);
    }

    @Override
    public boolean isVisible() {
        return !Mouse.isGrabbed();
    }

    @Override
    public void setGrabbed(boolean newGrabbed) {
        if (newGrabbed != mouseGrabbed) {
            mouseGrabbed = newGrabbed;
            Mouse.setGrabbed(newGrabbed);
        }
    }

    @Override
    public Queue<MouseAction> getInputQueue() {
        Queue<MouseAction> result = Queues.newArrayDeque();

        while (Mouse.next()) {
            if (Mouse.getEventButton() != -1) {
                ButtonState state = (Mouse.getEventButtonState()) ? ButtonState.DOWN : ButtonState.UP;
                result.add(new MouseAction(InputType.MOUSE_BUTTON.getInput(Mouse.getEventButton()), state,
                        getPosition()));
            }
            if (Mouse.getEventDWheel() != 0) {
                int id = (Mouse.getEventDWheel() > 0) ? 1 : -1;
                result.add(new MouseAction(InputType.MOUSE_WHEEL.getInput(id), id * Mouse.getEventDWheel() / 120,
                        getPosition()));
            }
        }

        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RenderingConfig.UI_SCALE)) {
            this.uiScale = this.renderingConfig.getUiScale() / 100f;
        }
    }
}
