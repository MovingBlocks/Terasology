/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.widgets;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.utilities.Assets;

import java.util.ArrayList;
import java.util.List;

/**
 * One radial section of the Radial Ring
 */
public class UIRadialSection extends CoreWidget {

    private Rect2i infoRegion;
    private Rect2i innerRegion;
    private Rect2i sectionRegion;
    private TextureRegion sectionTexture = Assets.getTextureRegion("engine:radialUnit").get();
    private TextureRegion selectedTexture = Assets.getTextureRegion("engine:radialUnitSelected").get();
    private Boolean isSelected = false;
    private List<ActivateEventListener> listeners;

    @LayoutConfig
    private Binding<TextureRegion> icon = new DefaultBinding<>();
    @LayoutConfig
    private Binding<String> text = new DefaultBinding<>();
    @LayoutConfig
    private Binding<UIWidget> widget = new DefaultBinding<>();

    /**
     * Draws the widget
     *
     * @param canvas The canvas to draw on.
     */
    public void onDraw(Canvas canvas) {
        canvas.getRegion();
        canvas.drawTexture(sectionTexture, sectionRegion);

        if (icon.get() != null) {
            canvas.drawTexture(icon.get(), innerRegion);
        }

        if (text.get() != null) {
            canvas.drawText(text.get(), innerRegion);
        }
        if (isSelected) {
            canvas.drawTexture(selectedTexture, sectionRegion);
            if (widget.get() != null) {
                canvas.drawWidget(widget.get(), infoRegion);
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return sectionRegion == null ? Vector2i.zero() : sectionRegion.size();
    }

    /**
     * Add a listener to this section. It will be fired when the section is activated
     *
     * @param listener The listener to add
     */
    public void addListener(ActivateEventListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    /**
     * Removes a listener from the section.
     *
     * @param listener
     */
    public void removeListener(ActivateEventListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Activates the section, triggering all listeners.
     */
    public void activateSection() {
        if (listeners != null) {
            for (ActivateEventListener listener : listeners) {
                listener.onActivated(this);
            }
        }
    }

    /**
     * Sets the selected state of the section
     */
    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * Sets info widget
     */
    public void setInfoWidget(UIWidget infoWidget) {
        widget.set(infoWidget);
    }

    public void setInfoWidget(Binding<UIWidget> infoWidget) {
        widget = infoWidget;
    }

    /**
     * Set icon texture
     */
    public void setIcon(TextureRegion newIcon) {
        icon.set(newIcon);
    }

    public void setIcon(Binding<TextureRegion> newIcon) {
        icon = newIcon;
    }

    /**
     * Set section text
     */
    public void setText(String newText) {
        text.set(newText);
    }

    public void setText(Binding<String> newText) {
        text = newText;
    }

    /**
     * Sets the region in which to draw the info widget
     */
    public void setInfoRegion(Rect2i newRegion) {
        infoRegion = newRegion;
    }

    /**
     * Sets the draw region of the widget itself
     */
    public void setDrawRegion(Rect2i region) {
        sectionRegion = region;
    }

    /**
     * Sets the draw region of the items inside the widget.
     */
    public void setInnerRegion(Rect2i region) {
        innerRegion = region;
    }
}
