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
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
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
import org.terasology.logic.players.LocalPlayer;
import org.terasology.module.ModuleEnvironment;
import org.terasology.network.ClientComponent;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.InjectionHelper;
import org.terasology.rendering.nui.ControlWidget;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.ScreenLayerClosedEvent;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.asset.UIData;
import org.terasology.rendering.nui.asset.UIElement;
import org.terasology.rendering.nui.layers.hud.HUDScreenLayer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class NUIManagerInternal extends BaseComponentSystem implements NUIManager {
    private Logger logger = LoggerFactory.getLogger(NUIManagerInternal.class);
    private Deque<UIScreenLayer> screens = Queues.newArrayDeque();
    private HUDScreenLayer hudScreenLayer;
    private BiMap<AssetUri, UIScreenLayer> screenLookup = HashBiMap.create();
    private CanvasControl canvas;
    private WidgetLibrary widgetsLibrary;
    private UIWidget focus;
    private boolean autoReloadEnabled;
    private Set<AssetUri> autoReloadingScreensUris;
    private Map<Path, AssetUri> filePathToAssetUriMap;
    private Map<WatchKey, Path> watchKeyToPathMap;
    private Set<Path> watchedDirectories;
    private WatchService autoReloadWatchService;

    private boolean forceReleaseMouse;

    private Map<AssetUri, ControlWidget> overlays = Maps.newLinkedHashMap();
    private Context context;

    public NUIManagerInternal(CanvasRenderer renderer, Context context) {
        this.context = context;
        this.hudScreenLayer = new HUDScreenLayer();
        InjectionHelper.inject(hudScreenLayer, context);
        this.canvas = new CanvasImpl(this, context, renderer);
    }

    public void refreshWidgetsLibrary() {
        widgetsLibrary = new WidgetLibrary(context.get(ReflectFactory.class), context.get(CopyStrategyLibrary.class));
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
        return isOpen(new AssetUri(AssetType.UI_ELEMENT, screenUri));
    }

    @Override
    public boolean isOpen(AssetUri screenUri) {
        return screenLookup.containsKey(screenUri);
    }

    @Override
    public boolean isOpen(UIElement element) {
        return isOpen(element.getURI());
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
        boolean sendEvents = true;
        closeScreen(screenUri, sendEvents);
    }

    private void closeScreen(AssetUri screenUri, boolean sendEvents) {
        UIScreenLayer screen = screenLookup.remove(screenUri);
        if (screen != null) {
            screens.remove(screen);
            onCloseScreen(screen, screenUri, sendEvents);
        }
    }

    private void closeScreenWithoutEvent(AssetUri screenUri) {
        boolean sendEvents = false;
        closeScreen(screenUri, sendEvents);
    }

    @Override
    public void closeScreen(UIScreenLayer screen) {
        if (screens.remove(screen)) {
            AssetUri screenUri = screenLookup.inverse().remove(screen);
            onCloseScreen(screen, screenUri, true);
        }
    }

    private void onCloseScreen(UIScreenLayer screen, AssetUri screenUri, boolean sendEvents) {
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
        closeScreen(element.getURI());
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
    public void toggleScreen(UIElement element) {
        toggleScreen(element.getURI());
    }

    @Override
    public UIScreenLayer pushScreen(AssetUri screenUri) {
        UIElement element = Assets.get(screenUri, UIElement.class);
        return pushScreen(element);
    }

    @Override
    public UIScreenLayer pushScreen(String screenUri) {
        UIElement element = Assets.getUIElement(screenUri);
        if (element != null) {
            return pushScreen(element);
        } else {
            logger.error("Can't find screen " + screenUri);
        }
        return null;
    }

    @Override
    public UIScreenLayer pushScreen(UIElement element) {
        if (element != null && element.getRootWidget() instanceof CoreScreenLayer) {
            CoreScreenLayer result = (CoreScreenLayer) element.getRootWidget();
            if (!screens.contains(result)) {
                result.setId(element.getURI().toString());
                pushScreen(result, element.getURI());
            }
            return result;
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
    public <T extends CoreScreenLayer> T pushScreen(UIElement element, Class<T> expectedType) {
        UIScreenLayer result = pushScreen(element);
        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        }
        return null;
    }

    private void pushScreen(CoreScreenLayer screen, AssetUri uri) {
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
            popped.onClosed();
        }
    }

    @Override
    public <T extends ControlWidget> T addOverlay(String screenUri, Class<T> expectedType) {
        UIElement element = Assets.getUIElement(screenUri);
        return addOverlay(element, expectedType);
    }

    @Override
    public <T extends ControlWidget> T addOverlay(AssetUri screenUri, Class<T> expectedType) {
        UIElement element = Assets.get(screenUri, UIElement.class);
        return addOverlay(element, expectedType);
    }

    @Override
    public <T extends ControlWidget> T addOverlay(UIElement element, Class<T> expectedType) {
        if (element != null && expectedType.isInstance(element.getRootWidget())) {
            T result = expectedType.cast(element.getRootWidget());
            addOverlay(result, element.getURI());
            return result;
        }
        return null;
    }

    private void addOverlay(ControlWidget overlay, AssetUri uri) {
        prepare(overlay);
        overlays.put(uri, overlay);
    }

    @Override
    public void removeOverlay(UIElement overlay) {
        removeOverlay(overlay.getURI());
    }

    @Override
    public void removeOverlay(String uri) {
        AssetUri assetUri = Assets.resolveAssetUri(AssetType.UI_ELEMENT, uri);
        if (assetUri != null) {
            removeOverlay(assetUri);
        }
    }

    @Override
    public void removeOverlay(AssetUri uri) {
        ControlWidget widget = overlays.remove(uri);
        if (widget != null) {
            widget.onClosed();
        }
    }

    @Override
    public void clear() {
        for (ControlWidget overlay : overlays.values()) {
            overlay.onClosed();
        }
        overlays.clear();
        hudScreenLayer.clear();
        for (ControlWidget screen : screens) {
            screen.onClosed();
        }
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
        canvas.processMousePosition(Mouse.getPosition());

        for (UIScreenLayer screen : screens) {
            screen.update(delta);
        }

        for (ControlWidget widget : overlays.values()) {
            widget.update(delta);
        }

        if (autoReloadEnabled) {
            try {
                if (autoReloadWatchService == null) {
                    autoReloadWatchService = FileSystems.getDefault().newWatchService();
                    // TODO dispose watch service properly. As it is only for debugging it does not matter much.
                    autoReloadingScreensUris = Sets.newHashSet();
                    watchedDirectories = Sets.newHashSet();
                    watchKeyToPathMap = Maps.newHashMap();
                    filePathToAssetUriMap = Maps.newHashMap();
                }
                registerCurrentScreensForAutoReloading();
                reloadChangedScreens();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void registerCurrentScreensForAutoReloading() throws URISyntaxException, IOException {
        for (AssetUri uri: screenLookup.keySet()) {
            if (!autoReloadingScreensUris.contains(uri)) {
                autoReloadingScreensUris.add(uri);
                List<URL> urls = context.get(AssetManager.class).getAssetURLs(uri);
                URL firstURL = urls.get(0);
                Path filePath = Paths.get(firstURL.toURI());
                Path directory = filePath.getParent();
                if (!watchedDirectories.contains(directory)) {
                    watchedDirectories.add(directory);
                    WatchKey watchKey = directory.register(autoReloadWatchService, StandardWatchEventKinds.ENTRY_MODIFY,
                            StandardWatchEventKinds.ENTRY_CREATE);
                    watchKeyToPathMap.put(watchKey, directory);
                    logger.info("Watching directory {} for screen ui file changes", directory);
                }

                filePathToAssetUriMap.put(filePath, uri);
            }
        }
    }

    private void reloadChangedScreens() throws InterruptedException {
        while (true) {
            WatchKey key = autoReloadWatchService.poll();
            if (key == null) {
                return;
            }
            Path directory = watchKeyToPathMap.get(key);
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path relativePath = (Path) event.context();
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    logger.warn("Can't keep up with screen changes");
                    continue;
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY
                        || kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    /*
                     * Reason for ENTRY_CREATE watching: Create events get also sent at renames.
                     * Some editors perform a rename when they save to do it atomically
                     */

                    Path fullPath = directory.resolve(relativePath);
                    logger.info("File in folder with screens changed: {}", fullPath);

                    AssetUri uri = filePathToAssetUriMap.get(fullPath);
                    if (uri != null) {

                        UIData uiData = context.get(AssetManager.class).loadAssetData(uri, UIData.class);
                        if (uiData != null) {
                            context.get(AssetManager.class).generateAsset(uri, uiData);

                            closeScreenWithoutEvent(uri);
                            pushScreen(uri);

                            logger.info("Reloaded screen {}", uri);
                        } else {
                            logger.error("Failed to reload screen {}", uri);
                        }
                    }
                }
            }
            key.reset();
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
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
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
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
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
//                event.consume();
                return;
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

    private void prepare(ControlWidget screen) {
        InjectionHelper.inject(screen);
        screen.onOpened();
    }

    @Override
    public void enableAutoReload() {
        this.autoReloadEnabled = true;
    }
}
