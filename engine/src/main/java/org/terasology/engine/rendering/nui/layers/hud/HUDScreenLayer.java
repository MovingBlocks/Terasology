// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.hud;

import com.google.common.collect.Maps;
import org.joml.Vector2i;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.joml.geom.Rectanglefc;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.math.TeraMath;
import org.terasology.nui.Canvas;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.UIElement;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class HUDScreenLayer extends CoreScreenLayer {

    private Map<ResourceUrn, HUDElement> elementsLookup = Maps.newLinkedHashMap();

    @In
    private AssetManager assetManager;

    private NUIManager manager;

    public ControlWidget addHUDElement(String uri) {
        return addHUDElement(uri, ControlWidget.class, new Rectanglef(0, 0, 1, 1));
    }

    public <T extends ControlWidget> T addHUDElement(String urn, Class<T> type, Rectanglef region) {
        Optional<? extends UIElement> data = assetManager.getAsset(urn, UIElement.class);
        if (data.isPresent() && type.isInstance(data.get().getRootWidget())) {
            return addHUDElement(data.get().getUrn(), type.cast(data.get().getRootWidget()), region);
        }
        return null;

    }

    public <T extends ControlWidget> T addHUDElement(ResourceUrn urn, Class<T> type, Rectanglef region) {
        Optional<? extends UIElement> data = assetManager.getAsset(urn, UIElement.class);
        if (data.isPresent() && type.isInstance(data.get().getRootWidget())) {
            return addHUDElement(urn, type.cast(data.get().getRootWidget()), region);
        }
        return null;
    }

    public <T extends ControlWidget> T addHUDElement(ResourceUrn urn, T widget, Rectanglef region) {
        InjectionHelper.inject(widget);
        widget.onOpened();
        elementsLookup.put(urn, new HUDElement(widget, region));
        return widget;
    }

    public ControlWidget getHUDElement(String urn) {
        return getHUDElement(new ResourceUrn(urn));
    }

    public ControlWidget getHUDElement(ResourceUrn urn) {
        HUDElement element = elementsLookup.get(urn);
        if (element != null) {
            return element.widget;
        }
        return null;
    }

    public <T extends ControlWidget> T getHUDElement(String uri, Class<T> type) {
        return getHUDElement(new ResourceUrn(uri), type);
    }

    public <T extends ControlWidget> T getHUDElement(ResourceUrn urn, Class<T> type) {
        ControlWidget widget = getHUDElement(urn);
        if (widget != null && type.isInstance(widget)) {
            return type.cast(widget);
        }
        return null;
    }

    public boolean removeHUDElement(ResourceUrn uri) {
        HUDElement removed = elementsLookup.remove(uri);
        if (removed != null) {
            removed.widget.onClosed();
            return true;
        }
        return false;
    }

    public boolean removeHUDElement(ControlWidget element) {
        Iterator<Map.Entry<ResourceUrn, HUDElement>> iterator = elementsLookup.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ResourceUrn, HUDElement> item = iterator.next();
            if (item.getValue().widget.equals(element)) {
                iterator.remove();
                item.getValue().widget.onClosed();
                return true;
            }
        }
        return false;
    }

    public void clear() {
        for (HUDElement value : elementsLookup.values()) {
            value.widget.onClosed();
        }
        elementsLookup.clear();
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    @Override
    public boolean isReleasingMouse() {
        return false;
    }

    @Override
    protected boolean isEscapeToCloseAllowed() {
        return false;
    }

    @Override
    public NUIManager getManager() {
        return manager;
    }

    @Override
    public void setManager(NUIManager manager) {
        this.manager = manager;
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (HUDElement element : elementsLookup.values()) {
            int minX = TeraMath.floorToInt(element.region.minX() * canvas.size().x);
            int minY = TeraMath.floorToInt(element.region.minY() * canvas.size().y);
            int sizeX = TeraMath.floorToInt(element.region.getSizeX() * canvas.size().x);
            int sizeY = TeraMath.floorToInt(element.region.getSizeY() * canvas.size().y);
            Rectanglei region = new Rectanglei(minX, minY).setSize(sizeX, sizeY);
            canvas.drawWidget(element.widget, region);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return new Iterator<UIWidget>() {
            private Iterator<HUDElement> elementIterator = elementsLookup.values().iterator();

            @Override
            public boolean hasNext() {
                return elementIterator.hasNext();
            }

            @Override
            public UIWidget next() {
                return elementIterator.next().widget;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void onOpened() {
    }

    @Override
    public void initialise() {
    }

    @Override
    public boolean isModal() {
        return false;
    }

    private static final class HUDElement {
        ControlWidget widget;
        Rectanglef region = new Rectanglef();

        private HUDElement(ControlWidget widget, Rectanglefc region) {
            this.widget = widget;
            this.region.set(region);
        }
    }
}
