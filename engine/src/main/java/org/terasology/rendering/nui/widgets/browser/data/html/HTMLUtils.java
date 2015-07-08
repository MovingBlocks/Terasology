/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.nui.widgets.browser.data.html;

import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.HorizontalAlign;
import org.terasology.rendering.nui.widgets.browser.ui.style.ContainerInteger;
import org.terasology.rendering.nui.widgets.browser.ui.style.FixedContainerInteger;
import org.terasology.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.rendering.nui.widgets.browser.ui.style.PercentageContainerInteger;
import org.xml.sax.Attributes;

public final class HTMLUtils {
    private HTMLUtils() {
    }

    public static String findAttribute(Attributes attributes, String name) {
        int size = attributes.getLength();
        for (int i = 0; i < size; i++) {
            if (attributes.getQName(i).equalsIgnoreCase(name)) {
                return attributes.getValue(i);
            }
        }
        return null;
    }

    public static ParagraphRenderStyle createParagraphRenderStyleFromCommonAttributes(Attributes attributes) {
        Color textColor = getTextColor(attributes);

        ParagraphRenderStyle.FloatStyle floatStyle = getFloatStyle(attributes);
        ParagraphRenderStyle.ClearStyle clearStyle = getClearStyle(attributes);
        HorizontalAlign horizontalAlign = getHorizontalAlign(attributes);
        Color backgroundColor = getBackgroundColor(attributes);

        ContainerInteger paragraphMarginTop = getParagraphMarginTop(attributes);
        ContainerInteger paragraphMarginBottom = getParagraphMarginBottom(attributes);
        ContainerInteger paragraphMarginLeft = getParagraphMarginLeft(attributes);
        ContainerInteger paragraphMarginRight = getParagraphMarginRight(attributes);

        ContainerInteger paragraphPaddingTop = getParagraphPaddingTop(attributes);
        ContainerInteger paragraphPaddingBottom = getParagraphPaddingBottom(attributes);
        ContainerInteger paragraphPaddingLeft = getParagraphPaddingLeft(attributes);
        ContainerInteger paragraphPaddingRight = getParagraphPaddingRight(attributes);

        ContainerInteger minimumWidth = getMinimumWidth(attributes);

        return new ParagraphRenderStyle() {
            @Override
            public Color getColor(boolean hyperlink) {
                return textColor;
            }

            @Override
            public FloatStyle getFloatStyle() {
                return floatStyle;
            }

            @Override
            public ClearStyle getClearStyle() {
                return clearStyle;
            }

            @Override
            public HorizontalAlign getHorizontalAlignment() {
                return horizontalAlign;
            }

            @Override
            public Color getParagraphBackground() {
                return backgroundColor;
            }

            @Override
            public ContainerInteger getParagraphMarginTop() {
                return paragraphMarginTop;
            }

            @Override
            public ContainerInteger getParagraphMarginBottom() {
                return paragraphMarginBottom;
            }

            @Override
            public ContainerInteger getParagraphMarginLeft() {
                return paragraphMarginLeft;
            }

            @Override
            public ContainerInteger getParagraphMarginRight() {
                return paragraphMarginRight;
            }

            @Override
            public ContainerInteger getParagraphPaddingTop() {
                return paragraphPaddingTop;
            }

            @Override
            public ContainerInteger getParagraphPaddingBottom() {
                return paragraphPaddingBottom;
            }

            @Override
            public ContainerInteger getParagraphPaddingLeft() {
                return paragraphPaddingLeft;
            }

            @Override
            public ContainerInteger getParagraphPaddingRight() {
                return paragraphPaddingRight;
            }

            @Override
            public ContainerInteger getParagraphMinimumWidth() {
                return minimumWidth;
            }
        };
    }

    private static ContainerInteger getMinimumWidth(Attributes attributes) {
        String widthStr = getStyleAttribute(attributes, "width");
        if (widthStr != null) {
            return parseContainerInteger(widthStr);
        }
        return null;
    }

    private static ContainerInteger getParagraphMarginTop(Attributes attributes) {
        return getStyleTopContainerInteger(attributes, "margin");
    }

    private static ContainerInteger getParagraphMarginBottom(Attributes attributes) {
        return getStyleBottomContainerInteger(attributes, "margin");
    }

    private static ContainerInteger getParagraphMarginLeft(Attributes attributes) {
        return getStyleLeftContainerInteger(attributes, "margin");
    }

    private static ContainerInteger getParagraphMarginRight(Attributes attributes) {
        return getStyleRightContainerInteger(attributes, "margin");
    }

    private static ContainerInteger getParagraphPaddingTop(Attributes attributes) {
        return getStyleTopContainerInteger(attributes, "padding");
    }

    private static ContainerInteger getParagraphPaddingBottom(Attributes attributes) {
        return getStyleBottomContainerInteger(attributes, "padding");
    }

    private static ContainerInteger getParagraphPaddingLeft(Attributes attributes) {
        return getStyleLeftContainerInteger(attributes, "padding");
    }

