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

import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.UILabel;

import java.util.Collection;
import java.util.List;

/**
 */
public abstract class AbstractWidget implements UIWidget {

    @LayoutConfig
    private String id;

    @LayoutConfig
    private UISkin skin;

    @LayoutConfig
    private Binding<String> family = new DefaultBinding<>();

    @LayoutConfig
    private Binding<Boolean> visible = new DefaultBinding<>(true);

    @LayoutConfig
    private Binding<UIWidget> tooltip = new DefaultBinding<>();

    @LayoutConfig
    private float tooltipDelay = 0.5f;

    private boolean focused;

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
    public final UISkin getSkin() {
        return skin;
    }

    @Override
    public final void setSkin(UISkin skin) {
        this.skin = skin;
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

    @Override
    public final <T extends UIWidget> Collection<T> findAll(Class<T> type) {
        List<T> results = Lists.newArrayList();
        findAll(type, this, results);
        return results;
    }

    private <T extends UIWidget> void findAll(Class<T> type, UIWidget widget, List<T> results) {
        if (type.isInstance(widget)) {
            results.add(type.cast(widget));
        }
        for (UIWidget content : widget) {
            findAll(type, content, results);
        }
    }

    @Override
    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    public void bindVisible(Binding<Boolean> bind) {
        this.visible = bind;
    }

    public void clearVisibleBinding() {
        this.visible = new DefaultBinding<>(true);
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
    public void bindTooltip(Binding<UIWidget> binding) {
        tooltip = binding;
    }

    @Override
    public UIWidget getTooltip() {
        return tooltip.get();
    }

    @Override
    public void setTooltip(UIWidget val) {
        tooltip.set(val);
    }

    @Override
    public void bindTooltipString(Binding<String> bind) {
        bindTooltip(new TooltipLabelBinding(bind));
    }

    @Override
    public void setTooltip(String value) {
        if (value != null && !value.isEmpty()) {
            setTooltip(new UILabel(value));
        } else {
            tooltip = new DefaultBinding<>(null);
        }
    }

    @Override
    public float getTooltipDelay() {
        return tooltipDelay;
    }

    public final void setTooltipDelay(float value) {
        this.tooltipDelay = value;
    }

    private static class TooltipLabelBinding extends ReadOnlyBinding<UIWidget> {

        private UILabel tooltipLabel = new UILabel();

        public TooltipLabelBinding(Binding<String> stringBind) {
            tooltipLabel.bindText(stringBind);
        }

        @Override
        public UIWidget get() {
            if (tooltipLabel.getText().isEmpty()) {
                return null;
            }
            return tooltipLabel;
        }
    }
}
