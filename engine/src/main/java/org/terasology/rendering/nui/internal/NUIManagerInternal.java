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
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.i18n.TranslationSystem;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.InputSystem;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseAxisEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.module.ModuleEnvironment;
import org.terasology.network.ClientComponent;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.ScreenLayerClosedEvent;
import org.terasology.rendering.nui.SortOrderSystem;
import org.terasology.rendering.nui.TabbingManager;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.layers.hud.HUDScreenLayer;
import org.terasology.rendering.nui.layers.ingame.OnlinePlayersOverlay;
import org.terasology.utilities.Assets;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 */
public class NUIManagerInternal extends BaseComponentSystem implements NUIManager, PropertyChangeListener {
    private Logger logger = LoggerFactory.getLogger(NUIManagerInternal.class);
    private Deque<UIScreenLayer> screens = Queues.newArrayDeque();
    private HUDScreenLayer hudScreenLayer;
    private BiMap<ResourceUrn, UIScreenLayer> screenLookup = HashBiMap.create();
    private CanvasControl canvas;
    private WidgetLibrary widgetsLibrary;
    private UIWidget focus;
    private KeyboardDevice keyboard;
    private MouseDevice mouse;
    private DisplayDevice display;
    private boolean forceReleaseMouse;
    private boolean updateFrozen;
    private RenderingConfig renderingConfig;
    private float uiScale = 1f;

    private Map<ResourceUrn, ControlWidget> overlays = Maps.newLinkedHashMap();
    private Context context;
    private AssetManager assetManager;

    public NUIManagerInternal(CanvasRenderer renderer, Context context) {
        this.context = context;
        this.hudScreenLayer = new HUDScreenLayer();
        InjectionHelper.inject(hudScreenLayer, context);
        this.canvas = new CanvasImpl(this, context, renderer);
        this.keyboard = context.get(InputSystem.class).getKeyboard();
        this.mouse = context.get(InputSystem.class).getMouseDevice();

        this.renderingConfig = context.get(Config.class).getRendering();
        this.uiScale = this.renderingConfig.getUiScale() / 100f;
        this.renderingConfig.subscribe(RenderingConfig.UI_SCALE, this);

        this.display = context.get(DisplayDevice.class);
        this.assetManager = context.get(AssetManager.class);
        refreshWidgetsLibrary();

        TranslationSystem system = context.get(TranslationSystem.class);
        system.subscribe(proj -> invalidate());

        // All UIElement instances are disposed so that they are not automatically reloaded
        // by the AssetTypeManager. Reloading would not trigger the initialise() method
        // and UI screens should be created on demand anyway.
        ModuleAwareAssetTypeManager maaTypeManager = context.get(ModuleAwareAssetTypeManager.class);
        maaTypeManager.getAssetType(UIElement.class).ifPresent(type -> type.disposeAll());
    }

    @Override
    public Deque<UIScreenLayer> getScreens() {
        return screens;
    }

    @Override
    public void setScreens(Deque<UIScreenLayer> toSet) {
        screens = toSet;
    }

