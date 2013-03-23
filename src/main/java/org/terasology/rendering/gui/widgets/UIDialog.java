/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.gui.widgets;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.DialogListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.layout.RowLayout;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;

/**
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         TODO Remove UIDialogBackground -> should use the style UIStyleBackgroundSplit
 */
public class UIDialog extends UIWindow {

    //events
    private final ArrayList<DialogListener> dialogListeners = new ArrayList<DialogListener>();

    //drag
    private final Vector2f pressedOffset = new Vector2f(0f, 0f);
    private boolean dragged = false;

    //others
    public static enum EReturnCode {
        OK, CANCEL, YES, NO, NONE
    }

    ;
    private float titleWidth = 300f;

    //child elements
    private UIComposite container;
    private UIComposite dialogButtons;
    private UICompositeScrollable dialogArea;

    private UIImage overlay;
    private UIDialogBackground topBar;
    //private final UIButton closeButton;

    @Deprecated
    private class UIDialogBackground extends UIDisplayContainer {
        private UIImage leftBackground;
        private UIImage centerBackground;
        private UIImage rightBackground;
        private UILabel text;

        public UIDialogBackground(Vector2f size) {
            setSize(size);

            text = new UILabel();
            text.setColor(Color.orange);
            text.setVisible(true);
            text.setTextShadow(true);

            leftBackground = new UIImage(Assets.getTexture("engine:gui_menu"));
            leftBackground.setSize(new Vector2f(7f, 19f));
            leftBackground.setTextureSize(new Vector2f(7f, 19f));
            leftBackground.setTextureOrigin(new Vector2f(111f, 155f));
            leftBackground.setVisible(true);

            centerBackground = new UIImage(Assets.getTexture("engine:gui_menu"));
            centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
            centerBackground.setTextureSize(new Vector2f(51f, 19f));
            centerBackground.setTextureOrigin(new Vector2f(118f, 155f));
            centerBackground.setPosition(new Vector2f(centerBackground.getPosition().x + leftBackground.getSize().x, 0f));
            centerBackground.setVisible(true);

            rightBackground = new UIImage(Assets.getTexture("engine:gui_menu"));
            rightBackground.setSize(new Vector2f(8f, 19f));
            rightBackground.setTextureSize(new Vector2f(8f, 19f));
            rightBackground.setTextureOrigin(new Vector2f(189f, 155f));
            rightBackground.setVisible(true);
            rightBackground.setPosition(new Vector2f(centerBackground.getPosition().x + centerBackground.getSize().x, 0f));
            addDisplayElement(leftBackground);
            addDisplayElement(centerBackground);
            addDisplayElement(rightBackground);
            addDisplayElement(text);
        }

        public String getTitle() {
            return text.getText();
        }

        public void setTitle(String title) {
            text.setText(title);
            text.setPosition(new Vector2f(getSize().x / 2 - text.getSize().x / 2, 0f));
        }

        public void resize() {
            centerBackground.setSize(new Vector2f(getSize().x - 19f, 19f));
            centerBackground.setPosition(new Vector2f(leftBackground.getPosition().x + leftBackground.getSize().x, 0f));
            rightBackground.setPosition(new Vector2f(centerBackground.getPosition().x + centerBackground.getSize().x, 0f));
            text.setPosition(new Vector2f(getSize().x / 2 - text.getSize().x / 2, 0f));
        }
    }

    public UIDialog(Vector2f size) {
        super();
        setSize(size);
        setModal(true);
        setBackgroundImage("engine:gui_menu", new Vector2f(260f, 92f), new Vector2f(168f, 76f));
        setBorderImage("engine:gui_menu", new Vector2f(256f, 90f), new Vector2f(175f, 88f), new Vector4f(4f, 4f, 4f, 4f));
        setPositionType(EPositionType.ABSOLUTE);
        resetPosition();
        setEnableScrolling(true);
        create();
        createDialogArea(dialogArea);
        createButtons(dialogButtons);
        container.applyLayout();
    }

    @Override
    public void layout() {
        super.layout();

        if (topBar != null)
            topBar.resize();
    }

    @Override
    public void open() {
        super.open();
    }

    @Override
    public void close() {
        super.close();

        notifyDialogListeners(EReturnCode.NONE, null);
    }

    /**
     * Open the dialog.
     */
    public void openDialog() {
        super.open();
    }

    /**
     * Closes the dialog and returns a return code and a return value.
     *
     * @param code        The code which can be <i>OK</i>, <i>CANCEL</i>, <i>YES</i>, <i>NO</i>, <i>NONE</i>.
     * @param returnValue The value which can be any object or null if no return value should be returned.
     */
    public void closeDialog(EReturnCode code, Object returnValue) {
        super.close();

        notifyDialogListeners(code, returnValue);
    }

