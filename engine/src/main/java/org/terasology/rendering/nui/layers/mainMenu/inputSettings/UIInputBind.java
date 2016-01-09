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

import org.terasology.audio.StaticSound;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.MouseInput;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;

import java.util.List;

/**
 */
public class UIInputBind extends CoreWidget {

    private boolean capturingInput;

    private Input newInput;
    private Binding<Input> input = new DefaultBinding<>();
    private Binding<StaticSound> clickSound = new DefaultBinding<>();
    private Binding<Float> clickVolume = new DefaultBinding<>(1.0f);

    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (event.isTopMostElement()) {
                focusManager.setFocus(UIInputBind.this);
            }
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
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
            if (newInput != null) {
                canvas.drawText(newInput.getDisplayName());
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
        } else if (newInput != null) {
            text = newInput.getDisplayName();
        }
        List<String> lines = TextLineBuilder.getLines(font, text, areaHint.getX());
        return font.getSize(lines);
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
        if (capturingInput && event.isDown()) {
            setNewInput(InputType.MOUSE_BUTTON.getInput(event.getButton().getId()));
            capturingInput = false;
            event.consume();
        }
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
        if (capturingInput) {
            MouseInput mouseInput = MouseInput.find(InputType.MOUSE_WHEEL, event.getWheelTurns());
            setNewInput(InputType.MOUSE_WHEEL.getInput(mouseInput.getId()));
            capturingInput = false;
            event.consume();
        }
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            if (capturingInput) {
                setNewInput(InputType.KEY.getInput(event.getKey().getId()));
                capturingInput = false;
                return true;
            }
        }
        return false;
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
        newInput = input.get();
    }

    public Input getInput() {
        return input.get();
    }

    public void setInput(Input val) {
        input.set(val);
    }

    public void bindClickSound(Binding<StaticSound> binding) {
        clickSound = binding;
    }

    public StaticSound getClickSound() {
        return clickSound.get();
    }

    public void setClickSound(StaticSound val) {
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

    public void setNewInput(Input newInput) {
        this.newInput = newInput;
    }

    public Input getNewInput() {
        return newInput;
    }

    public void saveInput() {
        setInput(newInput);
    }
}