    public void refreshWidgetsLibrary() {
        widgetsLibrary = new WidgetLibrary(context);
        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        for (Class<? extends UIWidget> type : environment.getSubtypesOf(UIWidget.class)) {
            widgetsLibrary.register(new SimpleUri(environment.getModuleProviding(type), type.getSimpleName()), type);
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
        return isOpen(new ResourceUrn(screenUri));
    }

    @Override
    public boolean isOpen(ResourceUrn screenUri) {
        return screenLookup.containsKey(screenUri);
    }

    @Override
    public boolean isOpen(UIElement element) {
        return isOpen(element.getUrn());
    }

    @Override
    public UIScreenLayer getScreen(ResourceUrn screenUri) {
        return screenLookup.get(screenUri);
    }

    @Override
    public UIScreenLayer getScreen(String screenUri) {
        return getScreen(new ResourceUrn(screenUri));
    }

    @Override
    public void closeScreen(String screenUri) {
        closeScreen(new ResourceUrn(screenUri));
    }

    @Override
    public void closeScreen(ResourceUrn screenUri) {
        boolean sendEvents = true;
        closeScreen(screenUri, sendEvents);
    }

    private void closeScreen(ResourceUrn screenUri, boolean sendEvents) {
        UIScreenLayer screen = screenLookup.remove(screenUri);
        if (screen != null) {
            screens.remove(screen);
            onCloseScreen(screen, screenUri, sendEvents);
        }
    }

    private void closeScreenWithoutEvent(ResourceUrn screenUri) {
        boolean sendEvents = false;
        closeScreen(screenUri, sendEvents);
    }

    @Override
    public ResourceUrn getUri(UIScreenLayer screen) {
        BiMap<ResourceUrn, UIScreenLayer> lookup =  HashBiMap.create(screenLookup);
        return lookup.inverse().remove(screen);
    }
    @Override
    public void closeScreen(UIScreenLayer screen) {
        if (screens.remove(screen)) {
            ResourceUrn screenUri = screenLookup.inverse().remove(screen);
            onCloseScreen(screen, screenUri, true);
        }
    }

    private void onCloseScreen(UIScreenLayer screen, ResourceUrn screenUri, boolean sendEvents) {
        screen.onClosed();
        if (sendEvents) {
            LocalPlayer localPlayer = context.get(LocalPlayer.class);
            if (localPlayer != null) {
                localPlayer.getClientEntity().send(new ScreenLayerClosedEvent(screenUri));
            }
        }
    }

    @Override
    public void closeScreen(UIElement element) {
        closeScreen(element.getUrn());
    }

    @Override
    public void closeAllScreens() {
        for (UIScreenLayer screen : screens) {
            if (screen.isLowerLayerVisible()) {
                closeScreen(screen);
            }
        }
    }

    @Override
    public void toggleScreen(String screenUri) {
        toggleScreen(new ResourceUrn(screenUri));
    }

    @Override
    public void toggleScreen(ResourceUrn screenUri) {
        if (isOpen(screenUri)) {
            closeScreen(screenUri);
        } else {
            pushScreen(screenUri);
        }
    }

    @Override
    public void toggleScreen(UIElement element) {
        toggleScreen(element.getUrn());
    }

    @Override
    public UIScreenLayer createScreen(String screenUri) {
        return createScreen(screenUri, CoreScreenLayer.class);
    }

    @Override
    public UIScreenLayer createScreen(ResourceUrn screenUri) {
        return createScreen(screenUri, CoreScreenLayer.class);
    }

    @Override
    public <T extends CoreScreenLayer> T createScreen(String screenUri, Class<T> expectedType) {
        Set<ResourceUrn> urns = assetManager.resolve(screenUri, UIElement.class);
        switch (urns.size()) {
            case 0:
                logger.warn("No asset found for screen '{}'", screenUri);
                return null;
            case 1:
                ResourceUrn urn = urns.iterator().next();
                return createScreen(urn, expectedType);
            default:
                logger.warn("Multiple matches for screen '{}': {}", screenUri, urns);
                return null;
        }
    }

    @Override
    public <T extends CoreScreenLayer> T createScreen(ResourceUrn screenUri, Class<T> expectedType) {
        boolean existsAlready = !screenUri.isInstance() && assetManager.isLoaded(screenUri, UIElement.class);

        Optional<UIElement> opt = Assets.get(screenUri, UIElement.class);
        if (!opt.isPresent()) {
            logger.error("Can't find screen '{}'", screenUri);
        } else {
            UIElement element = opt.get();
            UIWidget root = element.getRootWidget();
            if (expectedType.isInstance(root)) {
                T screen = expectedType.cast(root);
                if (!existsAlready) {
                    initialiseScreen(screen, screenUri);
                }
                return screen;
            } else {
                logger.error("Screen '{}' is a '{}' and not a '{}'", screenUri, root.getClass(), expectedType);
            }
        }
        return null;
    }

    @Override
    public CoreScreenLayer pushScreen(ResourceUrn screenUri) {
        return pushScreen(screenUri, CoreScreenLayer.class);
    }

    @Override
    public <T extends CoreScreenLayer> T pushScreen(ResourceUrn screenUri, Class<T> expectedType) {
        T layer = createScreen(screenUri, expectedType);
        if (layer != null) {
            pushScreen(layer);
        }
        return layer;
    }

    @Override
    public CoreScreenLayer pushScreen(String screenUri) {
        return pushScreen(screenUri, CoreScreenLayer.class);
    }

    @Override
    public <T extends CoreScreenLayer> T pushScreen(String screenUri, Class<T> expectedType) {
        T screen = createScreen(screenUri, expectedType);
        if (screen != null) {
            pushScreen(screen);
        }
        return screen;
    }

    @Override
    public CoreScreenLayer pushScreen(UIElement element) {
        return pushScreen(element, CoreScreenLayer.class);
    }

    @Override
    public <T extends CoreScreenLayer> T pushScreen(UIElement element, Class<T> expectedType) {
        if (element != null && expectedType.isInstance(element.getRootWidget())) {
            @SuppressWarnings("unchecked")
            T result = (T) element.getRootWidget();
            initialiseScreen(result, element.getUrn());
            pushScreen(result);
            return result;
        }
        return null;
    }

    private void initialiseScreen(CoreScreenLayer screen, ResourceUrn uri) {
        InjectionHelper.inject(screen);
        screen.setId(uri.toString());
        screen.setManager(this);
        screen.initialise();
    }

    @Override
    public void pushScreen(UIScreenLayer screen) {
        TabbingManager.setInitialized(false);
        if (!screen.isLowerLayerVisible()) {
            UIScreenLayer current = screens.peek();
            if (current != null) {
                current.onHide();
            }
        }
        screens.push(screen);
        screen.onOpened();
        String id = screen.getId();
        if (ResourceUrn.isValid(id)) {
            ResourceUrn uri = new ResourceUrn(id);
            screenLookup.put(uri, screen);
        }
    }

    @Override
    public void popScreen() {
        if (!screens.isEmpty()) {
            UIScreenLayer top = screens.peek();
            closeScreen(top);
            if (!top.isLowerLayerVisible()) {
                UIScreenLayer current = screens.peek();
                if (current != null) {
                    current.onShow();
                }
            }
        }
    }

    @Override
    public <T extends ControlWidget> T addOverlay(String overlayUri, Class<T> expectedType) {
        Set<ResourceUrn> urns = assetManager.resolve(overlayUri, UIElement.class);
        switch (urns.size()) {
            case 0:
                logger.warn("No asset found for overlay '{}'", overlayUri);
                return null;
            case 1:
                ResourceUrn urn = urns.iterator().next();
                return addOverlay(urn, expectedType);
            default:
                logger.warn("Multiple matches for overlay '{}': {}", overlayUri, urns);
                return null;
        }
    }

    @Override
    public <T extends ControlWidget> T addOverlay(ResourceUrn overlayUri, Class<T> expectedType) {
        boolean existsAlready = assetManager.isLoaded(overlayUri, UIElement.class);

        Optional<UIElement> opt = Assets.get(overlayUri, UIElement.class);
        if (!opt.isPresent()) {
            logger.error("Can't find overlay '{}'", overlayUri);
        } else {
            UIElement element = opt.get();
            UIWidget root = element.getRootWidget();
            if (expectedType.isInstance(root)) {
                T overlay = expectedType.cast(root);
                if (!existsAlready) {
                    initialiseOverlay(overlay, overlayUri);
                }
                addOverlay(overlay, overlayUri);
                return overlay;
            } else {
                logger.error("Screen '{}' is a '{}' and not a '{}'", overlayUri, root.getClass(), expectedType);
            }
        }
        return null;
    }

    private <T extends ControlWidget> void initialiseOverlay(T overlay, ResourceUrn screenUri) {
        InjectionHelper.inject(overlay);
        overlay.initialise();
    }

    @Override
    public <T extends ControlWidget> T addOverlay(UIElement element, Class<T> expectedType) {
        if (element != null && expectedType.isInstance(element.getRootWidget())) {
            T result = expectedType.cast(element.getRootWidget());
            addOverlay(result, element.getUrn());
            return result;
        }
        return null;
    }

    private void addOverlay(ControlWidget overlay, ResourceUrn uri) {
        if (!AbstractWidget.getShiftPressed() || !SortOrderSystem.getControlPressed() || !overlay.getClass().equals(OnlinePlayersOverlay.class)) {
            overlay.onOpened();
            overlays.put(uri, overlay);
        }
    }

    @Override
    public void removeOverlay(UIElement overlay) {
        removeOverlay(overlay.getUrn());
    }

    @Override
    public void removeOverlay(String uri) {
        Set<ResourceUrn> assetUri = Assets.resolveAssetUri(uri, UIElement.class);
        if (assetUri.size() == 1) {
            removeOverlay(assetUri.iterator().next());
        }
    }

    @Override
    public void removeOverlay(ResourceUrn uri) {
        ControlWidget widget = overlays.remove(uri);
        if (widget != null) {
            widget.onClosed();
        }
    }

    @Override
    public void clear() {
        overlays.values().forEach(ControlWidget::onClosed);
        overlays.clear();
        hudScreenLayer.clear();
        screens.forEach(ControlWidget::onClosed);
        screens.clear();
        screenLookup.clear();
        focus = null;
        forceReleaseMouse = false;
    }

    @Override
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
        for (ControlWidget overlay : overlays.values()) {
            canvas.drawWidget(overlay);
        }
        canvas.postRender();
    }