    /**
     * Create the basic dialog elements. Override in the extended dialog class to customize the dialog.
     */
    protected void create() {
        overlay = new UIImage(new Color(0, 0, 0, 200));
        overlay.setPositionType(EPositionType.ABSOLUTE);
        overlay.setSize("100%", "100%");
        overlay.setVisible(true);

        topBar = new UIDialogBackground(new Vector2f(titleWidth, 19f));
        topBar.addMouseButtonListener(new MouseButtonListener() {
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                dragged = false;
                pressedOffset.x = 0f;
                pressedOffset.y = 0f;
            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (intersect) {
                    dragged = true;
                    Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
                    pressedOffset.x = mousePos.x - getPosition().x;
                    pressedOffset.y = mousePos.y - getPosition().y;
                }
            }
        });
        topBar.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void move(UIDisplayElement element) {
                if (dragged) {
                    Vector2f pos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());
                    pos.x -= pressedOffset.x;
                    pos.y -= pressedOffset.y;
                    setPosition(pos);
                }
            }

            @Override
            public void leave(UIDisplayElement element) {

            }

            @Override
            public void hover(UIDisplayElement element) {

            }

            @Override
            public void enter(UIDisplayElement element) {

            }
        });
        topBar.setHorizontalAlign(EHorizontalAlign.CENTER);
        topBar.setSize(new Vector2f(titleWidth, 19f));
        topBar.setVisible(true);
        
        /*
        closeButton = new UIButton(new Vector2f(19f, 19f), UIButton.ButtonType.NORMAL);
        closeButton.setPosition(new Vector2f(getSize().x - closeButton.getSize().x - 2, 2));
        closeButton.getLabel().setText("");
        closeButton.setTexture("engine:gui_menu");
        closeButton.setNormalState(new Vector2f(73f, 155f), new Vector2f(19f, 19f));
        closeButton.setHoverState(new Vector2f(54f, 155f), new Vector2f(19f, 19f));
        closeButton.setPressedState(new Vector2f(92f, 155f), new Vector2f(19f, 19f));
        closeButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                close();
            }
        });
        
        closeButton.setHorizontalAlign(EHorizontalAlign.RIGHT);
        closeButton.setPosition(new Vector2f(-2, 0f));
        closeButton.setVisible(true);
        */

        //addDisplayElement(closeButton);

        container = new UIComposite();
        container.setSize("100%", "100%");
        container.setLayout(new GridLayout(1));
        container.setVisible(true);

        dialogArea = new UICompositeScrollable();
        dialogArea.setSize("100%", "100%");
        dialogArea.setVisible(true);

        dialogButtons = new UIComposite();
        RowLayout layout = new RowLayout();
        layout.setSpacingHorizontal(5);
        dialogButtons.setLayout(layout);
        dialogButtons.setHorizontalAlign(EHorizontalAlign.CENTER);
        dialogButtons.setVerticalAlign(EVerticalAlign.BOTTOM);
        dialogButtons.setPosition(new Vector2f(0f, -10f));
        dialogButtons.setVisible(true);

        container.addDisplayElement(dialogArea);
        dialogArea.addDisplayElement(dialogButtons);
        addDisplayElement(container);

        addDisplayElement(topBar);
    }

    /**
     * Creates the dialog area. Override in the extended dialog class to customize the dialog.
     *
     * @param parent The parent where all the dialog elements should be added.
     */
    protected void createDialogArea(UIDisplayContainer parent) {

    }

    /**
     * Creates the buttons. Override in the extended dialog class to customize the dialog.
     *
     * @param parent The parent where all buttons should be added.
     */
    protected void createButtons(UIDisplayContainer parent) {

    }

    /**
     * Reset the position of the dialog to the center of the display.
     */
    public void resetPosition() {
        setPosition(new Vector2f((Display.getWidth() / 2) - (getSize().x / 2), (Display.getHeight() / 2) - (getSize().y / 2)));
    }

    /**
     * Get the title which will be displayed on the top center position of the dialog.
     *
     * @return Returns the title.
     */
    public String getTitle() {
        return topBar.getTitle();
    }

    /**
     * Set the title which will be displayed on the top center position of the dialog.
     *
     * @param title The title.
     */
    public void setTitle(String title) {
        topBar.setTitle(title);
    }
    
    /*
       Event listeners
    */

    private void notifyDialogListeners(EReturnCode returnCode, Object returnValue) {
        for (DialogListener listener : dialogListeners) {
            listener.close(this, returnCode, returnValue);
        }
    }

    public void addDialogListener(DialogListener listener) {
        dialogListeners.add(listener);
    }

    public void removeDialogListener(DialogListener listener) {
        dialogListeners.remove(listener);
    }
}
