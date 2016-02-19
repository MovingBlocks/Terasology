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
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class UIDropdownScrollable<T> extends UIDropdown<T> {
    private static final int SCROLL_MULTIPLIER = -42;

    private static final String LIST = "list";
    private static final String LIST_ITEM = "list-item";

    private UIScrollbar verticalBar = new UIScrollbar(true);

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

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            verticalBar.setValue(verticalBar.getValue() + event.getWheelTurns() * SCROLL_MULTIPLIER);
            return true;
        }
    };
    private List<InteractionListener> optionListeners = Lists.newArrayList();
    private ItemRenderer<T> optionRenderer = new ToStringTextRenderer<>();

    private boolean opened;

    public UIDropdownScrollable() {
    }

    public UIDropdownScrollable(String id) {
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

            // Limit number of options showed
            int optionsMaxNum = 5;
            int optionsSize = options.get().size()<=5 ? options.get().size() : optionsMaxNum;

            int height = (font.getLineHeight() + itemMargin.getTotalHeight()) * optionsSize + canvas.getCurrentStyle().getBackgroundBorder().getTotalHeight();
            canvas.addInteractionRegion(mainListener, Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y + height));

            Rect2i location = Rect2i.createFromMinAndSize(0, canvas.size().y, canvas.size().x, height);
            canvas.drawBackground(location);

            // Scrollbar Measurement
            int scrollbarWidth = canvas.calculateRestrictedSize(verticalBar, new Vector2i(canvas.size().x, canvas.size().y)).x;
            int scrollbarHeight = location.size().y-itemMargin.getTop();
            int availableWidth = location.size().x - scrollbarWidth;
            int availableHeight = scrollbarHeight;

            // Item
            int itemHeight = itemMargin.getTotalHeight() + font.getLineHeight();
            int itemWidth = availableWidth;

            canvas.setPart(LIST_ITEM);
            for (int i = 0; i < optionListeners.size(); ++i) {
                if (optionListeners.get(i).isMouseOver()) {
                    canvas.setMode(HOVER_MODE);
                } else {
                    canvas.setMode(DEFAULT_MODE);
                }

                Rect2i itemRegion = Rect2i.createFromMinAndSize(0, itemHeight * i-verticalBar.getValue(), itemWidth, itemHeight);


                // If outside location, then hide
                try (SubRegion ignored = canvas.subRegion(location, true)) {
                    canvas.drawBackground(itemRegion);
                    optionRenderer.draw(options.get().get(i), canvas, itemMargin.shrink(itemRegion));
                    canvas.addInteractionRegion(optionListeners.get(i), itemRegion);
                }

            }

            // Draw Scrollbar
            canvas.drawWidget(verticalBar, Rect2i.createFromMinAndSize(availableWidth-itemMargin.getRight(), itemMargin.getTotalHeight()*2 + font.getLineHeight(), scrollbarWidth, scrollbarHeight));


        } else {
            canvas.addInteractionRegion(mainListener);
        }
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
