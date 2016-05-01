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
    public static final String DISABLED_MODE = "disabled";

    @LayoutConfig
    private Binding<TextureRegion> image = new DefaultBinding<>();

    @LayoutConfig
    private Binding<String> text = new DefaultBinding<>("");

    @LayoutConfig
    private Binding<StaticSound> clickSound = new DefaultBinding<>(Assets.getSound("engine:click").get());

    @LayoutConfig
    private Binding<Float> clickVolume = new DefaultBinding<>(1.0f);

    @LayoutConfig
    private Binding<Boolean> enabled = new DefaultBinding<>(Boolean.TRUE);

    private boolean down;

    private List<ActivateEventListener> listeners = Lists.newArrayList();

    private InteractionListener interactionListener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (enabled.get() && event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                down = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            if (enabled.get() && event.getMouseButton() == MouseInput.MOUSE_LEFT) {
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
        canvas.addInteractionRegion(interactionListener);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Font font = canvas.getCurrentStyle().getFont();
        List<String> lines = TextLineBuilder.getLines(font, text.get(), areaHint.getX());
        return font.getSize(lines);
    }

    @Override
    public String getMode() {
        if (!enabled.get()) {
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

    public String getText() {
        return text.get();
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public void bindImage(Binding<TextureRegion> binding) {
        this.image = binding;
    }

    public void setImage(TextureRegion image) {
        this.image.set(image);
    }

    public TextureRegion getImage() {
        return image.get();
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

    public void bindEnabled(Binding<Boolean> binding) {
        enabled = binding;
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public void subscribe(ActivateEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(ActivateEventListener listener) {
        listeners.remove(listener);
    }
}
