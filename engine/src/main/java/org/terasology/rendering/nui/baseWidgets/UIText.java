/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.baseWidgets;

import com.google.common.collect.Lists;
import org.lwjgl.input.Keyboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.input.MouseInput;
import org.terasology.input.events.KeyEvent;
import org.terasology.math.Border;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.TextureRegion;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

/**
 * @author Immortius
 */
public class UIText extends CoreWidget {

    private static final Logger logger = LoggerFactory.getLogger(UIText.class);

    private static final float BLINK_RATE = 0.25f;

    private float blinkCounter;

    private TextureRegion cursorTexture;
    private Binding<String> text = new DefaultBinding<>("");
    private boolean multiline;

    private int cursorPosition;
    private boolean selection;
    private int selectionStart;

    private Border lastMargin = Border.ZERO;
    private int lastWidth;
    private Font lastFont;

    private List<TextEventListener> listeners = Lists.newArrayList();

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                moveCursor(pos);
                return true;
            }
            return false;
        }
    };

    public UIText() {
        cursorTexture = Assets.getTexture("engine:white");
    }

    public UIText(String id) {
        super(id);
        cursorTexture = Assets.getTexture("engine:white");
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (text.get() == null) {
            return;
        }
        lastMargin = canvas.getCurrentStyle().getMargin();
        lastFont = canvas.getCurrentStyle().getFont();
        lastWidth = canvas.size().x - lastMargin.getTotalWidth();
        canvas.addInteractionRegion(interactionListener);
        canvas.drawText(text.get());
        if (isFocused()) {
            drawCursor(canvas);
        }
    }

    private void drawCursor(Canvas canvas) {
        if (blinkCounter < BLINK_RATE) {
            Border margin = canvas.getCurrentStyle().getMargin();
            Font font = canvas.getCurrentStyle().getFont();
            String beforeCursor = text.get();
            if (cursorPosition < text.get().length()) {
                beforeCursor = beforeCursor.substring(0, cursorPosition);
            }
            List<String> lines = TextLineBuilder.getLines(font, beforeCursor, canvas.size().x - margin.getTotalWidth());
            int lastLineWidth = font.getWidth(lines.get(lines.size() - 1));
            Rect2i region = Rect2i.createFromMinAndSize(lastLineWidth + margin.getLeft(), (lines.size() - 1) * font.getLineHeight() + margin.getTop(), 1, font.getLineHeight());
            canvas.drawTexture(cursorTexture, region, canvas.getCurrentStyle().getTextColor());
        }
    }

    @Override
    public Vector2i calcContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, text.get(), areaHint.x);
        return font.getSize(lines);
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        if (event.isDown() && lastFont != null) {
            String fullText = text.get();
            cursorPosition = TeraMath.clamp(cursorPosition, 0, fullText.length());
            switch (event.getKey()) {
                case Keyboard.KEY_BACK: {
                    if (cursorPosition > 0) {
                        String before = fullText.substring(0, cursorPosition - 1);
                        String after = fullText.substring(cursorPosition);
                        setText(before + after);
                        cursorPosition--;
                    }
                    break;
                }
                case Keyboard.KEY_DELETE: {
                    if (cursorPosition < fullText.length()) {
                        String before = fullText.substring(0, cursorPosition);
                        String after = fullText.substring(cursorPosition + 1);
                        setText(before + after);
                    }
                    break;
                }
                case Keyboard.KEY_LEFT: {
                    if (cursorPosition > 0) {
                        cursorPosition--;
                    }
                    break;
                }
                case Keyboard.KEY_RIGHT: {
                    if (cursorPosition < fullText.length()) {
                        cursorPosition++;
                    }
                    break;
                }
                case Keyboard.KEY_HOME: {
                    cursorPosition = 0;
                    break;
                }
                case Keyboard.KEY_END: {
                    cursorPosition = fullText.length();
                    break;
                }
                case Keyboard.KEY_RETURN: {
                    for (TextEventListener listener : listeners) {
                        listener.onEnterPressed(this);
                    }
                    break;
                }
                default: {
                    if (org.terasology.input.Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || org.terasology.input.Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                        if (event.getKey() == Keyboard.KEY_V) {
                            paste();
                            break;
                        }
                    }
                    if (event.getKeyCharacter() != 0 && lastFont.hasCharacter(event.getKeyCharacter())) {
                        String before = fullText.substring(0, cursorPosition);
                        String after = fullText.substring(cursorPosition);
                        setText(before + event.getKeyCharacter() + after);
                        cursorPosition++;
                    }
                    break;
                }
            }
        }
    }

    private void paste() {
        String fullText = getText();
        String before = fullText.substring(0, cursorPosition);
        String after = fullText.substring(cursorPosition);
        String pasted = getClipboardContents();
        setText(before + pasted + after);
        cursorPosition += pasted.length();

    }

    private String getClipboardContents() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) t.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (UnsupportedFlavorException | IOException e) {
            logger.warn("Failed to get data from clipboard", e);
        }

        return "";
    }

    private void moveCursor(Vector2i pos) {
        if (lastFont != null) {
            pos.x -= lastMargin.getLeft();
            pos.y -= lastMargin.getTop();
            List<String> lines = TextLineBuilder.getLines(lastFont, getText(), lastWidth);
            int lineIndex = TeraMath.clamp(pos.y / lastFont.getLineHeight(), 0, lines.size() - 1);
            int newCursorPos = 0;
            int totalWidth = 0;
            for (char c : lines.get(lineIndex).toCharArray()) {
                int charWidth = lastFont.getWidth(c);
                if (totalWidth + charWidth / 2 >= pos.x) {
                    break;
                }
                newCursorPos++;
                totalWidth += charWidth;
            }
            for (int i = 0; i < lineIndex; ++i) {
                newCursorPos += lines.get(i).length() + 1;
            }
            cursorPosition = newCursorPos;
        }
    }

    public void bindText(Binding<String> binding) {
        text = binding;
    }

    public String getText() {
        return text.get();
    }

    public void setText(String val) {
        text.set(val);
    }

    public boolean isMultiline() {
        return multiline;
    }

    public void setMultiline(boolean multiline) {
        this.multiline = multiline;
    }

    public void subscribe(TextEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(TextEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        blinkCounter += delta;
        while (blinkCounter > 2 * BLINK_RATE) {
            blinkCounter -= 2 * BLINK_RATE;
        }
    }
}
