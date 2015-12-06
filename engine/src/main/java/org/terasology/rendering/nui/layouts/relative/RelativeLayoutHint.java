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

import com.google.gson.annotations.SerializedName;
import org.terasology.rendering.nui.LayoutHint;

/**
 */
public class RelativeLayoutHint implements LayoutHint {

    private int width;
    private int height;
    @SerializedName("use-content-width")
    private boolean usingContentWidth;
    @SerializedName("use-content-height")
    private boolean usingContentHeight;

    @SerializedName("position-left")
    private HorizontalInfo positionLeft;
    @SerializedName("position-right")
    private HorizontalInfo positionRight;
    @SerializedName("position-horizontal-center")
    private HorizontalInfo positionCenterHorizontal;


    @SerializedName("position-top")
    private VerticalInfo positionTop;
    @SerializedName("position-bottom")
    private VerticalInfo positionBottom;
    @SerializedName("position-vertical-center")
    private VerticalInfo positionCenterVertical;

    public RelativeLayoutHint() {
    }

    public RelativeLayoutHint(HorizontalHint horizontal, VerticalHint vertical) {
        width = horizontal.getWidth();
        positionLeft = horizontal.getPositionLeft();
        positionCenterHorizontal = horizontal.getPositionCenter();
        positionRight = horizontal.getPositionRight();

        height = vertical.getHeight();
        positionTop = vertical.getPositionTop();
        positionCenterVertical = vertical.getPositionCenter();
        positionBottom = vertical.getPositionBottom();
    }

    public int getWidth() {
        return width;
    }

    public RelativeLayoutHint setWidth(int value) {
        this.width = value;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public RelativeLayoutHint setHeight(int value) {
        this.height = value;
        return this;
    }

    public HorizontalInfo getPositionLeft() {
        return positionLeft;
    }

    public RelativeLayoutHint setPositionLeft(HorizontalInfo value) {
        this.positionLeft = value;
        return this;
    }

    public HorizontalInfo getPositionRight() {
        return positionRight;
    }

    public RelativeLayoutHint setPositionRight(HorizontalInfo value) {
        this.positionRight = value;
        return this;
    }

    public HorizontalInfo getPositionCenterHorizontal() {
        return positionCenterHorizontal;
    }

    public RelativeLayoutHint setPositionCenterHorizontal(HorizontalInfo value) {
        this.positionCenterHorizontal = value;
        return this;
    }

    public VerticalInfo getPositionTop() {
        return positionTop;
    }

    public RelativeLayoutHint setPositionTop(VerticalInfo value) {
        this.positionTop = value;
        return this;
    }

    public VerticalInfo getPositionBottom() {
        return positionBottom;
    }

    public RelativeLayoutHint setPositionBottom(VerticalInfo value) {
        this.positionBottom = value;
        return this;
    }

    public VerticalInfo getPositionCenterVertical() {
        return positionCenterVertical;
    }

    public RelativeLayoutHint setPositionCenterVertical(VerticalInfo value) {
        this.positionCenterVertical = value;
        return this;
    }

    public boolean isUsingContentWidth() {
        return usingContentWidth;
    }

    public RelativeLayoutHint setUsingContentWidth(boolean value) {
        this.usingContentWidth = value;
        return this;
    }

    public boolean isUsingContentHeight() {
        return usingContentHeight;
    }

    public RelativeLayoutHint setUsingContentHeight(boolean value) {
        this.usingContentHeight = value;
        return this;
    }
}
