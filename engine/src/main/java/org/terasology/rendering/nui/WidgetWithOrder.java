package org.terasology.rendering.nui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.Keyboard;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UIRadialRing;

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
    @Override
    public String getMode() {
        if (isFocused()) {
            logger.info(getId()+" is focused!!!!");
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }
    public int getOrder() {
        if (!(this instanceof ScrollerWidget)) {
            if (order == TabbingManagerSystem.UNINITIALIZED_DEPTH) {
                order = TabbingManagerSystem.getNewNextNum();
                TabbingManagerSystem.addToUsedNums(order, this);
                added = true;
            } else if (!added) {
                TabbingManagerSystem.addToWidgetsList(this);
                added = true;
            }
        }
        return order;
    }
    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int keyId = event.getKey().getId();
            if (keyId == Keyboard.KeyId.UP) {
                if (TabbingManagerSystem.focusedWidget instanceof UIRadialRing) {
                    ((UIRadialRing) TabbingManagerSystem.focusedWidget).changeSelectedTab(true);
                } else if (TabbingManagerSystem.focusedWidget instanceof UIDropdown) {
                    ((UIDropdown)TabbingManagerSystem.focusedWidget).changeHighlighted(false);
                    if (!((UIDropdown) TabbingManagerSystem.focusedWidget).isOpened()) {
                    }
                } else if ((TabbingManagerSystem.focusedWidget instanceof ScrollerWidget) && TabbingManagerSystem.focusedWidget.getOrder() != TabbingManagerSystem.UNINITIALIZED_DEPTH) {
                    logger.info("instance of scroller");
                    ((ScrollerWidget)TabbingManagerSystem.focusedWidget).moveDown(false);
                }
                return true;
            } else if (keyId == Keyboard.KeyId.DOWN) {
                if (TabbingManagerSystem.focusedWidget instanceof UIRadialRing) {
                    ((UIRadialRing) TabbingManagerSystem.focusedWidget).changeSelectedTab(false);
                } else if (TabbingManagerSystem.focusedWidget instanceof UIDropdown) {
                    ((UIDropdown)TabbingManagerSystem.focusedWidget).changeHighlighted(true);
                } else if ((TabbingManagerSystem.focusedWidget instanceof ScrollerWidget) && TabbingManagerSystem.focusedWidget.getOrder() != TabbingManagerSystem.UNINITIALIZED_DEPTH) {
                    logger.info("instance of scroller");
                    ((ScrollerWidget)TabbingManagerSystem.focusedWidget).moveDown(true);
                }
                return true;
            }
        }
        return super.onKeyEvent(event);
    }
}
