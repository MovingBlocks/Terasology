/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.input.lwjgl;

import com.google.common.collect.Lists;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.input.ButtonState;
import org.terasology.input.InputType;
import org.terasology.input.MouseInput;
import org.terasology.input.device.MouseAction;
import org.terasology.input.device.MouseDevice;
import org.terasology.math.geom.Vector2i;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Queue;

/**

 */
public class LwjglMouseDevice implements MouseDevice, PropertyChangeListener {
    private RenderingConfig renderingConfig;
    private float uiScale; // FIXME: LWJGL 3 - test and reimplement  DPI
    private boolean mouseGrabbed;
    private Queue<MouseAction> queue = Lists.newLinkedList();

    private double xpos = 0.0;
    private double ypos = 0.0;

    private double xposDelta = 0.0;
    private double yposDelta = 0.0;

    public LwjglMouseDevice(Context context) {
        this.renderingConfig = context.get(Config.class).getRendering();
        this.uiScale = this.renderingConfig.getUiScale() / 100f;
        this.renderingConfig.subscribe(RenderingConfig.UI_SCALE, this);

        // GLFW callback
        long window = GLFW.glfwGetCurrentContext();
        GLFW.glfwSetCursorPosCallback(window, this::cursorPosCallback);
        GLFW.glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        GLFW.glfwSetScrollCallback(window, this::scrollCallback);

    }

    @Override
    public Vector2i getPosition() {
        return new Vector2i((int) (xpos / this.uiScale), (int)  (ypos/ this.uiScale));
    }

    @Override
    public Vector2i getDelta() {
        Vector2i result = new Vector2i((int) xposDelta, (int) yposDelta);
        xposDelta = 0.0;
        yposDelta = 0.0;
        return result;
    }

    @Override
    public boolean isButtonDown(int button) {
        return false; //FIXME: low priority unused method
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

    private void cursorPosCallback(long window, double xpos, double ypos) {
        xposDelta = xpos - this.xpos;
        yposDelta = ypos - this.ypos;
        this.xpos = xpos;
        this.ypos = ypos;
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        ButtonState state;
        if (action == GLFW.GLFW_PRESS) {
            state = ButtonState.DOWN;
        } else if (action == GLFW.GLFW_RELEASE) {
            state = ButtonState.UP;
        } else /*if (action == GLFW.GLFW_REPEAT)*/ {
            state = ButtonState.REPEAT;
        }
        MouseInput mouseInput = MouseInput.find(InputType.MOUSE_BUTTON, button);
        queue.offer(new MouseAction(mouseInput, state, getPosition()));
    }

    private void scrollCallback(long windows, double xoffset, double yoffset) {
        if (yoffset != 0.0) {
            int id = (yoffset > 0) ? 1 : -1;
            queue.offer(new MouseAction(InputType.MOUSE_WHEEL.getInput(id), (int) (id * yoffset / 120), getPosition()));
        }
    }
}