    @Override
    public void update(float delta) {
        canvas.processMousePosition(mouse.getPosition());

        // part of the update could be adding/removing screens
        // modifying a collection while iterating of it is typically not supported
        for (UIScreenLayer screen : new ArrayList<>(screens)) {
            screen.update(delta);
        }

        for (ControlWidget widget : overlays.values()) {
            widget.update(delta);
        }
        InputSystem inputSystem = context.get(InputSystem.class);
        inputSystem.getMouseDevice().setGrabbed(inputSystem.isCapturingMouse() && !(this.isReleasingMouse()));

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
        return forceReleaseMouse;
    }

    @Override
    public boolean isForceReleasingMouse() {
        return forceReleaseMouse;
    }

    @Override
    public void setForceReleasingMouse(boolean value) {
        forceReleaseMouse = value;
    }

    /*
      The following events will capture the mouse and keyboard inputs. They have high priority so the GUI will
      have first pick of input
    */

    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseAxisEvent(MouseAxisEvent event, EntityRef entity) {
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //mouse button events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity) {
        if (!mouse.isVisible()) {
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
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity) {
        if (!mouse.isVisible()) {
            return;
        }

        if (focus != null) {
            focus.onMouseWheelEvent(event);
            if (event.isConsumed()) {
                return;
            }
        }



        if (canvas.processMouseWheel(event.getWheelTurns(), mouse.getPosition())) {
            event.consume();
        }
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //raw input events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void keyEvent(KeyEvent ev, EntityRef entity) {
        NUIKeyEvent nuiEvent = new NUIKeyEvent(mouse, keyboard, ev.getKey(), ev.getKeyCharacter(), ev.getState());
        if (focus != null) {
            if (focus.onKeyEvent(nuiEvent)) {
                ev.consume();
            }
        }

        // send event to screen stack if not yet consumed
        if (!ev.isConsumed()) {
            for (UIScreenLayer screen : screens) {
                if (screen != focus) {    // explicit identity check
                    if (screen.onKeyEvent(nuiEvent)) {
                        ev.consume();
                        break;
                    }
                }
                if (screen.isModal()) {
                    break;
                }
            }
        }
    }

    //bind input events (will be send after raw input events, if a bind button was pressed and the raw input event hasn't consumed the event)
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
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
        for (UIScreenLayer screen : screens) {
            if (screen.isModal()) {
                event.consume();
                return;
            }
        }
    }

    @Override
    public void invalidate() {
        assetManager.getLoadedAssets(UIElement.class).forEach(UIElement::dispose);

        boolean hudVisible = isHUDVisible();
        if (hudVisible) {
            setHUDVisible(false);
        }

        Deque<ResourceUrn> reverseUrns = new LinkedList<>();
        Map<UIScreenLayer, ResourceUrn> inverseLookup = screenLookup.inverse();
        for (UIScreenLayer screen : screens) {
            screen.onClosed();
            reverseUrns.addFirst(inverseLookup.get(screen));
        }

        screens.clear();
        screenLookup.clear();

        reverseUrns.forEach(this::pushScreen);

        if (hudVisible) {
            setHUDVisible(true);
        }
    }
    @Override
    public CanvasControl getCanvas() {
        return canvas;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(RenderingConfig.UI_SCALE)) {
            this.uiScale = this.renderingConfig.getUiScale() / 100f;
        }
    }
}
