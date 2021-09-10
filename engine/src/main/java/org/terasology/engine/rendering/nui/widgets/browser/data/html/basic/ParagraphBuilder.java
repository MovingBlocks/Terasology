// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.html.basic;

import com.google.common.collect.Maps;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.FlowParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.TextFlowRenderable;
import org.terasology.nui.Color;
import org.terasology.nui.asset.font.Font;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLBlockBuilder;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLFontResolver;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLParseException;
import org.terasology.engine.rendering.nui.widgets.browser.data.html.HTMLUtils;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.TextRenderStyle;
import org.xml.sax.Attributes;

import java.util.Map;

public class ParagraphBuilder implements HTMLBlockBuilder {
    private HTMLFontResolver htmlFontResolver;

    private Map<String, FlowRenderableFactory> flowRenderableFactoryMap = Maps.newHashMap();

    private FlowParagraphData paragraphData;

    private String hyperlink;
    private String fontName;
    private boolean bold;
    private Color color;

    public ParagraphBuilder(HTMLFontResolver htmlFontResolver, Attributes attributes) {
        this.htmlFontResolver = htmlFontResolver;
        paragraphData = new FlowParagraphData(HTMLUtils.createParagraphRenderStyleFromCommonAttributes(attributes));
    }

    public void addKnownFlowTag(String tag, FlowRenderableFactory flowRenderableFactory) {
        flowRenderableFactoryMap.put(tag, flowRenderableFactory);
    }

    @Override
    public boolean startTag(String tag, Attributes attributes) {
        if (tag.equalsIgnoreCase("b")) {
            bold = true;
            return true;
        } else if (tag.equalsIgnoreCase("font")) {
            String name = HTMLUtils.findAttribute(attributes, "name");
            if (name != null) {
                fontName = name;
            }
            String colorAttr = HTMLUtils.findAttribute(attributes, "color");
            if (colorAttr != null && colorAttr.length() == 7 && colorAttr.charAt(0) == '#') {
                color = new Color(Integer.parseInt(colorAttr.substring(1), 16));
            }
            return true;
        } else if (tag.equalsIgnoreCase("a")) {
            String href = HTMLUtils.findAttribute(attributes, "href");
            if (href == null) {
                throw new HTMLParseException("Expected href attribute");
            }
            hyperlink = href;
        }

        for (Map.Entry<String, FlowRenderableFactory> flowRenderableFactoryEntry : flowRenderableFactoryMap.entrySet()) {
            if (flowRenderableFactoryEntry.getKey().equalsIgnoreCase(tag)) {
                paragraphData.append(flowRenderableFactoryEntry.getValue()
                        .create(attributes, htmlFontResolver.getFont(fontName, bold), color, hyperlink));
                return true;
            }
        }

        return false;
    }

    @Override
    public void text(String text) {
        StaticTextRenderStyle renderStyle = new StaticTextRenderStyle(htmlFontResolver.getFont(fontName, bold), color);
        paragraphData.append(new TextFlowRenderable(text, renderStyle, hyperlink));
    }

    @Override
    public void endTag(String tag) {
        if (tag.equalsIgnoreCase("b")) {
            bold = false;
        } else if (tag.equalsIgnoreCase("font")) {
            fontName = null;
            color = null;
        } else if (tag.equalsIgnoreCase("a")) {
            hyperlink = null;
        }
    }

    @Override
    public ParagraphData build() {
        return paragraphData;
    }

    private static final class StaticTextRenderStyle implements TextRenderStyle {
        private Font font;
        private Color color;

        private StaticTextRenderStyle(Font font, Color color) {
            this.font = font;
            this.color = color;
        }

        @Override
        public Font getFont(boolean hyperlink) {
            return font;
        }

        @Override
        public Color getColor(boolean hyperlink) {
            return color;
        }
    }
}
