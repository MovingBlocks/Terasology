package org.terasology.rendering.nui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.input.Keyboard;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.UIDropdown;

public abstract class WidgetWithOrder extends CoreWidget {

    @LayoutConfig
    protected int order = TabbingManagerSystem.UNINITIALIZED_DEPTH;

    private boolean added = false;

    protected boolean initialized = false;

    private ScrollableArea parent;

    public WidgetWithOrder() {
        this.setId("");
    }

    public WidgetWithOrder(String id) {
        this.setId(id);
    }

    public ScrollableArea getParent() { return parent; }

    public void setParent(ScrollableArea area) { parent = area; }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        Logger logger = LoggerFactory.getLogger("widget w/ order");
        logger.info("adding");
    }
    @Override
    public String getMode() {
        if (isFocused()) {
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }
    public int getOrder() {
       // if (!(this instanceof UIScrollbar)) {
            if (order == TabbingManagerSystem.UNINITIALIZED_DEPTH) {
                order = TabbingManagerSystem.getNewNextNum();
                TabbingManagerSystem.addToWidgetsList(this);
                TabbingManagerSystem.addToUsedNums(order);
                added = true;
            } else if (!added) {
                TabbingManagerSystem.addToWidgetsList(this);
                TabbingManagerSystem.addToUsedNums(order);
                added = true;
            }
       // }
        return order;
    }
    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            logger.info("parent: "+parent);
            int keyId = event.getKey().getId();
            if (keyId == Keyboard.KeyId.UP) {
                /*
                if (TabbingManagerSystem.focusedWidget instanceof UIRadialRing) {
                    ((UIRadialRing) TabbingManagerSystem.focusedWidget).changeSelectedTab(true);
                } else */if (TabbingManagerSystem.focusedWidget instanceof UIDropdown) {
                    if (((UIDropdown)TabbingManagerSystem.focusedWidget).isOpened()) {
                        TabbingManagerSystem.widgetIsOpen = true;
                        ((UIDropdown) TabbingManagerSystem.focusedWidget).changeHighlighted(false);
                    } else {
                        TabbingManagerSystem.widgetIsOpen = false;
                    }
                }
                if (parent != null && !TabbingManagerSystem.widgetIsOpen) {
                    parent.scroll(true);
                }
                /*else if ((TabbingManagerSystem.focusedWidget instanceof UIScrollbar) && TabbingManagerSystem.focusedWidget.getOrder() != TabbingManagerSystem.UNINITIALIZED_DEPTH) {
                    logger.info("instance of scroller");
                    ((UIScrollbar)TabbingManagerSystem.focusedWidget).moveDown(false);
                }*/
                return true;
            } else if (keyId == Keyboard.KeyId.DOWN) {
                /*
                if (TabbingManagerSystem.focusedWidget instanceof UIRadialRing) {
                    ((UIRadialRing) TabbingManagerSystem.focusedWidget).changeSelectedTab(false);
                } else */if (TabbingManagerSystem.focusedWidget instanceof UIDropdown) {
                    if (((UIDropdown)TabbingManagerSystem.focusedWidget).isOpened()) {
                        TabbingManagerSystem.widgetIsOpen = true;
                        ((UIDropdown) TabbingManagerSystem.focusedWidget).changeHighlighted(true);
                    } else {
                        TabbingManagerSystem.widgetIsOpen = false;
                    }
                } /*else if ((TabbingManagerSystem.focusedWidget instanceof UIScrollbar) && TabbingManagerSystem.focusedWidget.getOrder() != TabbingManagerSystem.UNINITIALIZED_DEPTH) {
                    logger.info("instance of scroller");
                    ((UIScrollbar)TabbingManagerSystem.focusedWidget).moveDown(true);
                }*/
                if (parent != null && !TabbingManagerSystem.widgetIsOpen) {
                    parent.scroll(false);
                }
                return true;
            }
        }
        return super.onKeyEvent(event);
    }
}
