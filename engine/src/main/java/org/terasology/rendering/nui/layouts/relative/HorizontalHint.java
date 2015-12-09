/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layouts.relative;

import org.terasology.rendering.nui.HorizontalAlign;

/**
 * Information on how to horizontally position a widget.
 *
 */
public class HorizontalHint {
    private int width;

    private HorizontalInfo positionLeft;
    private HorizontalInfo positionRight;
    private HorizontalInfo positionCenter;

    public static HorizontalHint create() {
        return new HorizontalHint();
    }

    /**
     * Sets the width of this widget to a fixed value
     *
     * @param value
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint fixedWidth(int value) {
        this.width = value;
        return this;
    }

    /**
     * Centers the widget in the drawing region.
     *
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint center() {
        return centerRelativeTo("", HorizontalAlign.CENTER, 0);
    }

    /**
     * Centers the widget, with a pixel offset.
     *
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint center(int offset) {
        return centerRelativeTo("", HorizontalAlign.CENTER, offset);
    }

    /**
     * Centers the widget on part of the drawing area
     *
     * @param targetPart
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint center(HorizontalAlign targetPart) {
        return centerRelativeTo("", targetPart, 0);
    }

    /**
     * Centers the widget on part of the drawing area but with a pixel offset
     *
     * @param targetPart
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint center(HorizontalAlign targetPart, int offset) {
        return centerRelativeTo("", targetPart, offset);
    }

    /**
     * Centers the widget, relative to part of another widget.
     *
     * @param widgetId   The id of the other widget to center against
     * @param targetPart
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint centerRelativeTo(String widgetId, HorizontalAlign targetPart) {
        return centerRelativeTo(widgetId, targetPart, 0);
    }

    /**
     * Centers the widget relative to part of another widget, with a pixel offset.
     *
     * @param widgetId
     * @param targetPart
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint centerRelativeTo(String widgetId, HorizontalAlign targetPart, int offset) {
        positionCenter = new HorizontalInfo();
        positionCenter.setTarget(targetPart);
        positionCenter.setWidget(widgetId);
        positionCenter.setOffset(offset);
        return this;
    }

    /**
     * Aligns the left edge of the widget to the left edge of the draw region
     *
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignLeft() {
        return alignLeftRelativeTo("", HorizontalAlign.LEFT, 0);
    }

    /**
     * Aligns the left edge of the widget to the left edge of the draw region, with a pixel offset
     *
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignLeft(int offset) {
        return alignLeftRelativeTo("", HorizontalAlign.LEFT, offset);
    }

    /**
     * Aligns the left edge of the widget against a specified part of the draw region
     *
     * @param targetPart
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignLeft(HorizontalAlign targetPart) {
        return alignLeftRelativeTo("", targetPart, 0);
    }

    /**
     * Aligns the left edge of the widget against a specified part of the draw region, with a pixel offset
     *
     * @param targetPart
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignLeft(HorizontalAlign targetPart, int offset) {
        return alignLeftRelativeTo("", targetPart, offset);
    }

    /**
     * Aligns the left edge of the widget against part of a target widget
     *
     * @param widgetId
     * @param targetPart
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignLeftRelativeTo(String widgetId, HorizontalAlign targetPart) {
        return alignLeftRelativeTo(widgetId, targetPart, 0);
    }

    /**
     * Aligns the left edge of the widget against part of a target widget, with a pixel offset
     *
     * @param widgetId
     * @param targetPart
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignLeftRelativeTo(String widgetId, HorizontalAlign targetPart, int offset) {
        positionLeft = new HorizontalInfo();
        positionLeft.setTarget(targetPart);
        positionLeft.setWidget(widgetId);
        positionLeft.setOffset(offset);
        return this;
    }

    /**
     * Aligns the right edge of the widget to the right edge of the draw area
     *
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignRight() {
        return alignRightRelativeTo("", HorizontalAlign.RIGHT, 0);
    }

    /**
     * Aligns the right edge of the widget to the right edge of the draw area, with a pixel offset
     *
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignRight(int offset) {
        return alignRightRelativeTo("", HorizontalAlign.RIGHT, offset);
    }

    /**
     * Aligns the right edge of the widget to the given part of the draw area
     *
     * @param targetPart
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignRight(HorizontalAlign targetPart) {
        return alignRightRelativeTo("", targetPart, 0);
    }

    /**
     * Aligns the right edge of the widget to the given part of the draw area, with a pixel offset
     *
     * @param targetPart
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignRight(HorizontalAlign targetPart, int offset) {
        return alignRightRelativeTo("", targetPart, offset);
    }

    /**
     * Aligns the right edge of the widget against the specified part of another widget
     *
     * @param widgetId
     * @param targetPart
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignRightRelativeTo(String widgetId, HorizontalAlign targetPart) {
        return alignRightRelativeTo(widgetId, targetPart, 0);
    }

    /**
     * Aligns the right edge of the widget against the specified part of another widget, with a pixel offset
     *
     * @param widgetId
     * @param targetPart
     * @param offset
     * @return The horizontal hint for method chaining.
     */
    public HorizontalHint alignRightRelativeTo(String widgetId, HorizontalAlign targetPart, int offset) {
        positionRight = new HorizontalInfo();
        positionRight.setTarget(targetPart);
        positionRight.setWidget(widgetId);
        positionRight.setOffset(offset);
        return this;
    }

    /**
     * @return The fixed width of the content, or 0 if unfixed
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return Information on how to position the left edge of the content
     */
    public HorizontalInfo getPositionLeft() {
        return positionLeft;
    }

    /**
     * @return Information on how to position the right edge of the content
     */
    public HorizontalInfo getPositionRight() {
        return positionRight;
    }

    /**
     * @return Information on how to position the center of the content
     */
    public HorizontalInfo getPositionCenter() {
        return positionCenter;
    }
}
