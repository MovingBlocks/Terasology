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
package org.terasology.rendering.nui;

import com.google.common.collect.Lists;
import org.terasology.engine.SimpleUri;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.ButtonState;
import org.terasology.input.MouseInput;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.skin.UISkin;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UIRadialSection;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    protected int depth = new DefaultBinding<Integer>(SortOrderSystem.DEFAULT_DEPTH).get();

    private boolean focused;

    private static boolean shiftPressed;

    @LayoutConfig
    private Binding<Boolean> enabled = new DefaultBinding<>(true);

    public AbstractWidget() {
        id = "";
    }

    public AbstractWidget(String id) {
        this.id = id;
    }

    @Override
    public String getMode() {
        if (this.isEnabled()) {
            return DEFAULT_MODE;
        }
        return DISABLED_MODE;
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
    public <T extends UIWidget> Optional<T> tryFind(String id, Class<T> type) {
        return Optional.ofNullable(find(id, type));
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

    public boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);

        for (UIWidget child : this) {
            if (child instanceof AbstractWidget) {
                AbstractWidget widget = (AbstractWidget) child;
                widget.setEnabled(this.isEnabled());
            }
        }

    }

    public void bindEnabled(Binding<Boolean> binding) {
        enabled = binding;

        for (UIWidget child : this) {
            if (child instanceof AbstractWidget) {
                AbstractWidget widget = (AbstractWidget) child;
                widget.bindEnabled(binding);
            }
        }
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
        this.onMouseButtonEvent(new MouseButtonEvent(MouseInput.MOUSE_LEFT, ButtonState.UP, 0));
    }

    @Override
    public void onLoseFocus() {
        focused = false;

        if (TabbingManager.focusedWidget != null && TabbingManager.focusedWidget.equals(this)) {
            TabbingManager.unfocusWidget();
        }
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
    public void setTooltip(String value) {
        if (value != null && !value.isEmpty()) {
            setTooltip(new UILabel(value));
        } else {
            tooltip = new DefaultBinding<>(null);
        }
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
    public float getTooltipDelay() {
        return tooltipDelay;
    }

    public final void setTooltipDelay(float value) {
        this.tooltipDelay = value;
    }

    private static class TooltipLabelBinding extends ReadOnlyBinding<UIWidget> {

        private UILabel tooltipLabel = new UILabel();

        TooltipLabelBinding(Binding<String> stringBind) {
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

    @Override
    public void onBindEvent(BindButtonEvent event) {
        if (event.getState().equals(ButtonState.DOWN) && !SortOrderSystem.containsConsole()) {

            if (event.getId().equals(new SimpleUri("engine:tabbingModifier"))) {
                shiftPressed = true;
            }

            if (event.getId().equals(new SimpleUri("engine:tabbingUI"))) {
                if (!TabbingManager.isInitialized()) {
                    TabbingManager.init();
                }
                if (TabbingManager.getOpenScreen().getManager().getFocus() == null) {
                    if (TabbingManager.getWidgetsList().size() > 0) {
                        TabbingManager.resetCurrentNum();
                        TabbingManager.focusedWidget = TabbingManager.getWidgetsList().get(0);
                    }
                }
                TabbingManager.focusSetThrough = true;
                TabbingManager.changeCurrentNum(!shiftPressed);

                for (WidgetWithOrder widget : TabbingManager.getWidgetsList()) {
                    if (widget.getOrder() == TabbingManager.getCurrentNum()) {
                        if (!widget.isEnabled()) {
                            TabbingManager.changeCurrentNum(true);
                        } else {
                            widget.onGainFocus();
                            TabbingManager.focusedWidget = widget;
                            TabbingManager.getOpenScreen().getManager().setFocus(widget);
                        }
                    } else {
                        widget.onLoseFocus();

                        if (widget instanceof UIRadialSection) {
                            ((UIRadialSection) widget).setSelected(false);
                        }
                    }
                }

                event.prepare(new SimpleUri("engine:tabbingUI"), ButtonState.UP, event.getDelta());
            } else if (event.getId().equals(new SimpleUri("engine:activate"))) {
                if (TabbingManager.focusedWidget instanceof UIDropdown) {
                    UIDropdown dropdown = ((UIDropdown) TabbingManager.focusedWidget);
                    if (dropdown.isOpened()) {
                        dropdown.setOpenedReverse(true);
                    }
                } else if  (TabbingManager.focusedWidget instanceof ActivatableWidget) {
                    ((ActivatableWidget) TabbingManager.focusedWidget).activateWidget();
                }

                event.prepare(new SimpleUri("engine:activate"), ButtonState.UP, event.getDelta());
            }
        }
        if (event.getState().equals(ButtonState.UP) && !SortOrderSystem.containsConsole()) {

            if (event.getId().equals(new SimpleUri("engine:tabbingModifier"))) {
                shiftPressed = false;
            }
        }
    }

    public static boolean getShiftPressed() {
        return shiftPressed;
    }
}
