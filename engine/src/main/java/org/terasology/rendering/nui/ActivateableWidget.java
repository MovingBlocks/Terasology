package org.terasology.rendering.nui;

import com.google.common.collect.Lists;
import org.terasology.rendering.nui.widgets.ActivateEventListener;

import java.util.List;

public abstract class ActivateableWidget extends WidgetWithOrder {

    public ActivateableWidget() {
        this.setId("");
    }

    public ActivateableWidget(String id) {
        this.setId(id);
    }
    /**
     * A {@link List} of listeners subscribed to this button
     */
    protected List<ActivateEventListener> listeners = Lists.newArrayList();

    /**
     * Called when this is pressed to activate all subscribed listeners.
     */
    protected void activate() {
        for (ActivateEventListener listener : listeners) {
            listener.onActivated(this);
        }
    }
}
