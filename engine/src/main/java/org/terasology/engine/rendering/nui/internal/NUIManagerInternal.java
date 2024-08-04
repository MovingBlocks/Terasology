// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.nui.internal;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.core.subsystem.config.BindsManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.i18n.TranslationSystem;
import org.terasology.engine.input.BindButtonEvent;
import org.terasology.engine.input.InputSystem;
import org.terasology.engine.input.events.CharEvent;
import org.terasology.engine.input.events.KeyEvent;
import org.terasology.engine.input.events.MouseAxisEvent;
import org.terasology.engine.input.events.MouseButtonEvent;
import org.terasology.engine.input.events.MouseWheelEvent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.ScreenLayerClosedEvent;
import org.terasology.engine.rendering.nui.SortOrderSystem;
import org.terasology.engine.rendering.nui.UIScreenLayer;
import org.terasology.engine.rendering.nui.layers.hud.HUDScreenLayer;
import org.terasology.engine.rendering.nui.layers.ingame.OnlinePlayersOverlay;
import org.terasology.engine.rendering.nui.widgets.TypeWidgetFactoryRegistryImpl;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.input.device.MouseDevice;
import org.terasology.nui.AbstractWidget;
import org.terasology.nui.ControlWidget;
import org.terasology.nui.TabbingManager;
import org.terasology.nui.UIWidget;
import org.terasology.nui.asset.UIElement;
import org.terasology.nui.canvas.CanvasControl;
import org.terasology.nui.events.NUIBindButtonEvent;
import org.terasology.nui.events.NUICharEvent;
import org.terasology.nui.events.NUIKeyEvent;
import org.terasology.nui.events.NUIMouseButtonEvent;
import org.terasology.nui.events.NUIMouseWheelEvent;
import org.terasology.nui.reflection.WidgetLibrary;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIText;
import org.terasology.nui.widgets.types.RegisterTypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetFactory;
import org.terasology.nui.widgets.types.TypeWidgetFactoryRegistry;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Verify.verifyNotNull;

public class NUIManagerInternal extends BaseComponentSystem implements NUIManager, PropertyChangeListener {
    private final ModuleEnvironment moduleEnvironment;
    private final TypeWidgetFactoryRegistry typeWidgetFactoryRegistry;
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
    private BindsManager bindsManager;
    private TypeWidgetLibrary typeWidgetLibrary;


    public NUIManagerInternal(TerasologyCanvasRenderer renderer, Context context) {
        this.context = context;
        this.hudScreenLayer = new HUDScreenLayer();
        InjectionHelper.inject(hudScreenLayer, context);
        this.canvas = new TerasologyCanvasImpl(this, context, renderer);
        this.keyboard = context.get(InputSystem.class).getKeyboard();
        this.mouse = context.get(InputSystem.class).getMouseDevice();
        this.bindsManager = context.get(BindsManager.class);

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

        // NOTE: Taken from the original UIText source.
        UIText.DEFAULT_CURSOR_TEXTURE = assetManager.getAsset("engine:white", Texture.class).get();

        // NOTE: Taken from the original UIButton source.
        UIButton.DEFAULT_CLICK_SOUND = assetManager.getAsset("engine:click", StaticSound.class).get();

        // TODO: This is a work-around for-now to fix tabbing
        TabbingManager.setFocusManager(this);

        // NOTE: Fix for tests
        if (bindsManager != null
                && bindsManager.getBindsConfig().hasBinds(new SimpleUri("engine:tabbingUI"))
                && bindsManager.getBindsConfig().hasBinds(new SimpleUri("engine:tabbingModifier"))
                && bindsManager.getBindsConfig().hasBinds(new SimpleUri("engine:activate"))) {
            TabbingManager.tabForwardInput =
                    bindsManager.getBindsConfig().getBinds(new SimpleUri("engine:tabbingUI")).get(0);
            TabbingManager.tabBackInputModifier = bindsManager.getBindsConfig().getBinds(new SimpleUri("engine" +
                    ":tabbingModifier")).get(0);
            TabbingManager.activateInput =
                    bindsManager.getBindsConfig().getBinds(new SimpleUri("engine:activate")).get(0);
        }

        moduleEnvironment = context.get(ModuleManager.class).getEnvironment();

        typeWidgetFactoryRegistry = new TypeWidgetFactoryRegistryImpl(context);
        context.put(TypeWidgetFactoryRegistry.class, typeWidgetFactoryRegistry);
        registerTypeWidgetFactories();
    }

