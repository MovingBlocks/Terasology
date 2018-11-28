package org.terasology.rendering.nui;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.widgets.ActivateEventListener;

import java.util.List;

public abstract class WidgetWithOrder extends CoreWidget {

    //TODO: call init of tabbingManagerSystem

    @LayoutConfig
    private int order = TabbingManagerSystem.UNINITIALIZED_DEPTH;

    /**
     * A {@link List} of listeners subscribed to this button
     */
    protected List<ActivateEventListener> listeners = Lists.newArrayList();

    private boolean added = false;

    public WidgetWithOrder() {
        this.setId("");
    }

    public WidgetWithOrder(String id) {
        this.setId(id);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        Logger logger = LoggerFactory.getLogger("widget w/ order");
        logger.info("adding");
        TabbingManagerSystem.addToWidgetsList(this);
    }
    public int getOrder() {
        if (order == TabbingManagerSystem.UNINITIALIZED_DEPTH) {
            order = TabbingManagerSystem.getNewNextNum();
        } else if (!added) {
            TabbingManagerSystem.addToUsedNums(order, this);
            added = true;
        }
        return order;
    }

    /**
     * Called when this is pressed to activate all subscribed listeners.
     */
    protected void activate() {
        for (ActivateEventListener listener : listeners) {
            listener.onActivated(this);
        }
    }
}
