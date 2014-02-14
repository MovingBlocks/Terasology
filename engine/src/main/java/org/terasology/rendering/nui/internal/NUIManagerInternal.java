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
package org.terasology.rendering.nui.internal;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.Time;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.Keyboard;
import org.terasology.input.Mouse;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.network.ClientComponent;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.FocusManager;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.layers.hud.HUDScreenLayer;

import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class NUIManagerInternal extends BaseComponentSystem implements NUIManager, FocusManager {

    private AssetManager assetManager;

    private Deque<UIScreenLayer> screens = Queues.newArrayDeque();
    private HUDScreenLayer hudScreenLayer = new HUDScreenLayer();
    private BiMap<AssetUri, UIScreenLayer> screenLookup = HashBiMap.create();
    private CanvasControl canvas;
    private WidgetLibrary widgetsLibrary;
    private UIWidget focus;

    private List<ControlWidget> overlays = Lists.newArrayList();

    public NUIManagerInternal(AssetManager assetManager, CanvasRenderer renderer) {
        this.assetManager = assetManager;
        this.canvas = new CanvasImpl(this, CoreRegistry.get(Time.class), renderer);
    }

    public void refreshWidgetsLibrary() {
        widgetsLibrary = new WidgetLibrary(CoreRegistry.get(ReflectFactory.class), CoreRegistry.get(CopyStrategyLibrary.class));
        for (Map.Entry<String, Class<? extends UIWidget>> entry : CoreRegistry.get(ModuleManager.class).findAllSubclassesOf(UIWidget.class).entries()) {
            widgetsLibrary.register(new SimpleUri(entry.getKey(), entry.getValue().getSimpleName()), entry.getValue());
        }
    }

    @Override
    public HUDScreenLayer getHUD() {
        return hudScreenLayer;
    }

    @Override
    public boolean isHUDVisible() {
        return !screens.isEmpty() && screens.getLast() == hudScreenLayer;
    }

    @Override
    public void setHUDVisible(boolean visible) {
        if (visible) {
            if (!isHUDVisible()) {
                screens.addLast(hudScreenLayer);
            }
        } else {
            if (isHUDVisible()) {
                screens.removeLast();
            }
        }
    }


    @Override
    public boolean isOpen(String screenUri) {
        return isOpen(new AssetUri(AssetType.UI_ELEMENT, screenUri));
    }

    @Override
    public boolean isOpen(AssetUri screenUri) {
        return screenLookup.containsKey(screenUri);
    }

    @Override
    public UIScreenLayer getScreen(AssetUri screenUri) {
        return screenLookup.get(screenUri);
    }

    @Override
    public UIScreenLayer getScreen(String screenUri) {
        return getScreen(new AssetUri(AssetType.UI_ELEMENT, screenUri));
    }

    @Override
    public void closeScreen(String screenUri) {
        closeScreen(new AssetUri(AssetType.UI_ELEMENT, screenUri));
    }

    @Override
    public void closeScreen(AssetUri screenUri) {
        UIScreenLayer screen = screenLookup.remove(screenUri);
        if (screen != null) {
            screens.remove(screen);
        }
    }

    @Override
    public void closeScreen(UIScreenLayer screen) {
        if (screens.remove(screen)) {
            screenLookup.inverse().remove(screen);
        }
    }

    @Override
    public void toggleScreen(String screenUri) {
        toggleScreen(new AssetUri(AssetType.UI_ELEMENT, screenUri));
    }

    @Override
    public void toggleScreen(AssetUri screenUri) {
        if (isOpen(screenUri)) {
            closeScreen(screenUri);
        } else {
            pushScreen(screenUri);
        }
    }

    @Override
    public UIScreenLayer pushScreen(AssetUri screenUri) {
        UIData data = assetManager.loadAssetData(screenUri, UIData.class);
        if (data != null && data.getRootWidget() instanceof CoreScreenLayer) {
            CoreScreenLayer result = (CoreScreenLayer) data.getRootWidget();
            result.setId(screenUri.toNormalisedSimpleString());
            pushScreen(result, screenUri);
            return result;
        }
        return null;
    }

    @Override
    public UIScreenLayer pushScreen(String screenUri) {
        AssetUri assetUri = assetManager.resolve(AssetType.UI_ELEMENT, screenUri);
        if (assetUri != null) {
            return pushScreen(assetUri);
        }
        return null;
    }

    @Override
    public <T extends CoreScreenLayer> T pushScreen(AssetUri screenUri, Class<T> expectedType) {
        UIScreenLayer result = pushScreen(screenUri);
        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        }
        return null;
    }

    @Override
    public <T extends CoreScreenLayer> T pushScreen(String screenUri, Class<T> expectedType) {
        UIScreenLayer result = pushScreen(screenUri);
        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        }
        return null;
    }

    @Override
    public void pushScreen(CoreScreenLayer screen) {
        pushScreen(screen, null);
    }

    public void pushScreen(CoreScreenLayer screen, AssetUri uri) {
        screen.setManager(this);
        prepare(screen);
        screens.push(screen);
        if (uri != null) {
            screenLookup.put(uri, screen);
        }
    }

    @Override
    public void popScreen() {
        if (!screens.isEmpty()) {
            UIScreenLayer popped = screens.pop();
            screenLookup.inverse().remove(popped);
        }
    }

    @Override
    public UIScreenLayer setScreen(AssetUri screenUri) {
        UIData data = assetManager.loadAssetData(screenUri, UIData.class);
        if (data != null && data.getRootWidget() instanceof CoreScreenLayer) {
            CoreScreenLayer result = (CoreScreenLayer) data.getRootWidget();
            result.setId(screenUri.toNormalisedSimpleString());
            setScreen(result, screenUri);
            return result;
        }
        return null;
    }

    @Override
    public UIScreenLayer setScreen(String screenUri) {
        AssetUri assetUri = assetManager.resolve(AssetType.UI_ELEMENT, screenUri);
        if (assetUri != null) {
            return setScreen(assetUri);
        }
        return null;
    }

    @Override
    public <T extends CoreScreenLayer> T setScreen(AssetUri screenUri, Class<T> expectedType) {
        UIScreenLayer result = setScreen(screenUri);
        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        }
        return null;
    }

    @Override
    public <T extends CoreScreenLayer> T setScreen(String screenUri, Class<T> expectedType) {
        UIScreenLayer result = setScreen(screenUri);
        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        }
        return null;
    }

    @Override
    public void setScreen(CoreScreenLayer screen) {
        setScreen(screen, null);
    }

    @Override
    public <T extends ControlWidget> T addOverlay(String screenUri, Class<T> expectedType) {
        AssetUri assetUri = assetManager.resolve(AssetType.UI_ELEMENT, screenUri);
        if (assetUri != null) {
            return addOverlay(assetUri, expectedType);
        }
        return null;
    }

    @Override
    public <T extends ControlWidget> T addOverlay(AssetUri screenUri, Class<T> expectedType) {
        UIData data = assetManager.loadAssetData(screenUri, UIData.class);
        if (data != null && expectedType.isInstance(data.getRootWidget())) {
            T result = expectedType.cast(data.getRootWidget());
            addOverlay(result);
            return result;
        }
        return null;
    }

    @Override
    public void addOverlay(ControlWidget overlay) {
        prepare(overlay);
        overlays.add(overlay);
    }

    @Override
    public void removeOverlay(ControlWidget overlay) {
        overlays.remove(overlay);
    }

    public void setScreen(CoreScreenLayer screen, AssetUri uri) {
        screens.clear();
        pushScreen(screen, uri);
    }

    @Override
    public void clear() {
        overlays.clear();
        hudScreenLayer.clear();
        screens.clear();
        screenLookup.clear();
        focus = null;
    }

    public void render() {
        canvas.preRender();
        Deque<UIScreenLayer> screensToRender = Queues.newArrayDeque();
        for (UIScreenLayer layer : screens) {
            screensToRender.push(layer);
            if (!layer.isLowerLayerVisible()) {
                break;
            }
        }
        for (UIScreenLayer screen : screensToRender) {
            canvas.drawWidget(screen, canvas.getRegion());
        }
        for (ControlWidget overlay : overlays) {
            canvas.drawWidget(overlay);
        }
        canvas.postRender();
    }

    public void update(float delta) {
        canvas.processMousePosition(Mouse.getPosition());

        for (UIScreenLayer screen : screens) {
            screen.update(delta);
        }

        for (ControlWidget widget : overlays) {
            widget.update(delta);
        }
    }

    @Override
    public ClassLibrary<UIWidget> getWidgetMetadataLibrary() {
        return widgetsLibrary;
    }

    @Override
    public void setFocus(UIWidget widget) {
        if (widget != null && !widget.canBeFocus()) {
            return;
        }
        if (!Objects.equal(widget, focus)) {
            if (focus != null) {
                focus.onLoseFocus();
            }
            focus = widget;
            if (focus != null) {
                focus.onGainFocus();
            }
        }
    }

    @Override
    public UIWidget getFocus() {
        return focus;
    }

    @Override
    public boolean isReleasingMouse() {
        for (UIScreenLayer screen : screens) {
            if (screen.isReleasingMouse()) {
                return true;
            }
        }
        return false;
    }

    /*
      The following events will capture the mouse and keyboard inputs. They have high priority so the GUI will
      have first pick of input
    */

    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void mouseAxisEvent(MouseAxisEvent event, EntityRef entity) {
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //mouse button events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity) {
        if (!Mouse.isVisible()) {
            return;
        }
        if (focus != null) {
            focus.onMouseButtonEvent(event);
            if (event.isConsumed()) {
                return;
            }
        }
        if (event.isDown()) {
            if (canvas.processMouseClick(event.getButton(), event.getMousePosition())) {
                event.consume();
            }
        } else {
            if (canvas.processMouseRelease(event.getButton(), event.getMousePosition())) {
                event.consume();
            }
        }
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //mouse wheel events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity) {
        if (!Mouse.isVisible()) {
            return;
        }

        if (focus != null) {
            focus.onMouseWheelEvent(event);
            if (event.isConsumed()) {
                return;
            }
        }
        if (canvas.processMouseWheel(event.getWheelTurns(), Mouse.getPosition())) {
            event.consume();
        }
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //raw input events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void keyEvent(KeyEvent event, EntityRef entity) {
        if (focus != null) {
            focus.onKeyEvent(event);
        }
        if (event.isDown() && !event.isConsumed() && event.getKey() == Keyboard.Key.ESCAPE) {
            for (UIScreenLayer screen : screens) {
                if (screen.isEscapeToCloseAllowed()) {
                    closeScreen(screen);
                    event.consume();
                    break;
                } else if (screen.isModal()) {
                    break;
                }
            }
        }
        for (UIScreenLayer screen : screens) {
            if (screen.isModal()) {
                event.consume();
            }
        }
    }

    //bind input events (will be send after raw input events, if a bind button was pressed and the raw input event hasn't consumed the event)
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_CRITICAL)
    public void bindEvent(BindButtonEvent event, EntityRef entity) {
        if (focus != null) {
            focus.onBindEvent(event);
        }
        if (!event.isConsumed()) {
            for (UIScreenLayer layer : screens) {
                if (layer.isReleasingMouse()) {
                    layer.onBindEvent(event);
                    if (event.isConsumed() || !layer.isLowerLayerVisible()) {
                        break;
                    }
                }
            }
        }
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    private void prepare(ControlWidget screen) {
        InjectionHelper.inject(screen);
        screen.initialise();
    }

}
