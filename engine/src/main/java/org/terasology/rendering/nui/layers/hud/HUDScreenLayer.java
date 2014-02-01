/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.hud;

import com.google.common.collect.Lists;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.asset.UIData;

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class HUDScreenLayer extends CoreWidget implements UIScreenLayer {

    private List<HUDElement> elements = Lists.newArrayList();

    private AssetManager assetManager = CoreRegistry.get(AssetManager.class);
    private NUIManager manager;

    public ControlWidget addHUDElement(String uri) {
        return addHUDElement(uri, ControlWidget.class, Rect2f.createFromMinAndSize(0, 0, 1, 1));
    }

    public <T extends ControlWidget> T addHUDElement(String uri, Class<T> type, Rect2f region) {
        AssetUri resolvedUri = Assets.resolveAssetUri(AssetType.UI_ELEMENT, uri);
        if (resolvedUri != null) {
            return addHUDElement(resolvedUri, type, region);
        }
        return null;
    }

    public <T extends ControlWidget> T addHUDElement(AssetUri uri, Class<T> type, Rect2f region) {
        UIData data = assetManager.loadAssetData(uri, UIData.class);
        if (data != null && type.isInstance(data.getRootWidget())) {
            return addHUDElement(uri, type.cast(data.getRootWidget()), region);
        }
        return null;
    }

    public <T extends ControlWidget> T addHUDElement(AssetUri uri, T widget, Rect2f region) {
        InjectionHelper.inject(widget);
        widget.initialise();
        elements.add(new HUDElement(uri, widget, region));
        return widget;
    }

    public void clear() {
        elements.clear();
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
    public boolean isEscapeToCloseAllowed() {
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
        for (HUDElement element : elements) {
            int minX = TeraMath.floorToInt(element.region.minX() * canvas.size().x);
            int minY = TeraMath.floorToInt(element.region.minY() * canvas.size().y);
            int sizeX = TeraMath.floorToInt(element.region.width() * canvas.size().x);
            int sizeY = TeraMath.floorToInt(element.region.height() * canvas.size().y);
            Rect2i region = Rect2i.createFromMinAndSize(minX, minY, sizeX, sizeY);
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
            private Iterator<HUDElement> elementIterator = elements.iterator();

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
    public void initialise() {
    }

    private static final class HUDElement {
        AssetUri uri;
        ControlWidget widget;
        Rect2f region;

        private HUDElement(AssetUri uri, ControlWidget widget, Rect2f region) {
            this.uri = uri;
            this.widget = widget;
            this.region = region;
        }
    }
}
