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
package org.terasology.rendering.gui.framework;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.rendering.gui.components.UIScrollBar;
import org.terasology.rendering.gui.framework.events.ScrollListener;

import javax.vecmath.Vector2f;


public class UIScrollableDisplayContainer extends UIDisplayContainer {

    /*
     * ScrollBars
     */
    protected UIScrollBar _scrollBarVertical = null;
    protected UIScrollBar _scrollBarHorizontal = null;

    private float _contentHeight = 1.0f;
    private float _contentWidth = 1.0f;
    private float _scrollShiftVertical = 0.0f;
    private float _scrollShiftHorizontal = 0.0f;

    private float _oldVertivalValue = 0.0f;
    private float _oldHorizontalValue = 0.0f;

    private Vector2f _containerPosVertical = null;
    private Vector2f _containerPosHorizontal = null;

    public UIScrollableDisplayContainer() {
        super();
        _scrollBarVertical = new UIScrollBar(getSize(), UIScrollBar.ScrollType.vertical);
        _scrollBarHorizontal = new UIScrollBar(getSize(), UIScrollBar.ScrollType.horizontal);

        _scrollBarVertical.setVisible(true);
        _scrollBarHorizontal.setVisible(true);

        _scrollBarVertical.setCroped(false);
        _scrollBarHorizontal.setCroped(false);

        addDisplayElement(_scrollBarVertical);
        addDisplayElement(_scrollBarHorizontal);

        _scrollBarVertical.addScrollListener(new ScrollListener() {
            public void scrolled(UIDisplayElement element) {
                float shift = (_scrollBarVertical.getValue() - _oldVertivalValue);
                _scrollShiftVertical += shift;

                for (UIDisplayElement displayElement : getDisplayElements()) {
                    if (!displayElement.isFixed()) {
                        displayElement.getPosition().y -= shift;
                    }
                }
                _oldVertivalValue = _scrollBarVertical.getValue();
            }
        });

        _scrollBarHorizontal.addScrollListener(new ScrollListener() {
            public void scrolled(UIDisplayElement element) {
                float shift = (_scrollBarHorizontal.getValue() - _oldHorizontalValue);
                _scrollShiftHorizontal += shift;

                for (UIDisplayElement displayElement : getDisplayElements()) {
                    if (!displayElement.isFixed()) {
                        displayElement.getPosition().x -= shift;
                    }
                }
                _oldHorizontalValue = _scrollBarHorizontal.getValue();
            }
        });
    }

    public void setScrollBarsPosition(Vector2f position, Vector2f size) {
        _containerPosVertical = new Vector2f(position.x + size.x - 15f, position.y);
        _scrollBarVertical.setPosition(_containerPosVertical);
        _scrollBarVertical.setMaxMin(0.0f, getSize().y - 15f);

        _containerPosHorizontal = new Vector2f(position.x, position.y + size.y - 15f);
        _scrollBarHorizontal.setPosition(_containerPosHorizontal);
        _scrollBarHorizontal.setMaxMin(0.0f, getSize().x - 15f);
    }

    public void render() {
        super.render();
    }

    public UIScrollBar getScrollBarVertival(){
        return _scrollBarVertical;
    }

    public UIScrollBar getScrollBarHorizontal(){
        return _scrollBarHorizontal;
    }

    public void update() {

        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if (intersects(mousePos)) {
            _scrollBarVertical.setWheelled(true);
        } else {
            _scrollBarVertical.setWheelled(false);
        }

        boolean verticalScrollIsScrolled = _scrollBarVertical.isScrolled();
        boolean horizontalScrollIsScrolled = _scrollBarHorizontal.isScrolled();
        float checkConfusionVertical = 0.0f;
        float checkConfusionHorizontal = 0.0f;


        if (!verticalScrollIsScrolled) {
            _contentHeight = 0.0f;
        }

        if (!horizontalScrollIsScrolled) {
            _contentWidth = 0.0f;
        }

        for (UIDisplayElement displayElement : getDisplayElements()) {
            if (!displayElement.isFixed()) {
                if (!verticalScrollIsScrolled) {
                    if (_contentHeight <= (displayElement.getPosition().y + _scrollShiftVertical + displayElement.getSize().y)) {
                        _contentHeight = displayElement.getPosition().y + displayElement.getSize().y + _scrollShiftVertical;
                    }

                    if (!_scrollBarVertical.isVisible() && displayElement.getPosition().y < checkConfusionVertical) {
                        checkConfusionVertical = displayElement.getPosition().y;
                    }
                }

                if (!horizontalScrollIsScrolled) {
                    if (_contentWidth <= (displayElement.getPosition().x + _scrollShiftHorizontal + displayElement.getSize().x)) {
                        _contentWidth = displayElement.getPosition().x + displayElement.getSize().x + _scrollShiftHorizontal;
                    }

                    if (!_scrollBarHorizontal.isVisible() && displayElement.getPosition().x < checkConfusionHorizontal) {
                        checkConfusionHorizontal = displayElement.getPosition().x;
                    }
                }
            }
        }


        if (_contentHeight <= getSize().y && _scrollBarVertical.isVisible()) {
            _scrollBarVertical.setVisible(false);
        } else if (_contentHeight > getSize().y && !_scrollBarVertical.isVisible()) {
            _scrollBarVertical.setPosition(_containerPosVertical);
            _scrollBarVertical.setVisible(true);
        }

        if (_contentWidth <= getSize().x && _scrollBarHorizontal.isVisible()) {
            _scrollBarHorizontal.setVisible(false);
        } else if (_contentWidth > getSize().x && !_scrollBarHorizontal.isVisible()) {
            _scrollBarHorizontal.setPosition(_containerPosHorizontal);
            _scrollBarHorizontal.setVisible(true);
        }

        if (checkConfusionVertical < 0.0f) {
            for (UIDisplayElement displayElement : getDisplayElements()) {
                if (!displayElement.isFixed()) {
                    displayElement.getPosition().y += (-1) * checkConfusionVertical;
                }
            }
        }

        if (checkConfusionHorizontal < 0.0f) {
            for (UIDisplayElement displayElement : getDisplayElements()) {
                if (!displayElement.isFixed()) {
                    displayElement.getPosition().x += (-1) * checkConfusionHorizontal;
                }
            }
        }

        _scrollBarVertical.setStep(_contentHeight, getSize().y - 15f);

        _scrollBarHorizontal.setStep(_contentWidth, getSize().x - 15f);

        super.update();
    }
}