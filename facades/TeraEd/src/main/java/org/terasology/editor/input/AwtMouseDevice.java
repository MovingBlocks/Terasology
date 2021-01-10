// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.editor.input;

import com.google.common.collect.Lists;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.terasology.config.RenderingConfig;
import org.terasology.input.ButtonState;
import org.terasology.input.InputType;
import org.terasology.input.MouseInput;
import org.terasology.input.device.MouseAction;
import org.terasology.input.device.MouseDevice;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Queue;

/**
 * Awt mouse device convertor. Handles mouse input via AWT's callbacks Handles mouse state.
 */
public class AwtMouseDevice implements MouseDevice, PropertyChangeListener {
    private RenderingConfig renderingConfig;
    private float uiScale;
    private boolean mouseGrabbed;
    private Queue<MouseAction> queue = Lists.newLinkedList();

    private TIntSet buttonStates = new TIntHashSet();

    private double xPos;
    private double yPos;

    private double xPosDelta;
    private double yPosDelta;

    public AwtMouseDevice(RenderingConfig renderingConfig) {
        this.renderingConfig = renderingConfig;
        this.uiScale = renderingConfig.getUiScale() / 100f;
        renderingConfig.subscribe(RenderingConfig.UI_SCALE, this);
    }

    public void registerToAwtGlCanvas(AWTGLCanvas canvas) {
        canvas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int button = e.getButton() - 1;
                buttonStates.add(button);
                MouseInput mouseInput = MouseInput.find(InputType.MOUSE_BUTTON, button);
                queue.offer(new MouseAction(mouseInput, ButtonState.DOWN, getPosition()));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int button = e.getButton() - 1;
                buttonStates.remove(button);
                MouseInput mouseInput = MouseInput.find(InputType.MOUSE_BUTTON, button);
                queue.offer(new MouseAction(mouseInput, ButtonState.UP, getPosition()));
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        canvas.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                updateMouse(e.getX(), e.getY());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                updateMouse(e.getX(), e.getY());
            }
        });

        canvas.addMouseWheelListener(e -> {
            int yOffset = e.getUnitsToScroll();
            if (yOffset != 0.0) {
                int id = (yOffset > 0) ? 1 : -1;
                queue.offer(new MouseAction(InputType.MOUSE_WHEEL.getInput(id), 1, getPosition()));
            }
        });
    }

    @Override
    public void update() {
    }

    private void updateMouse(double x, double y) {
        xPosDelta = x - this.xPos;
        yPosDelta = y - this.yPos;
        this.xPos = x;
        this.yPos = y;
    }

    @Override
    public Vector2i getPosition() {
        return new Vector2i((int) (xPos / this.uiScale), (int) (yPos / this.uiScale));
    }

    @Override
    public Vector2d getDelta() {

        Vector2d result = new Vector2d(xPosDelta, yPosDelta);
        return result;
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
            // TODO handle swing mouse grabbing
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

    public void resetDelta() {
        xPosDelta = 0;
        yPosDelta = 0;
    }
}
