/*
 * Copyright 2016 MovingBlocks
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
    private static final String LIST = "list";
    private static final String LIST_ITEM = "list-item";

    private UIScrollbar verticalBar = new UIScrollbar(true);
    private int visibleOptionsNum = 5;

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
            int scrollMultiplier = 0 - verticalBar.getRange() / getOptions().size();
            verticalBar.setValue(verticalBar.getValue() + event.getWheelTurns() * scrollMultiplier);
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

            // Limit number of visible options
            float optionsSize = options.get().size() <= visibleOptionsNum ? options.get().size() : (visibleOptionsNum + 0.5f);

            // Calculate total options height
            int itemHeight = itemMargin.getTotalHeight() + font.getLineHeight();
            int height = (int) (itemHeight * optionsSize + canvas.getCurrentStyle().getBackgroundBorder().getTotalHeight());
            canvas.addInteractionRegion(mainListener, Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y + height));

            // Dropdown Background Frame
            Rect2i frame = Rect2i.createFromMinAndSize(0, canvas.size().y, canvas.size().x, height);
            canvas.drawBackground(frame);
            canvas.setPart(LIST_ITEM);

            if (options.get().size() > visibleOptionsNum) {
                createScrollbarItems(canvas, frame, font, itemMargin, height, itemHeight);
            } else {
                createNoScrollItems(canvas, itemMargin, itemHeight);
            }

        } else {
            canvas.addInteractionRegion(mainListener);
        }
    }

    /**
     * Located in the onDraw method, this draws the menu items when the scrollbar is unnecessary.
     * @param canvas {@link Canvas} from the onDraw method.
     * @param itemMargin Margin around every menu item.
     * @param itemHeight Height per menu item.
     */
    private void createNoScrollItems(Canvas canvas, Border itemMargin, int itemHeight) {
        for (int i = 0; i < optionListeners.size(); ++i) {
            readItemMouseOver(canvas, i);
            Rect2i itemRegion = Rect2i.createFromMinAndSize(0, canvas.size().y + itemHeight * i, canvas.size().x, itemHeight);
            drawItem(canvas, itemMargin, i, itemRegion);
        }
    }

    /**
     * Located in the onDraw method, this draws the menu items with a scrollbar.
     * @param canvas {@link Canvas} from the onDraw method.
     * @param frame Menu frame.
     * @param font {@link Font} used in the menu.
     * @param itemMargin Margin around every menu item.
     * @param height Total menu height.
     * @param itemHeight Height per menu item.
     */
    private void createScrollbarItems(Canvas canvas, Rect2i frame, Font font, Border itemMargin, int height, int itemHeight) {
        // Scrollable Area
        Rect2i scrollableArea = Rect2i.createFromMinAndSize(0, canvas.size().y, canvas.size().x, height - itemMargin.getBottom());

        // Scrollbar Measurement
        int scrollbarWidth = canvas.calculateRestrictedSize(verticalBar, new Vector2i(canvas.size().x, canvas.size().y)).x;
        int scrollbarHeight = frame.size().y - itemMargin.getTop();
        int availableWidth = frame.size().x - scrollbarWidth;
        int scrollbarXPos = availableWidth - itemMargin.getRight();
        int scrollbarYPos = itemMargin.getTotalHeight() * 2 + font.getLineHeight();

        // Draw Scrollbar
        Rect2i scrollbarRegion = Rect2i.createFromMinAndSize(scrollbarXPos, scrollbarYPos, scrollbarWidth, scrollbarHeight);
        canvas.drawWidget(verticalBar, scrollbarRegion);

        // Set the range of Scrollbar
        float maxVertBarDesired = itemHeight * (optionListeners.size() - visibleOptionsNum - 0.5f) + itemMargin.getBottom();
        verticalBar.setRange((int)maxVertBarDesired);

        for (int i = 0; i < optionListeners.size(); ++i) {
            readItemMouseOver(canvas, i);
            Rect2i itemRegion = Rect2i.createFromMinAndSize(0, itemHeight * i - verticalBar.getValue(), availableWidth, itemHeight);

            // If outside location, then hide
            try (SubRegion ignored = canvas.subRegion(scrollableArea, true)) {
                drawItem(canvas, itemMargin, i, itemRegion);
            }
        }
    }

    /**
     * Looks for MouseOver event for every item in the menu.
     * @param canvas {@link Canvas} from the onDraw method.
     * @param i Item index.
     */
    private void readItemMouseOver(Canvas canvas, int i) {
        if (optionListeners.get(i).isMouseOver()) {
            canvas.setMode(HOVER_MODE);
        } else {
            canvas.setMode(DEFAULT_MODE);
        }
    }

    /**
     * Draws the item on the {@link Canvas}.
     * @param canvas {@link Canvas} from the onDraw method.
     * @param itemMargin Margin around every menu item.
     * @param i Item index.
     * @param itemRegion Region of the item in the menu.
     */
    private void drawItem(Canvas canvas, Border itemMargin, int i, Rect2i itemRegion) {
        canvas.drawBackground(itemRegion);
        optionRenderer.draw(options.get().get(i), canvas, itemMargin.shrink(itemRegion));
        canvas.addInteractionRegion(optionListeners.get(i), itemRegion);
    }

    @Override
    public void onLoseFocus() {
        super.onLoseFocus();

        String mode = verticalBar.getMode();
        if (!mode.equals("active")) {
            opened = false;
            super.onGainFocus();
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

    public void setVisibleOptions(int num) {
        visibleOptionsNum = num;
    }

    public int getVisibleOptions() {
        return visibleOptionsNum;
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
