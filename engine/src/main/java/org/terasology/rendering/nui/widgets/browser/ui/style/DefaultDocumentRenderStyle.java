// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.ui.style;

import org.terasology.nui.Color;
import org.terasology.nui.HorizontalAlign;
import org.terasology.nui.asset.font.Font;

public class DefaultDocumentRenderStyle implements DocumentRenderStyle {
    private Font defaultFont;
    private Color defaultColor;

    public DefaultDocumentRenderStyle(Font defaultFont, Color defaultColor) {
        this.defaultFont = defaultFont;
        this.defaultColor = defaultColor;
    }

    @Override
    public ContainerInteger getDocumentMarginTop() {
        return new FixedContainerInteger(3);
    }

    @Override
    public ContainerInteger getDocumentMarginBottom() {
        return new FixedContainerInteger(3);
    }

    @Override
    public ContainerInteger getDocumentMarginLeft() {
        return new FixedContainerInteger(3);
    }

    @Override
    public ContainerInteger getDocumentMarginRight() {
        return new FixedContainerInteger(3);
    }

    @Override
    public ContainerInteger getParagraphMarginTop() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphMarginBottom() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphMarginLeft() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphMarginRight() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphPaddingTop() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphPaddingBottom() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphPaddingLeft() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphPaddingRight() {
        return new FixedContainerInteger(0);
    }

    @Override
    public ContainerInteger getParagraphMinimumWidth() {
        return new FixedContainerInteger(0);
    }

    @Override
    public Font getFont(boolean hyperlink) {
        return defaultFont;
    }

    @Override
    public Color getColor(boolean hyperlink) {
        if (hyperlink) {
            return Color.BLUE;
        } else {
            return defaultColor;
        }
    }

    @Override
    public Color getBackgroundColor() {
        return null;
    }

    @Override
    public Color getParagraphBackground() {
        return null;
    }

    @Override
    public HorizontalAlign getHorizontalAlignment() {
        return HorizontalAlign.LEFT;
    }

    @Override
    public FloatStyle getFloatStyle() {
        return FloatStyle.NONE;
    }

    @Override
    public ClearStyle getClearStyle() {
        return ClearStyle.NONE;
    }
}
