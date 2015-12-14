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
package org.terasology.rendering.nui;

import com.google.common.collect.Lists;
import org.terasology.rendering.assets.font.Font;

import java.util.Arrays;
import java.util.List;

/**
 */
public class TextLineBuilder {

    private final Font font;
    private final int spaceWidth;
    private final int maxWidth;
    private List<String> lines = Lists.newArrayList();

    private int currentLineLength;
    private StringBuilder lineBuilder = new StringBuilder();

    private boolean lineHasWord;

    public TextLineBuilder(Font font, int maxWidth, int startOffset) {
        this.font = font;
        this.spaceWidth = font.getWidth(' ');
        this.maxWidth = maxWidth;
        this.currentLineLength = startOffset;
    }

    public static List<String> getLines(Font font, String text, int maxWidth) {
        return getLines(font, text, maxWidth, 0);
    }

    public static List<String> getLines(Font font, String text, int maxWidth, int startOffset) {
        TextLineBuilder textLineBuilder = new TextLineBuilder(font, maxWidth, startOffset);
        textLineBuilder.addText(text);
        return textLineBuilder.getLines();
    }

    public void addText(String text) {
        List<String> paragraphs = Arrays.asList(text.split("\\r?\\n", -1));
        for (String paragraph : paragraphs) {
            String remainder = paragraph;
            while (remainder != null && !remainder.isEmpty()) {
                String[] split = remainder.split(" ", 2);
                String word = split[0];
                if (split.length > 1) {
                    remainder = split[1];
                } else {
                    remainder = null;
                }

                addWord(word);
            }
            if (remainder != null) {
                addWord(remainder);
            }
            endLine();
        }
    }

    public void addWord(String word) {
        int wordWidth = font.getWidth(word);
        if (wordWidth > maxWidth) {
            if (currentLineLength > 0) {
                endLine();
            }
            for (char c : word.toCharArray()) {
                int charWidth = font.getWidth(c);
                if (currentLineLength + charWidth > maxWidth) {
                    endLine();
                }
                lineBuilder.append(c);
                currentLineLength += charWidth;
                lineHasWord = true;
            }
        } else {
            if (currentLineLength > 0 && currentLineLength + spaceWidth + wordWidth > maxWidth) {
                endLine();
            }
            if (lineHasWord) {
                lineBuilder.append(' ');
                currentLineLength += spaceWidth;
            }
            lineBuilder.append(word);
            currentLineLength += wordWidth;
            lineHasWord = true;
        }
    }

    public List<String> getLines() {
        return lines;
    }

    private void endLine() {
        currentLineLength = 0;
        lines.add(lineBuilder.toString());
        lineBuilder.setLength(0);
        lineHasWord = false;
    }
}
