// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.input.lwjgl;

import com.google.common.collect.Lists;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.input.ButtonState;
import org.terasology.input.InputType;
import org.terasology.input.MouseInput;
import org.terasology.input.device.MouseAction;
import org.terasology.input.device.MouseDevice;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.DoubleBuffer;
import java.util.Queue;

/**
 * Lwjgl 3's (GLFW) mouse device representation.
 * Handles mouse input via GLFW's callbacks.
 * Handles mouse state.
 */
public class LwjglMouseDevice implements MouseDevice, PropertyChangeListener {
    private RenderingConfig renderingConfig;
    private float uiScale;
    private boolean mouseGrabbed;
    private Queue<MouseAction> queue = Lists.newLinkedList();

    private TIntSet buttonStates = new TIntHashSet();

    private double xpos;
    private double ypos;

    private double xposDelta;
    private double yposDelta;

    public LwjglMouseDevice(RenderingConfig renderingConfig) {
        this.renderingConfig = renderingConfig;
        this.uiScale = renderingConfig.getUiScale() / 100f;
        renderingConfig.subscribe(RenderingConfig.UI_SCALE, this);
    }

    public void registerToLwjglWindow(long window) {
        GLFW.glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        GLFW.glfwSetScrollCallback(window, this::scrollCallback);
        GLFW.glfwSetWindowFocusCallback(window, this::focusCallback);
    }

    @Override
    public void update() {
        long window = GLFW.glfwGetCurrentContext();
        DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);

        GLFW.glfwGetCursorPos(window, mouseX, mouseY);

        double x = mouseX.get(0);
        double y = mouseY.get(0);

        xposDelta = x - this.xpos;
        yposDelta = y - this.ypos;
        this.xpos = x;
        this.ypos = y;
    }

    @Override
    public Vector2i getPosition() {
        return new Vector2i((int) (xpos / this.uiScale), (int) (ypos / this.uiScale));
    }

    @Override
    public Vector2d getDelta() {
        return new Vector2d(xposDelta, yposDelta);
    }

    @Override
    public boolean isButtonDown(int button) {
        return buttonStates.contains(button);
    }

    @Override
    public boolean isVisible() {
        return !mouseGrabbed;
    }

    @Override
    public void setGrabbed(boolean newGrabbed) {
        if (newGrabbed != mouseGrabbed) {
            mouseGrabbed = newGrabbed;
            GLFW.glfwSetInputMode(GLFW.glfwGetCurrentContext(), GLFW.GLFW_CURSOR,
                    newGrabbed ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    @Override
    public Queue<MouseAction> getInputQueue() {
        Queue<MouseAction> mouseActions = Lists.newLinkedList();
        MouseAction action;
        while ((action = queue.poll()) != null) {
            mouseActions.add(action);
        }
        return mouseActions;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RenderingConfig.UI_SCALE)) {
            this.uiScale = this.renderingConfig.getUiScale() / 100f;
        }
    }

    private void focusCallback(long window, boolean isFocused) {
        // When the window is unfocused and focused again, the mouse coordinates
        // reported by GLFW change even if the mouse hasn't moved.
        // So when the window regains focus, we set the new position as the reference without generating deltas.
        if (isFocused) {
            DoubleBuffer mouseX = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer mouseY = BufferUtils.createDoubleBuffer(1);

            GLFW.glfwGetCursorPos(window, mouseX, mouseY);

            xpos = mouseX.get(0);
            ypos = mouseY.get(0);
        }
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        ButtonState state;
        if (action == GLFW.GLFW_PRESS) {
            state = ButtonState.DOWN;
            buttonStates.add(button);
        } else if (action == GLFW.GLFW_RELEASE) {
            state = ButtonState.UP;
            buttonStates.remove(button);
        } else /*if (action == GLFW.GLFW_REPEAT)*/ {
            state = ButtonState.REPEAT;
        }
        MouseInput mouseInput = MouseInput.find(InputType.MOUSE_BUTTON, button);
        queue.offer(new MouseAction(mouseInput, state, getPosition()));
    }

    private void scrollCallback(long windows, double xoffset, double yoffset) {
        if (yoffset != 0.0) {
            int id = (yoffset > 0) ? 1 : -1;
            queue.offer(new MouseAction(InputType.MOUSE_WHEEL.getInput(id), 1, getPosition()));
        }
    }
}
