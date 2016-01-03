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
import org.terasology.math.Border;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class UIDropdown<T> extends CoreWidget {
    private static final String LIST = "list";
    private static final String LIST_ITEM = "list-item";

    private Binding<List<T>> options = new DefaultBinding<>(new ArrayList<>());
    private Binding<T> selection = new DefaultBinding<>();
    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            opened = !opened;
            optionListeners.clear();
            if (opened) {
                for (int i = 0; i < getOptions().size(); ++i) {
                    optionListeners.add(new ItemListener(i));
                }
            }
            return true;
        }
    };
    private List<InteractionListener> optionListeners = Lists.newArrayList();
    private ItemRenderer<T> optionRenderer = new ToStringTextRenderer<>();

    private boolean opened;

    public UIDropdown() {
    }

    public UIDropdown(String id) {
        super(id);
    }

    @Override
    public boolean isSkinAppliedByCanvas() {
        return false;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBackground();
        try (SubRegion ignored = canvas.subRegion(canvas.getCurrentStyle().getMargin().shrink(canvas.getRegion()), false)) {
            if (selection.get() != null) {
                optionRenderer.draw(selection.get(), canvas);
            }
        }

        if (opened) {
            canvas.setPart(LIST);
            canvas.setDrawOnTop(true);
            Font font = canvas.getCurrentStyle().getFont();
            Border itemMargin = canvas.getCurrentStyle().getMargin();
            int height = (font.getLineHeight() + itemMargin.getTotalHeight()) * options.get().size() + canvas.getCurrentStyle().getBackgroundBorder().getTotalHeight();
            canvas.addInteractionRegion(mainListener, Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y + height));

            Rect2i location = Rect2i.createFromMinAndSize(0, canvas.size().y, canvas.size().x, height);
            canvas.drawBackground(location);

            int itemHeight = itemMargin.getTotalHeight() + font.getLineHeight();
            canvas.setPart(LIST_ITEM);
            for (int i = 0; i < optionListeners.size(); ++i) {
                if (optionListeners.get(i).isMouseOver()) {
                    canvas.setMode(HOVER_MODE);
                } else {
                    canvas.setMode(DEFAULT_MODE);
                }
                Rect2i itemRegion = Rect2i.createFromMinAndSize(0, canvas.size().y + itemHeight * i, canvas.size().x, itemHeight);
                canvas.drawBackground(itemRegion);
                optionRenderer.draw(options.get().get(i), canvas, itemMargin.shrink(itemRegion));
                canvas.addInteractionRegion(optionListeners.get(i), itemRegion);
            }
        } else {
            canvas.addInteractionRegion(mainListener);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        if (selection.get() != null) {
            return canvas.getCurrentStyle().getMargin().grow(optionRenderer.getPreferredSize(selection.get(), canvas));
        }
        return canvas.getCurrentStyle().getMargin().grow(new Vector2i(1, canvas.getCurrentStyle().getFont().getLineHeight()));
    }

    @Override
    public String getMode() {
        if (opened) {
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();
        opened = false;
    }

    public void bindOptions(Binding<List<T>> binding) {
        options = binding;
    }

    public List<T> getOptions() {
        return options.get();
    }

    public void setOptions(List<T> values) {
        this.options.set(values);
    }

    public void bindSelection(Binding<T> binding) {
        this.selection = binding;
    }

    public T getSelection() {
        return selection.get();
    }

    public void setSelection(T value) {
        selection.set(value);
    }

    public void setOptionRenderer(ItemRenderer<T> itemRenderer) {
        optionRenderer = itemRenderer;
    }

    private class ItemListener extends BaseInteractionListener {
        private int index;

        public ItemListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            setSelection(getOptions().get(index));
            opened = false;
            return true;
        }
    }
}
