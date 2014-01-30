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

import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author Immortius
 */
public abstract class AbstractWidget implements UIWidget {

    private String id;
    private Binding<String> family = new DefaultBinding<>();
    private boolean focused;

    private Binding<Boolean> visible = new DefaultBinding<>(true);
    private Binding<String> tooltip = new DefaultBinding<>("");
    private float tooltipDelay = 0.5f;

    public AbstractWidget() {
        id = "";
    }

    public AbstractWidget(String id) {
        this.id = id;
    }

    @Override
    public String getMode() {
        return DEFAULT_MODE;
    }

    @Override
    public final String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    @Override
    public final String getFamily() {
        return family.get();
    }

    @Override
    public final void setFamily(String family) {
        this.family.set(family);
    }

    @Override
    public void bindFamily(Binding<String> binding) {
        this.family = binding;
    }

    @Override
    public final <T extends UIWidget> T find(String targetId, Class<T> type) {
        if (this.id.equals(targetId)) {
            if (type.isInstance(this)) {
                return type.cast(this);
            }
            return null;
        }
        for (UIWidget contents : this) {
            if (contents != null) {
                T result = contents.find(targetId, type);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    public void bindVisible(Binding<Boolean> bind) {
        this.visible = bind;
    }

    @Override
    public void onGainFocus() {
        focused = true;
    }

    @Override
    public void onLoseFocus() {
        focused = false;
    }

    public final boolean isFocused() {
        return focused;
    }

    @Override
    public boolean isSkinAppliedByCanvas() {
        return true;
    }

    @Override
    public void update(float delta) {
        for (UIWidget item : this) {
            item.update(delta);
        }
    }

    @Override
    public boolean canBeFocus() {
        return true;
    }

    @Override
    public void bindTooltip(Binding<String> binding) {
        tooltip = binding;
    }

    @Override
    public String getTooltip() {
        return tooltip.get();
    }

    @Override
    public void setTooltip(String val) {
        tooltip.set(val);
    }

    @Override
    public float getTooltipDelay() {
        return tooltipDelay;
    }

    public final void setTooltipDelay(float value) {
        this.tooltipDelay = value;
    }
}
