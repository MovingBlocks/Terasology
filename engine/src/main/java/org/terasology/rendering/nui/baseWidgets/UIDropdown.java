/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.baseWidgets;

import com.google.common.collect.Lists;
import org.terasology.input.MouseInput;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.assets.font.Font;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.displayAdapting.DisplayValueAdapter;
import org.terasology.rendering.nui.displayAdapting.ToStringAdapter;

import java.util.List;

/**
 * @author Immortius
 */
public class UIDropdown<T> extends AbstractWidget {
    private static final String BOX = "box";
    private static final String LIST = "list";
    private static final String LIST_ITEM = "list-item";
    private static final String ACTIVE_MODE = "active";
    private static final String HOVER_MODE = "hover";

    private Binding<List<T>> options = new DefaultBinding<List<T>>(Lists.<T>newArrayList());
    private Binding<T> selection = new DefaultBinding<>();
    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
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
    private DisplayValueAdapter<T> optionAdapter = new ToStringAdapter<>();

    private boolean opened;

    // TODO: Option -> display translation?
    // TODO: Selection changed events

    public UIDropdown() {
    }

    public UIDropdown(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart(BOX);
        canvas.drawBackground();
        if (selection.get() != null) {
            canvas.drawText(optionAdapter.convert(selection.get()));
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
            for (int i = 0; i < options.get().size(); ++i) {
                if (optionListeners.get(i).isMouseOver()) {
                    canvas.setMode(HOVER_MODE);
                } else {
                    canvas.setMode(DEFAULT_MODE);
                }
                Rect2i itemRegion = Rect2i.createFromMinAndSize(0, canvas.size().y + itemHeight * i, canvas.size().x, itemHeight);
                canvas.drawBackground(itemRegion);
                canvas.drawText(optionAdapter.convert(options.get().get(i)), itemRegion);
                canvas.addInteractionRegion(optionListeners.get(i), itemRegion);
            }
        } else {
            canvas.addInteractionRegion(mainListener);
        }
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

    public void setOptionAdapter(DisplayValueAdapter<T> displayValueAdapter) {
        optionAdapter = displayValueAdapter;
    }

    private class ItemListener extends BaseInteractionListener {
        private int index;

        public ItemListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            setSelection(getOptions().get(index));
            opened = false;
            return true;
        }
    }
}
