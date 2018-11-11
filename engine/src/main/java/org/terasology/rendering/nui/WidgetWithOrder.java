package org.terasology.rendering.nui;

public abstract class WidgetWithOrder extends CoreWidget {
    @LayoutConfig
    private int order = -9999;

    public int getOrder() {
        if (order != -9999) {
            order = tabbingManager.getNewNextNum();
        } else {
            tabbingManager.addToUsedNums(order, this);
        }

        return order;
    }
}
