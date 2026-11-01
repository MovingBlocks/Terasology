// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui;

import org.joml.Vector2i;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystem;
import org.terasology.engine.rendering.nui.animation.MenuAnimationSystemStub;
import org.terasology.input.ButtonState;
import org.terasology.input.Keyboard;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.AbstractWidget;
import org.terasology.nui.BaseInteractionListener;
import org.terasology.nui.Canvas;
import org.terasology.nui.InteractionListener;
import org.terasology.nui.LayoutConfig;
import org.terasology.nui.TabbingManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.WidgetWithOrder;
import org.terasology.nui.events.NUIBindButtonEvent;
import org.terasology.nui.events.NUICharEvent;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.events.NUIMouseButtonEvent;
import org.terasology.nui.events.NUIMouseClickEvent;
import org.terasology.nui.events.NUIMouseWheelEvent;
import org.terasology.nui.layouts.ScrollableArea;
import org.terasology.nui.widgets.UIRadialRing;
import org.terasology.nui.widgets.UIRadialSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

public abstract class CoreScreenLayer extends AbstractWidget implements UIScreenLayer {

    private static final InteractionListener DEFAULT_SCREEN_LISTENER = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            return true;
        }
    };

    @LayoutConfig
    private UIWidget contents;

    private NUIManager manager;

    private boolean modifyingList;

    private ScrollableArea parentToSet;

    private boolean activateBindEvent;

    private MenuAnimationSystem animationSystem = new MenuAnimationSystemStub();

    public CoreScreenLayer() {
    }

    public CoreScreenLayer(String id) {
        super(id);
    }

    public int getDepth() {
        return depth;
    }

    /**
     * Automatically sets the depth of this screen using SortOrderSystem.
     */
    public void setDepthAuto() {
        if (SortOrderSystem.isInitialized()) {
            depth = SortOrderSystem.getCurrent();
        }
    }

    @Override
    @SuppressWarnings("PMD.UselessOverridingMethod") // override elevates access modifier
    public void setId(String id) {
        super.setId(id);
    }

    protected InteractionListener getScreenListener() {
        return DEFAULT_SCREEN_LISTENER;
    }

    public void setContents(UIWidget contents) {
        this.contents = contents;
    }

    @Override
    public void onOpened() {
        if (depth == SortOrderSystem.DEFAULT_DEPTH) {
            setDepthAuto();
        }
        if (SortOrderSystem.isInitialized() && !SortOrderSystem.getUsed().contains(depth)) {
            SortOrderSystem.getUsed().add(depth);
        }
        modifyingList = false;
        activateBindEvent = false;
        TabbingManager.setInitialized(false);

        animationSystem.triggerFromPrev();
        onScreenOpened();
    }

    private void iterateThrough(Iterator<UIWidget> widgets) {
        modifyingList = true;
        while (widgets.hasNext()) {
            UIWidget next = widgets.next();
            boolean setParent = false;
            if (next instanceof ScrollableArea) {
                parentToSet = (ScrollableArea) next;
            }

            if (next instanceof WidgetWithOrder) {
                TabbingManager.addToWidgetsList((WidgetWithOrder) next);
                TabbingManager.addToUsedNums(((WidgetWithOrder) next).getOrder());
                ((WidgetWithOrder) next).setParent(parentToSet);
            }

            if (next.iterator().hasNext()) {
                iterateThrough(next.iterator());
            } else if (next instanceof UIRadialRing) {
                Iterator<UIRadialSection> iter = ((UIRadialRing) next).getSections().iterator();
                while (iter.hasNext()) {
                    next = iter.next();
                    TabbingManager.addToWidgetsList((WidgetWithOrder) next);
                    TabbingManager.addToUsedNums(((WidgetWithOrder) next).getOrder());
                    if (setParent) {
                        ((WidgetWithOrder) next).setParent(parentToSet);
                    }
                }
            }
        }
        modifyingList = false;
    }

    /**
     * adds or removes from enabledWidgets based on if the screen is showing or not
     * @param showing if the screen is visible or not
     */
    protected void addOrRemove(boolean showing) {
        if (SortOrderSystem.getEnabledWidgets() != null) {
            if (!SortOrderSystem.getEnabledWidgets().contains(this)) {
                if (showing) {
                    ArrayList<CoreScreenLayer> enabledWidgets = SortOrderSystem.getEnabledWidgets();

                    enabledWidgets.add(this);
                    SortOrderSystem.setEnabledWidgets(enabledWidgets);

                    SortOrderSystem.addAnother(depth);
                }
            } else {
                if (!showing) {
                    ArrayList<CoreScreenLayer> enabledWidgets = SortOrderSystem.getEnabledWidgets();
                    enabledWidgets.remove(this);
                    SortOrderSystem.setEnabledWidgets(enabledWidgets);

                    SortOrderSystem.removeOne(depth);
                }
            }
        }
    }

    /**
     * Lifecycle method called when this screen is displayed under any circumstance.
     * <p>
     * This differs from {@link #onOpened} and {@link #onShow} in that it is called both when the
     * screen is first opened (as {@code onOpened}) as well as when a screen previously opened
     * (e.g., a parent menu in the menu system) is returned to (as {@code onShow}).
     */
    public void onScreenOpened() {
        if (!SortOrderSystem.isInSortOrder()) {
            addOrRemove(true);
        }
        // TODO: Tabbing
        //TabbingManager.setOpenScreen(this);
    }

    @Override
    public boolean isLowerLayerVisible() {
        return true;
    }

    @Override
    public boolean isReleasingMouse() {
        return true;
    }

    public UIWidget getContents() {
        return contents;
    }

    @Override
    public void onDraw(Canvas canvas) {
        Rectanglei region = animationSystem.animateRegion(canvas.getRegion());
        if (isModal()) {
            canvas.addInteractionRegion(getScreenListener(), region);
        }
        if (getContents() != null) {
            canvas.drawWidget(getContents(), region);
        }
    }

    @Override
    public void update(float delta) {
        if (contents != null) {
            if (!TabbingManager.isInitialized()) {
                TabbingManager.init();
                // TODO: Tabbing
                // TabbingManager.setOpenScreen(this);

                Iterator<UIWidget> widgets = contents.iterator();
                iterateThrough(widgets);
            }

            // TODO: Tabbing
            /*if (TabbingManager.getOpenScreen() == null) {
                TabbingManager.setOpenScreen(this);

                Iterator<UIWidget> widgets = contents.iterator();
                iterateThrough(widgets);

            }*/
            contents.update(delta);
            animationSystem.update(delta);
            if (depth == SortOrderSystem.DEFAULT_DEPTH) {
                setDepthAuto();
            }
            if (activateBindEvent) {
                onBindEvent(new NUIBindButtonEvent(null, null, "engine:tabbingUI", ButtonState.DOWN));
            }
        }
    }

    @Override
    public NUIManager getManager() {
        return manager;
    }

    public void setManager(NUIManager manager) {
        this.manager = manager;
    }

    @Override
    public void onClosed() {
        if (!SortOrderSystem.isInSortOrder()) {
            addOrRemove(false);
        }
        TabbingManager.setInitialized(false);
    }

    @Override
    public void onShow() {
        animationSystem.triggerFromNext();
        onScreenOpened();
    }

    @Override
    public void onHide() {
        if (!SortOrderSystem.isInSortOrder()) {
            addOrRemove(false);
        }
        TabbingManager.setInitialized(false);
    }

    @Override
    public void onGainFocus() {
    }

    @Override
    public void onLoseFocus() {
    }

    @Override
    public void onMouseButtonEvent(NUIMouseButtonEvent event) {
    }

    @Override
    public void onMouseWheelEvent(NUIMouseWheelEvent event) {
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown() && event.getKey() == Keyboard.Key.ESCAPE) {
            animationSystem.stop();
            if (isEscapeToCloseAllowed()) {
                triggerBackAnimation();
                return true;
            }
        }

        return super.onKeyEvent(event);
    }

    @Override
    public boolean onCharEvent(NUICharEvent nuiEvent) {
        return false;
    }

    protected boolean isEscapeToCloseAllowed() {
        return true;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        return areaHint;
    }

    @Override
    public boolean isModal() {
        return true;
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE);
    }

    @Override
    public boolean isSkinAppliedByCanvas() {
        return true;
    }

    @Override
    public String getMode() {
        return DEFAULT_MODE;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        if (contents == null) {
            return Collections.emptyIterator();
        }
        return Arrays.asList(contents).iterator();
    }

    public MenuAnimationSystem getAnimationSystem() {
        return animationSystem;
    }

    protected void setAnimationSystem(MenuAnimationSystem animationSystem) {
        this.animationSystem = animationSystem;
    }

    protected void triggerForwardAnimation(ResourceUrn screenUri) {
        // create and initialize now, open when the animation has finished
        triggerForwardAnimation(getManager().createScreen(screenUri));
    }

    protected void triggerForwardAnimation(UIScreenLayer screen) {
        animationSystem.onEnd(() -> getManager().pushScreen(screen));
        animationSystem.triggerToNext();
    }

    protected void triggerBackAnimation() {
        animationSystem.onEnd(getManager()::popScreen);
        animationSystem.triggerToPrev();
    }
}
