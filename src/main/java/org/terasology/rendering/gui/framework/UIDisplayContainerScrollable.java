/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.gui.framework;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.events.ScrollListener;
import org.terasology.rendering.gui.framework.style.Style;
import org.terasology.rendering.gui.framework.style.StyleShadow.EShadowDirection;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIImage;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.ArrayList;
import java.util.List;

/**
 * A container where the contant can be scrolled if the content is to big to be displayed.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public abstract class UIDisplayContainerScrollable extends UIDisplayContainer {

    private final List<ScrollListener> scrollListener = new ArrayList<ScrollListener>();
    private UIComposite container;

    //scrollbar
    private UIImage scrollbarBackground;
    private UIImage scrollbar;
    private boolean scrolling;                    //true if the scrollbar was pressed, this will enable scrolling
    private float scrollbarPressedOffset;         //the position on which the scrollbar was grabbed with the mouse

    //content
    private float max;                            //the max position value of all child elements
    private float contentHeight;                  //the contents height of all child elements
    private float multiplier = 1f;                //the multiplier of how the movement in the scrollbar will move the actual child elements

    //settings
    private boolean enableScrolling;
    private boolean enableScrollbar;

    //layout
    private final float scrollbarWidth = 15f;
    private Vector4f padding = new Vector4f(0f, 0f, 0f, 0f); //top, right, bottom, left

    //other
    private boolean isScrollable;

    public UIDisplayContainerScrollable() {
        setup();
    }

    public UIDisplayContainerScrollable(Vector2f size) {
        setSize(size);
        setEnableScrolling(true);
        setEnableScrollbar(true);

        setup();
    }

    private void setup() {
        container = new UIComposite();
        container.setSize("100%", "100%");
        container.setVisible(true);

        scrollbarBackground = new UIImage(new Color(110, 110, 110, 220));
        scrollbarBackground.setHorizontalAlign(EHorizontalAlign.RIGHT);
        scrollbarBackground.setShadow(new Vector4f(0f, 0f, 0f, 3f), EShadowDirection.INSIDE, 1f);
        scrollbarBackground.setSize((scrollbarWidth + 1) + "px", "100%");

        scrollbar = new UIImage(Assets.getTexture("engine:gui_menu"));
        scrollbar.setShadow(new Vector4f(0f, 3f, 0f, 3f), EShadowDirection.INSIDE, 1f);
        scrollbar.setHorizontalAlign(EHorizontalAlign.RIGHT);
        scrollbar.setCrop(false);
        scrollbar.setTextureOrigin(new Vector2f(0f, 0f));
        scrollbar.setTextureSize(new Vector2f(60f, 15f));
        scrollbar.setSize(new Vector2f(0f, 0f));
        scrollbar.setVisible(false);
        scrollbar.addMouseButtonListener(new MouseButtonListener() {
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                scrolling = false;
                scrollbarPressedOffset = 0f;
            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (intersect && enableScrolling) {
                    scrolling = true;

                    //calculate the press point on the scrollbar
                    scrollbarPressedOffset = Display.getHeight() - Mouse.getY() - scrollbar.getPosition().y - getAbsolutePosition().y;
                }
            }
        });
        scrollbar.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void move(UIDisplayElement element) {
                if (scrolling && enableScrolling) {
                    moveScrollbar(Display.getHeight() - Mouse.getY());
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

        addMouseButtonListener(new MouseButtonListener() {
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
                if (wheel != 0 && enableScrolling && isScrollable && intersect) {
                    moveScrollbar(scrollbar.getAbsolutePosition().y - (wheel / 10));
                }
            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {

            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {

            }
        });

        super.addDisplayElement(container);
        super.addDisplayElement(scrollbarBackground);
        super.addDisplayElement(scrollbar);
    }

    /**
     * Move the scrollbar.
     *
     * @param pos The position (y direction) where to move the scrollbar.
     */
    private void moveScrollbar(float pos) {
        float scrollbarPos = pos - getAbsolutePosition().y - scrollbarPressedOffset;

        if (scrollbarPos < 0) {
            scrollbarPos = 0;
        }

        if (scrollbarPos > (getSize().y - scrollbar.getSize().y)) {
            scrollbarPos = getSize().y - scrollbar.getSize().y;
        }

        //calculate the different in the position
        scrollbar.setPosition(new Vector2f(0f, scrollbarPos));

        //move the content
        container.setPosition(new Vector2f(padding.w, -multiplier * scrollbar.getPosition().y + padding.x));

        notifyScrollListeners();
    }

    /**
     * Calculate the high of the content based on the child elements within this container.
     */
    protected void calcContentHeight() {
        if (scrollbar != null) {
            container.setSize(getSize());

            if (container.getDisplayElements().size() > 0) {

                //Lets calculate the max value recursively
                max = 0;
                calcMax(container.getDisplayElements());
                contentHeight = max - container.getAbsolutePosition().y + padding.x + padding.z;
            }

            //check if the content is bigger than the container
            if (enableScrolling && contentHeight > getSize().y) {
                isScrollable = true;

                //calculate how much space needs to be scrolled by the scrollbar
                float diff = contentHeight - getSize().y;

                //calculate the new multiplier based on the size which needs to be scrolled by the scrollbar
                //TODO calculate multiplier none linear..
                multiplier = (diff / getSize().y) + 1;

                //set the new size of the scrollbar based on the multiplier
                scrollbar.setSize(new Vector2f(scrollbarWidth, getSize().y / multiplier));

                if (enableScrollbar) {
                    //enable the scrollbar
                    scrollbar.setVisible(true);
                    scrollbarBackground.setVisible(true);

                    //setCropMargin(new Vector4f(0f, -scrollbarWidth, 0f, 0f));
                    container.setSize(new Vector2f(container.getSize().x - scrollbarWidth, contentHeight));
                }

                moveScrollbar(scrollbar.getAbsolutePosition().y);
            } else {
                isScrollable = false;

                multiplier = 1.0f;

                //disable the scrollbar
                scrollbar.setVisible(false);
                scrollbarBackground.setVisible(false);

                moveScrollbar(0f);

                //setCropMargin(new Vector4f(0f, 0f, 0f, 0f));
            }
        }
    }

    /**
     * Calculate the max value of all display elements within a display container recursively.
     *
     * @param displayElements The display elements of a display container.
     */
    private void calcMax(List<UIDisplayElement> displayElements) {
        float elementMax;

        //loop through all child elements
        for (UIDisplayElement element : displayElements) {
            if (element.isVisible() && !(element instanceof Style) && element != scrollbar) {
                //recursive action if the element also contains child elements
                if (element instanceof UIDisplayContainer) {
                    calcMax(((UIDisplayContainer) element).getDisplayElements());
                }

                elementMax = element.getAbsolutePosition().y + element.getSize().y;

                if (elementMax > max) {
                    max = elementMax;
                }
            }
        }
    }

    @Override
    public void addDisplayElement(UIDisplayElement element) {
        if (element instanceof Style) {
            super.addDisplayElement(element);
        } else {
            container.addDisplayElement(element);
        }

        layout();
    }

    @Override
    public void removeDisplayElement(UIDisplayElement element) {
        container.getDisplayElements().remove(element);
        element.setParent(null);

        layout();
    }

    @Override
    public void removeAllDisplayElements() {
        for (UIDisplayElement element : container.getDisplayElements()) {
            element.setParent(null);
        }
        container.getDisplayElements().clear();

        layout();
    }

    @Override
    public List<UIDisplayElement> getDisplayElements() {
        return container.getDisplayElements();
    }

    /**
     * Scroll to the given position.
     *
     * @param pos The position where to scroll to.
     */
    public void scrollTo(float pos) {
        moveScrollbar(getPosition().y + pos / multiplier);
    }

    /**
     * Scroll to top.
     */
    public void scrollToTop() {
        if (isScrollable) {
            moveScrollbar(getPosition().y);
        }
    }

    /**
     * Check whether the content is scrolled to the top.
     *
     * @return Returns true if the content is scrolled to the top.
     */
    public boolean isScrolledToTop() {
        if (scrollbar.getPosition().y <= 0) {
            return true;
        }

        return false;
    }

    /**
     * Scroll to bottom.
     */
    public void scrollToBottom() {
        if (isScrollable) {
            moveScrollbar(getPosition().y + getSize().y - scrollbar.getSize().y + 1f);
        }
    }

    /**
     * Check whether the content is scrolled to the bottom.
     *
     * @return Returns true if the content is scrolled to the bottom.
     */
    public boolean isScrolledToBottom() {
        if (scrollbar.getPosition().y >= getSize().y - scrollbar.getSize().y) {
            return true;
        }

        return false;
    }

    /**
     * Get the current scroll position.
     *
     * @return Returns the scroll position.
     */
    public float getScrollPosition() {
        return scrollbar.getPosition().y * multiplier;
    }

    /**
     * Check if scrolling is enabled.
     *
     * @return Returns true if scrolling is enabled.
     */
    public boolean isEnableScrolling() {
        return enableScrolling;
    }

    /**
     * Set if the container allows scrolling.
     *
     * @param enable True to enable scrolling.
     */
    public void setEnableScrolling(boolean enable) {
        this.enableScrolling = enable;

        setCropContainer(enable);
    }

    /**
     * Check if the scrollbar will be displayed if the content is scrollable. If the scrollbar is disabled, scrolling can be achieved by using the mouse wheel.
     *
     * @return Returns true if the scrollbar is enabled.
     */
    public boolean isEnableScrollbar() {
        return enableScrollbar;
    }

    /**
     * Set if the scrollbar will be displayed if the content is scrollable. If the scrollbar is disabled, scrolling can be achieved by using the mouse wheel.
     *
     * @param enable True to enable scrollbar.
     */
    public void setEnableScrollbar(boolean enable) {
        this.enableScrollbar = enable;

        layout();
    }

    /**
     * Get the padding of the text within the text container.
     *
     * @return Returns the padding.
     */
    public Vector4f getPadding() {
        return padding;
    }

    /**
     * Set the padding of the text within the text container.
     *
     * @param padding The padding, where x = top, y = right, z = bottom and w = left.
     */
    public void setPadding(Vector4f padding) {
        this.padding = padding;

        container.setPosition(new Vector2f(padding.w, padding.x));

        layout();
    }

    /**
     * Get the size of the area which will be scrolled, excluding the padding and the scrollbar width. This will be the actual displayed area of the display element.
     *
     * @return Returns the scroll container size.
     */
    public Vector2f getScrollContainerSize() {
        return new Vector2f(container.getSize().x - padding.y - padding.w, container.getSize().y - padding.x - padding.z);
    }

    /**
     * Get the size of the scrollbar.
     *
     * @return Returns the size of the scrollbar.
     */
    public Vector2f getScrollbarSize() {
        return scrollbar.getSize();
    }

    /**
     * Check whether the content of the scroll container is to big and will be scrolled.
     *
     * @return Returns true if to content will be scrolled.
     */
    public boolean isScrollable() {
        return isScrollable;
    }

    /**
     * Check whether the scrollbar is visible or not.
     *
     * @return Returns true if the scrollabr is visible.
     */
    public boolean isScrollbarVisible() {
        return scrollbar.isVisible();
    }

    private void notifyScrollListeners() {
        for (ScrollListener listener : scrollListener) {
            listener.scrolled(this);
        }
    }

    public void addScrollListener(ScrollListener listener) {
        scrollListener.add(listener);
    }

    public void removeScrollListener(ScrollListener listener) {
        scrollListener.remove(listener);
    }

    @Override
    public void layout() {
        super.layout();

        calcContentHeight();
    }
}
