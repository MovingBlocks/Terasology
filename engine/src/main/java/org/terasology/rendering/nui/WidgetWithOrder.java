package org.terasology.rendering.nui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WidgetWithOrder extends CoreWidget {

    //TODO: call init of tabbingManagerSystem

    @LayoutConfig
    protected int order = TabbingManagerSystem.UNINITIALIZED_DEPTH;

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
}
