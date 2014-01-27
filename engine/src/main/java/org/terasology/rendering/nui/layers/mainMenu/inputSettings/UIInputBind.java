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
package org.terasology.rendering.nui.layers.mainMenu.inputSettings;

import org.terasology.audio.Sound;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.util.List;

/**
 * @author Immortius
 */
public class UIInputBind extends CoreWidget {
    public static final String HOVER_MODE = "hover";
    public static final String ACTIVE_MODE = "active";

    private boolean capturingInput;

    private Binding<Input> input = new DefaultBinding<>();
    private Binding<Sound> clickSound = new DefaultBinding<>();
    private Binding<Float> clickVolume = new DefaultBinding<>(1.0f);

    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public void onMouseOver(Vector2i pos, boolean topMostElement) {
            super.onMouseOver(pos, topMostElement);
            if (topMostElement) {
                focusManager.setFocus(UIInputBind.this);
            }
        }

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                if (getClickSound() != null) {
                    getClickSound().play(getClickVolume());
                }
                capturingInput = true;
                return true;
            }
            return false;
        }
    };

    public UIInputBind() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!capturingInput) {
            if (input.get() != null) {
                canvas.drawText(input.get().getDisplayName());
            }
        } else {
            canvas.drawText("???");
        }
        canvas.addInteractionRegion(interactionListener);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        String text = "";
        if (capturingInput) {
            text = "???";
        } else if (input.get() != null) {
            text = input.get().getDisplayName();
        }
        List<String> lines = TextLineBuilder.getLines(font, text, areaHint.getX());
        Vector2i size = font.getSize(lines);
        return size;
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
        if (capturingInput && event.isDown()) {
            setInput(InputType.MOUSE_BUTTON.getInput(event.getButton().getId()));
            capturingInput = false;
            event.consume();
        }
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
        if (capturingInput) {
            MouseInput mouseInput = MouseInput.find(InputType.MOUSE_WHEEL, event.getWheelTurns());
            setInput(InputType.MOUSE_WHEEL.getInput(mouseInput.getId()));
            capturingInput = false;
            event.consume();
        }
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        if (event.isDown()) {
            if (capturingInput) {
                setInput(InputType.KEY.getInput(event.getKey().getId()));
                capturingInput = false;
                event.consume();
            } else if (event.getKey() == Keyboard.Key.DELETE || event.getKey() == Keyboard.Key.BACKSPACE) {
                setInput(null);
                event.consume();
            }
        }
    }

    @Override
    public void update(float delta) {


    }

    @Override
    public String getMode() {
        if (capturingInput) {
            return ACTIVE_MODE;
        } else if (interactionListener.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    public void bindInput(Binding<Input> binding) {
        input = binding;
    }

    public Input getInput() {
        return input.get();
    }

    public void setInput(Input val) {
        input.set(val);
    }

    public void bindClickSound(Binding<Sound> binding) {
        clickSound = binding;
    }

    public Sound getClickSound() {
        return clickSound.get();
    }

    public void setClickSound(Sound val) {
        clickSound.set(val);
    }

    public void bindClickVolume(Binding<Float> binding) {
        clickVolume = binding;
    }

    public float getClickVolume() {
        return clickVolume.get();
    }

    public void setClickVolume(float val) {
        clickVolume.set(val);
    }
}
