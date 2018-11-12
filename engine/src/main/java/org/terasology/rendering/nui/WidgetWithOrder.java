package org.terasology.rendering.nui;

import org.terasology.context.internal.ContextImpl;

public abstract class WidgetWithOrder extends CoreWidget {

    //TODO: call init of tabbingManager

    @LayoutConfig
    private int order = -9999;

    protected TabbingManager tabbingManager;

    public WidgetWithOrder() {
        this.setId("");
        tabbingManager = new ContextImpl().get(TabbingManager.class);
        if (tabbingManager == null) {
            tabbingManager = new TabbingManager();
        }
    }
    public WidgetWithOrder(String id) {
        this.setId(id);
        tabbingManager = new ContextImpl().get(TabbingManager.class);
        if (tabbingManager == null) {
            tabbingManager = new TabbingManager();
        }
    }

    public int getOrder() {
        if (order != -9999) {
            order = TabbingManager.getNewNextNum();
        } else {
            TabbingManager.addToUsedNums(order, this);
        }

        return order;
    }
}
