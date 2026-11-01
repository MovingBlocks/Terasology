// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.widgets.browser.data.basic;

import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.FlowRenderable;
import org.terasology.engine.rendering.nui.widgets.browser.data.basic.flow.TextFlowRenderable;
import org.terasology.nui.Color;
import org.terasology.nui.asset.font.Font;
import org.terasology.engine.rendering.nui.widgets.browser.data.ParagraphData;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.ParagraphRenderStyle;
import org.terasology.engine.rendering.nui.widgets.browser.ui.style.TextRenderStyle;
import org.terasology.engine.utilities.Assets;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public final class HTMLLikeParser {
    private HTMLLikeParser() {
    }

    public static String encodeHTMLLike(String text) {
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '&') {
                result.append("&amp;");
            } else if (c == '<') {
                result.append("&lt;");
            } else if (c == '>') {
                result.append("&gt;");
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static String unencodeHTMLLike(String text) {
        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();
        int i = 0;
        while (i < chars.length) {
            char c = chars[i];
            if (c == '&') {
                if (i + 4 < chars.length && chars[i + 1] == 'a' && chars[i + 2] == 'm' && chars[i + 3] == 'p' && chars[i + 4] == ';') {
                    result.append('&');
                    i += 4;
                } else if (i + 3 < chars.length && chars[i + 1] == 'l' && chars[i + 2] == 't' && chars[i + 3] == ';') {
                    result.append('<');
                    i += 3;
                } else if (i + 3 < chars.length && chars[i + 1] == 'g' && chars[i + 2] == 't' && chars[i + 3] == ';') {
                    result.append('>');
                    i += 3;
                } else {
                    throw new IllegalArgumentException("Invalid entity definition.");
                }
            } else {
                result.append(c);
            }

            i++;
        }
        return result.toString();
    }

    public static Collection<FlowRenderable> parseHTMLLike(String text) {
        if (text == null) {
            return null;
        }

        int character;
        try (StringReader reader = new StringReader(text)) {
            StringBuilder sb = new StringBuilder();
            Font font = null;
            Color color = null;
            String hyperlink = null;

            List<FlowRenderable> result = new LinkedList<>();
            while ((character = reader.read()) != -1) {
                char c = (char) character;
                if (c == '\n') {
                    throw new IllegalArgumentException("Parsed text cannot contain line breaks.");
                } else if (c == '<') {
                    char nextChar = (char) reader.read();
                    if (nextChar == '/') {
                        char id = (char) reader.read();
                        if (id == 'f') {
                            if (sb.length() > 0) {
                                result.add(new TextFlowRenderable(sb.toString(), new DefaultTextRenderStyle(font, color), hyperlink));
                                sb.setLength(0);
                            }
                            font = null;
                        } else if (id == 'c') {
                            if (sb.length() > 0) {
                                result.add(new TextFlowRenderable(sb.toString(), new DefaultTextRenderStyle(font, color), hyperlink));
                                sb.setLength(0);
                            }
                            color = null;
                        } else if (id == 'h') {
                            if (sb.length() > 0) {
                                result.add(new TextFlowRenderable(sb.toString(), new DefaultTextRenderStyle(font, color), hyperlink));
                                sb.setLength(0);
                            }
                            hyperlink = null;
                        } else {
                            throw new IllegalArgumentException("Unable to parse text - " + text);
                        }
                        reader.read();
                    } else if (nextChar == 'f') {
                        if (sb.length() > 0) {
                            result.add(new TextFlowRenderable(sb.toString(), new DefaultTextRenderStyle(font, color), hyperlink));
                            sb.setLength(0);
                        }
                        reader.read();
                        font = Assets.getFont(readUntilCharacter(reader, '>')).get();
                    } else if (nextChar == 'c') {
                        if (sb.length() > 0) {
                            result.add(new TextFlowRenderable(sb.toString(), new DefaultTextRenderStyle(font, color), hyperlink));
                            sb.setLength(0);
                        }
                        reader.read();
                        color = new Color(Integer.parseInt(readUntilCharacter(reader, '>'), 16));
                    } else if (nextChar == 'h') {
                        if (sb.length() > 0) {
                            result.add(new TextFlowRenderable(sb.toString(), new DefaultTextRenderStyle(font, color), hyperlink));
                            sb.setLength(0);
                        }
                        reader.read();
                        hyperlink = readUntilCharacter(reader, '>');
                    } else if (nextChar == 'l') {
                        readUntilCharacter(reader, '>');
                        sb.append('\n');
                    }
                } else if (c == '&') {
                    String escape = readUntilCharacter(reader, ';');
                    if (escape.equals("gt")) {
                        sb.append('>');
                    } else if (escape.equals("lt")) {
                        sb.append('<');
                    } else if (escape.equals("amp")) {
                        sb.append('&');
                    } else {
                        throw new IllegalArgumentException("Unknown escape sequence - " + escape);
                    }
                } else {
                    sb.append(c);
                }
            }

            if (sb.length() > 0) {
                result.add(new TextFlowRenderable(sb.toString(), new DefaultTextRenderStyle(font, color), hyperlink));
            }

            return result;
        } catch (IOException exp) {
            // Ignore - can't happen
        }

        return null;
    }

    // TODO: Quick and dirty - add something more solid and replaces uses of this one with it
    public static ParagraphData parseHTMLLikeParagraph(ParagraphRenderStyle paragraphRenderStyle, String text) {
        Collection<FlowRenderable> flowRenderables = parseHTMLLike(text);
        if (flowRenderables == null) {
            return null;
        }
        FlowParagraphData paragraphData = new FlowParagraphData(paragraphRenderStyle);
        flowRenderables.forEach(paragraphData::append);

        return paragraphData;
    }

    private static String readUntilCharacter(StringReader reader, char c) throws IOException {
        StringBuilder sb = new StringBuilder();
        char read;
        do {
            read = (char) reader.read();
            if (read != c) {
                sb.append(read);
            }
        } while (read != c);
        return sb.toString();
    }

    private static final class DefaultTextRenderStyle implements TextRenderStyle {
        private Font font;
        private Color color;

        private DefaultTextRenderStyle(Font font, Color color) {
            this.font = font;
            this.color = color;
        }

        @Override
        public Font getFont(boolean isHyperlink) {
            return font;
        }

        @Override
        public Color getColor(boolean isHyperlink) {
            return color;
        }
    }
}