    private static ContainerInteger getParagraphPaddingRight(Attributes attributes) {
        return getStyleRightContainerInteger(attributes, "padding");
    }

    private static ContainerInteger getStyleTopContainerInteger(Attributes attributes, String groupName) {
        return getStyleContainerInteger(attributes, groupName, "top", 0, 0, 0);
    }

    private static ContainerInteger getStyleBottomContainerInteger(Attributes attributes, String groupName) {
        return getStyleContainerInteger(attributes, groupName, "bottom", 2, 2, 0);
    }

    private static ContainerInteger getStyleLeftContainerInteger(Attributes attributes, String groupName) {
        return getStyleContainerInteger(attributes, groupName, "left", 3, 1, 1);
    }

    private static ContainerInteger getStyleRightContainerInteger(Attributes attributes, String groupName) {
        return getStyleContainerInteger(attributes, groupName, "right", 1, 1, 1);
    }

    private static ContainerInteger getStyleContainerInteger(Attributes attributes, String groupName, String sideName, int fourIndex, int threeIndex, int twoIndex) {
        String elementStr = getStyleAttribute(attributes, groupName + "-" + sideName);
        if (elementStr != null) {
            return parseContainerInteger(elementStr);
        }
        String groupStr = getStyleAttribute(attributes, groupName);
        if (groupStr != null) {
            String[] groupSplit = groupStr.split(" ");
            if (groupSplit.length == 4) {
                return parseContainerInteger(groupSplit[fourIndex]);
            } else if (groupSplit.length == 3) {
                return parseContainerInteger(groupSplit[threeIndex]);
            } else if (groupSplit.length == 2) {
                return parseContainerInteger(groupSplit[twoIndex]);
            } else if (groupSplit.length == 1) {
                return parseContainerInteger(groupSplit[0]);
            }
            return null;
        }
        return null;
    }

    private static ContainerInteger parseContainerInteger(String value) {
        if (value.endsWith("px")) {
            return new FixedContainerInteger(Integer.parseInt(value.substring(0, value.length() - 2)));
        } else if (value.endsWith("%")) {
            return new PercentageContainerInteger(Integer.parseInt(value.substring(0, value.length() - 1)));
        }
        return null;
    }

    private static String getStyleAttribute(Attributes attributes, String name) {
        String styleStr = findAttribute(attributes, "style");
        if (styleStr != null) {
            for (String style : styleStr.split(";")) {
                String[] styleSplit = style.trim().split(":", 2);
                if (styleSplit.length == 2 && styleSplit[0].trim().equals(name)) {
                    return styleSplit[1].trim();
                }
            }
        }
        return null;
    }

    private static ParagraphRenderStyle.FloatStyle getFloatStyle(Attributes attributes) {
        String floatStr = getStyleAttribute(attributes, "float");
        if (floatStr == null) {
            return null;
        }
        switch (floatStr) {
            case "left":
                return ParagraphRenderStyle.FloatStyle.LEFT;
            case "right":
                return ParagraphRenderStyle.FloatStyle.RIGHT;
            case "none":
                return ParagraphRenderStyle.FloatStyle.NONE;
        }
        return null;
    }

    private static ParagraphRenderStyle.ClearStyle getClearStyle(Attributes attributes) {
        String clearStr = getStyleAttribute(attributes, "clear");
        if (clearStr == null) {
            return null;
        }
        switch (clearStr) {
            case "left":
                return ParagraphRenderStyle.ClearStyle.LEFT;
            case "right":
                return ParagraphRenderStyle.ClearStyle.RIGHT;
            case "both":
                return ParagraphRenderStyle.ClearStyle.BOTH;
            case "none":
                return ParagraphRenderStyle.ClearStyle.NONE;
        }
        return null;
    }

    private static HorizontalAlign getHorizontalAlign(Attributes attributes) {
        String alignStr = getStyleAttribute(attributes, "text-align");
        if (alignStr == null) {
            return null;
        }
        switch (alignStr) {
            case "left":
                return HorizontalAlign.LEFT;
            case "right":
                return HorizontalAlign.RIGHT;
            case "center":
                return HorizontalAlign.CENTER;
        }
        return null;
    }

    private static Color getBackgroundColor(Attributes attributes) {
        String backgroundColorStr = getStyleAttribute(attributes, "background-color");
        return parseColor(backgroundColorStr);
    }

    private static Color getTextColor(Attributes attributes) {
        String backgroundColorStr = getStyleAttribute(attributes, "color");
        return parseColor(backgroundColorStr);
    }

    private static Color parseColor(String colorStr) {
        if (colorStr == null || colorStr.length() != 7
                || colorStr.charAt(0) != '#') {
            return null;
        }
        return new Color(
                Integer.parseInt(colorStr.substring(1, 3), 16),
                Integer.parseInt(colorStr.substring(3, 5), 16),
                Integer.parseInt(colorStr.substring(5, 7), 16),
                255);
    }
}
