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
package org.terasology.rendering.nui.widgets;

import com.google.common.collect.Lists;
import org.terasology.utilities.Assets;
import org.terasology.audio.StaticSound;
import org.terasology.input.MouseInput;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.TextLineBuilder;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

import java.util.List;

/**
 */
public class UIButton extends CoreWidget {
    public static final String DOWN_MODE = "down";

    @LayoutConfig
    private Binding<TextureRegion> image = new DefaultBinding<>();

    @LayoutConfig
    private Binding<String> text = new DefaultBinding<>("");

    @LayoutConfig
    private Binding<StaticSound> clickSound = new DefaultBinding<>(Assets.getSound("engine:click").get());

    @LayoutConfig
    private Binding<Float> clickVolume = new DefaultBinding<>(1.0f);

    private boolean down;

    private List<ActivateEventListener> listeners = Lists.newArrayList();

    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                down = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                if (isMouseOver()) {
                    if (getClickSound() != null) {
                        getClickSound().play(getClickVolume());
                    }
                    activate();
                }
                down = false;
            }
        }
    };

    public UIButton() {
    }

    public UIButton(String id) {
        super(id);
    }

    public UIButton(String id, String text) {
        super(id);
        this.text.set(text);
    }

    public UIButton(String id, Binding<String> text) {
        super(id);
        this.text = text;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (image.get() != null) {
            canvas.drawTexture(image.get());
        }
        canvas.drawText(text.get());
        if (isEnabled()) {
            canvas.addInteractionRegion(interactionListener);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, text.get(), areaHint.getX());
        return font.getSize(lines);
    }

    @Override
    public String getMode() {
        if (!isEnabled()) {
            return DISABLED_MODE;
        } else if (down) {
            return DOWN_MODE;
        } else if (interactionListener.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    private void activate() {
        for (ActivateEventListener listener : listeners) {
            listener.onActivated(this);
        }
    }

    public void bindText(Binding<String> binding) {
        this.text = binding;
    }

    /**
     * Get the text, if any, displayed on the button.
     *
     * @return The String on the button. In the case of no text the String is empty.
     */
    public String getText() {
        return text.get();
    }

    /**
     * Set the text on the button.
     *
     * @param text The String to display on the button.
     */
    public void setText(String text) {
        this.text.set(text);
    }

    public void bindImage(Binding<TextureRegion> binding) {
        this.image = binding;
    }

    /**
     * Set an image to display on the button.
     *
     * @param image A TextureRegion to set as the button's image.
     */
    public void setImage(TextureRegion image) {
        this.image.set(image);
    }

    /**
     * Get the image currently shown on the button.
     *
     * @return The image shown on the Button in a TextureRegion.
     */
    public TextureRegion getImage() {
        return image.get();
    }

    public void bindClickSound(Binding<StaticSound> binding) {
        clickSound = binding;
    }

    /**
     * Get the sound that is played when the button is clicked.
     *
     * @return The StaticSound that is played.
     */
    public StaticSound getClickSound() {
        return clickSound.get();
    }

    /**
     * Set the sound to be played when the button is clicked.
     *
     * @param val The StaticSound that should be played.
     */
    public void setClickSound(StaticSound val) {
        clickSound.set(val);
    }

    public void bindClickVolume(Binding<Float> binding) {
        clickVolume = binding;
    }

    /**
     * Get the volume the click sound is played at.
     *
     * @return A float indicating how load the sound is.
     */
    public float getClickVolume() {
        return clickVolume.get();
    }

    /**
     * Set how loud the click sound should be played.
     *
     * @param val A Float that indicates how load the sound should be.
     */
    public void setClickVolume(float val) {
        clickVolume.set(val);
    }

    /**
     * Add a listener to be called when the button is clicked.
     *
     * @param listener The listener to be called.
     */
    public void subscribe(ActivateEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the button such that it will no longer be called on click.
     *
     * @param listener The listener to remove.
     */
    public void unsubscribe(ActivateEventListener listener) {
        listeners.remove(listener);
    }
}