    private void registerTypeWidgetFactories() {
        for (Class<? extends TypeWidgetFactory> clazz : moduleEnvironment.getSubtypesOf(TypeWidgetFactory.class)) {
            if (!clazz.isAnnotationPresent(RegisterTypeWidgetFactory.class)) {
                continue;
            }

            TypeWidgetFactory instance = InjectionHelper.createWithConstructorInjection(clazz, context);
            InjectionHelper.inject(instance, context);
            typeWidgetFactoryRegistry.add(instance);
        }
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
        widgetsLibrary = new WidgetLibrary(() -> context.get(ModuleManager.class).getEnvironment(),
                context.get(ReflectFactory.class), context.get(CopyStrategyLibrary.class));
        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        for (Class<? extends UIWidget> type : environment.getSubtypesOf(UIWidget.class)) {
            Name module = verifyNotNull(environment.getModuleProviding(type), "No module provides %s", type);
            widgetsLibrary.register(new ResourceUrn(module.toString(), type.getSimpleName()), type);
        }
        // Interfaces are not instantiatable and so are not usually stored in the widget library.
        // We make a special exception in this case since all Terasology UI screens inherit from this base interface to use common styles.
        widgetsLibrary.register(new ResourceUrn("engine", UIScreenLayer.class.getSimpleName()), UIScreenLayer.class);
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

    @Override
    public ResourceUrn getUri(UIScreenLayer screen) {
        BiMap<ResourceUrn, UIScreenLayer> lookup = HashBiMap.create(screenLookup);
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
        if (opt.isEmpty()) {
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
                logger.error("Screen '{}' is a '{}' and not a '{}'", screenUri, root.getClass(), expectedType); //NOPMD
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

        initialiseControlWidget(screen, uri);
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
        if (opt.isEmpty()) {
            logger.error("Can't find overlay '{}'", overlayUri);
        } else {
            UIElement element = opt.get();
            UIWidget root = element.getRootWidget();
            if (expectedType.isInstance(root)) {
                T overlay = expectedType.cast(root);
                if (!existsAlready) {
                    initialiseControlWidget(overlay, overlayUri);
                }
                addOverlay(overlay, overlayUri);
                return overlay;
            } else {
                logger.error("Screen '{}' is a '{}' and not a '{}'", overlayUri, root.getClass(), expectedType); //NOPMD
            }
        }
        return null;
    }

    private <T extends ControlWidget> void initialiseControlWidget(T overlay, ResourceUrn screenUri) {
        ContextImpl timedContextForModulesWidgets = new ContextImpl(this.context);

        Module declaringModule = moduleEnvironment.get(screenUri.getModuleName());
        TypeWidgetLibrary moduleLibrary =
                new TypeWidgetLibraryImpl(typeWidgetFactoryRegistry, declaringModule, this.context);
        context.put(TypeWidgetLibrary.class, moduleLibrary);

        InjectionHelper.inject(overlay, timedContextForModulesWidgets);

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
    public WidgetLibrary getWidgetMetadataLibrary() {
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

    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = ClientComponent.class)
    public void mouseAxisEvent(MouseAxisEvent event, EntityRef entity) {
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //mouse button events
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = ClientComponent.class)
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity) {
        if (!mouse.isVisible()) {
            return;
        }

        Vector2i mousePosition = event.getMousePosition();
        if (focus != null) {
            focus.onMouseButtonEvent(new NUIMouseButtonEvent(event.getButton(), event.getState(), mousePosition));
            if (event.isConsumed()) {
                return;
            }
        }
        if (event.isDown()) {
            if (canvas.processMouseClick(event.getButton(), mousePosition)) {
                event.consume();
            }
        } else {
            if (canvas.processMouseRelease(event.getButton(), mousePosition)) {
                event.consume();
            }
        }
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //mouse wheel events
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = ClientComponent.class)
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity) {
        if (!mouse.isVisible()) {
            return;
        }

        Vector2i mousePosition = event.getMousePosition();
        if (focus != null) {
            NUIMouseWheelEvent nuiEvent = new NUIMouseWheelEvent(mouse, keyboard, mousePosition, event.getWheelTurns());
            focus.onMouseWheelEvent(nuiEvent);
            if (nuiEvent.isConsumed()) {
                event.consume();
                return;
            }
        }

        if (canvas.processMouseWheel(event.getWheelTurns(), mousePosition)) {
            event.consume();
        }
        if (isReleasingMouse()) {
            event.consume();
        }
    }

    //text input events
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = ClientComponent.class)
    public void charEvent(CharEvent ev, EntityRef entity) {
        NUICharEvent nuiEvent = new NUICharEvent(mouse, keyboard, ev.getCharacter());
        if (focus != null && focus.onCharEvent(nuiEvent)) {
            ev.consume();
        }

        // send event to screen stack if not yet consumed
        if (!ev.isConsumed()) {
            for (UIScreenLayer screen : screens) {
                if (screen != focus && screen.onCharEvent(nuiEvent)) {
                    // explicit identity check
                    ev.consume();
                    break;
                }
                if (screen.isModal()) {
                    break;
                }
            }
        }
    }

    //raw input events
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = ClientComponent.class)
    public void keyEvent(KeyEvent ev, EntityRef entity) {
        NUIKeyEvent nuiEvent = new NUIKeyEvent(mouse, keyboard, ev.getKey(), ev.getState());
        if (focus != null && focus.onKeyEvent(nuiEvent)) {
            ev.consume();
        }

        // send event to screen stack if not yet consumed
        if (!ev.isConsumed()) {
            for (UIScreenLayer screen : screens) {
                if (screen != focus && screen.onKeyEvent(nuiEvent)) {
                    // explicit identity check
                    ev.consume();
                    break;
                }
                if (screen.isModal()) {
                    break;
                }
            }
        }
    }

    //bind input events (will be send after raw input events, if a bind button was pressed and the raw input event
    // hasn't consumed the event)
    @Priority(EventPriority.PRIORITY_HIGH)
    @ReceiveEvent(components = ClientComponent.class)
    public void bindEvent(BindButtonEvent event, EntityRef entity) {
        NUIBindButtonEvent nuiEvent = new NUIBindButtonEvent(mouse, keyboard,
                new ResourceUrn(event.getId().getModuleName(), event.getId().getObjectName()).toString(),
                event.getState());

        if (focus != null) {
            focus.onBindEvent(nuiEvent);
        }
        if (!event.isConsumed()) {
            for (UIScreenLayer layer : screens) {
                if (layer.isReleasingMouse()) {
                    layer.onBindEvent(nuiEvent);
                    if (nuiEvent.isConsumed()) {
                        event.consume();
                        break;
                    }

                    if (!layer.isLowerLayerVisible()) {
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
