package org.terasology.rendering.nui;

import com.google.common.collect.Lists;

import java.util.List;

public abstract class ListableWidget extends WidgetWithOrder {

    public ListableWidget() {
        this.setId("");
    }

    public ListableWidget(String id) {
        this.setId(id);
    }

    protected List<InteractionListener> optionListeners = Lists.newArrayList();

    protected int highlightedIndex;

    public void changeHighlighted(boolean increase) {
        if (increase) {
            highlightedIndex++;
            if (highlightedIndex>=optionListeners.size()) {
                highlightedIndex = 0;
            }
        } else {
            highlightedIndex--;
            if (highlightedIndex<0) {
                highlightedIndex = optionListeners.size()-1;
            }
        }
    }
}
