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

import org.terasology.asset.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.audio.StaticSound;
import org.terasology.input.Input;
import org.terasology.input.MouseInput;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.*;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;

import java.util.List;

public class UIInputBindButton extends CoreWidget {
    public static final String HOVER_MODE = "hover";

    private static final ResourceUrn INPUT_CHANGING_SCREEN_URI = new ResourceUrn("engine:inputChangingScreen");

    private NUIManager manager;
    InputChangingScreen inputChangingScreen = new InputChangingScreen();

    private Binding<Input> input = new DefaultBinding<>();
    private Binding<StaticSound> clickSound = new DefaultBinding<>();
    private Binding<Float> clickVolume = new DefaultBinding<>(1.0f);

    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (event.isTopMostElement()) {
                focusManager.setFocus(UIInputBindButton.this);
            }
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                if (getClickSound() != null) {
                    getClickSound().play(getClickVolume());
                }
                inputChangingScreen.setSkin(getManager().getScreen("engine:inputScreen").getSkin());
                UIData inputScreenData = new UIData(inputChangingScreen);
                Assets.generateAsset(INPUT_CHANGING_SCREEN_URI, inputScreenData, UIElement.class);
                UIInputBindButton.this.getManager().pushScreen(UIInputBindButton.INPUT_CHANGING_SCREEN_URI);
                return true;
            }
            return false;
        }
    };

    public UIInputBindButton() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (input.get() != null) {
            canvas.drawText(input.get().getDisplayName());
        }
        canvas.addInteractionRegion(interactionListener);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        String text = "";
        if (input.get() != null) {
            text = input.get().getDisplayName();
        }
        List<String> lines = TextLineBuilder.getLines(font, text, areaHint.getX());
        return font.getSize(lines);
    }

    @Override
    public void update(float delta) {


    }

    @Override
    public String getMode() {
       if (interactionListener.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    public void bindInput(InputConfigBinding binding) {
        input = binding;
        inputChangingScreen.bindInput(binding);
    }

    public Input getInput() {
        return input.get();
    }

    public void setInput(Input val) {
        input.set(val);
    }

    public StaticSound getClickSound() {
        return clickSound.get();
    }

    public float getClickVolume() {
        return clickVolume.get();
    }

    public NUIManager getManager() {
        return manager;
    }

    public void setManager(NUIManager manager) {
        this.manager = manager;
    }

    public void setDescription(String description) {
        inputChangingScreen.setDescription(description);
    }

    public String getDescription() {
        return inputChangingScreen.getDescription();
    }
}
