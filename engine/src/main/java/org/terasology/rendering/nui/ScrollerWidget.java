package org.terasology.rendering.nui;

public abstract class ScrollerWidget extends WidgetWithOrder {
    public ScrollerWidget() {
        this.setId("");
    }

    public ScrollerWidget(String id) {
        this.setId(id);
    }

    public abstract void moveDown(boolean increase);
}
