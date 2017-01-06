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

import java.util.ArrayList;
import java.util.List;

public class UIRadialSection extends CoreWidget {

    private Rect2i drawRegion;
    private Rect2i infoRegion;
    private Rect2i sectionRegion;
    private TextureRegion sectionTexture;
    private TextureRegion selectedTexture;
    private Boolean isSelected = false;
    private List<ActivateEventListener> listeners;

    @LayoutConfig
    private TextureRegion icon;
    @LayoutConfig
    private String text = "";
    @LayoutConfig
    private UIWidget info;

    public void onDraw(Canvas canvas) {
        canvas.getRegion();
        if (icon != null) {
            canvas.drawTexture(icon, sectionRegion);
        }
        canvas.drawTexture(sectionTexture, sectionRegion);
        if (text != null) {
            canvas.drawText(text, sectionRegion);
        }
        if (isSelected) {
            canvas.drawTexture(selectedTexture,
                    canvas.getRegion());
            canvas.drawWidget(info, infoRegion);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return drawRegion == null ? Vector2i.zero() : drawRegion.size();
    }

    public void addListener(ActivateEventListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void activateSection() {
        if (listeners != null) {
            for (ActivateEventListener listener : listeners) {
                listener.onActivated(this);
            }
        }
    }

    public void removeListener(ActivateEventListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void setSectionTexture(TextureRegion newTexture) {
        sectionTexture = newTexture;
    }

    public void setSelectedTexture(TextureRegion newTexture) {
        selectedTexture = newTexture;
    }

    public void setDrawRegion(Rect2i newRegion) {
        drawRegion = newRegion;
    }

    public void setInfoRegion(Rect2i newRegion) {
        infoRegion = newRegion;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean getIsSubmenu() {
        return info instanceof UIRadialSection;
    }

    public void setCenter(Rect2i region) {
        sectionRegion = region;
    }
}
