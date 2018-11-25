package org.terasology.rendering.nui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.events.NUIKeyEvent;

public abstract class WidgetWithOrder extends CoreWidget {

    //TODO: call init of tabbingManager

    @LayoutConfig
    private int order = TabbingManager.UNINITIALIZED_DEPTH;

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
        TabbingManager.addToWidgetsList(this);
    }
    public int getOrder() {
        if (order == TabbingManager.UNINITIALIZED_DEPTH) {
            order = TabbingManager.getNewNextNum();
        } else if (!added) {
            TabbingManager.addToUsedNums(order, this);
            added = true;
        }
        return order;
    }
    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        return true;
    }

    /*
    @Override
    public void onBindEvent(BindButtonEvent event) {
        if (event.getId().equals())
    }*/
}
