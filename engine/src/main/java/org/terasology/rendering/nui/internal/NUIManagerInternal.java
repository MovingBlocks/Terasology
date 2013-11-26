/*
 * Copyright 2013 MovingBlocks
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
import com.google.common.collect.Queues;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.classMetadata.ClassLibrary;
import org.terasology.classMetadata.DefaultClassLibrary;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.Mouse;
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.network.ClientComponent;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIElement;
import org.terasology.rendering.nui.UIScreen;

import java.lang.reflect.Field;
import java.util.Deque;

/**
 * @author Immortius
 */
public class NUIManagerInternal extends BaseComponentSystem implements NUIManager {

    private static final Logger logger = LoggerFactory.getLogger(NUIManagerInternal.class);

    private Deque<UIScreen> screens = Queues.newArrayDeque();
    private CanvasInternal canvas = new LwjglCanvas(this);
    private ClassLibrary<UIElement> elementsLibrary;
    private UIElement focus;

    public void refreshElementsLibrary() {
        elementsLibrary = new DefaultClassLibrary<>(CoreRegistry.get(ReflectFactory.class), CoreRegistry.get(CopyStrategyLibrary.class));
        for (Module module : CoreRegistry.get(ModuleManager.class).getActiveCodeModules()) {
            for (Class<? extends UIElement> elementType : module.getReflections().getSubTypesOf(UIElement.class)) {
                if (!elementType.isInterface()) {
                    elementsLibrary.register(new SimpleUri(module.getId(), elementType.getSimpleName()), elementType);
                }
            }
        }
    }

    @Override
    public void pushScreen(UIScreen screen) {
        inject(screen);
        screen.initialise();
        screens.push(screen);
    }

    @Override
    public void popScreen() {
        if (!screens.isEmpty()) {
            screens.pop();
        }
    }

    @Override
    public void setScreen(UIScreen screen) {
        screens.clear();
        inject(screen);
        screen.initialise();
        screens.push(screen);
    }

    @Override
    public void closeScreens() {
        screens.clear();
    }

    public void render() {
        canvas.preRender();
        if (!screens.isEmpty()) {
            canvas.setSkin(screens.peek().getSkin());
            canvas.drawElement(screens.peek(), canvas.getRegion());
        }
        canvas.postRender();
    }

    public void update(float delta) {
        canvas.processMousePosition(Mouse.getPosition());

        for (UIScreen screen : screens) {
            screen.update(delta);
        }
    }

    @Override
    public ClassLibrary<UIElement> getElementMetadataLibrary() {
        return elementsLibrary;
    }

    @Override
    public void setFocus(UIElement widget) {
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
    public UIElement getFocus() {
        return focus;
    }

    /*
      The following events will capture the mouse and keyboard inputs. They have high priority so the GUI will
      have first pick of input
    */

    //mouse button events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseButtonEvent(MouseButtonEvent event, EntityRef entity) {
        if (event.isDown()) {
            canvas.processMouseClick(event.getButton(), Mouse.getPosition());
        } else {
            canvas.processMouseRelease(event.getButton(), Mouse.getPosition());
        }
    }

    //mouse wheel events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void mouseWheelEvent(MouseWheelEvent event, EntityRef entity) {
    }

    //raw input events
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void keyEvent(KeyEvent event, EntityRef entity) {
    }

    //bind input events (will be send after raw input events, if a bind button was pressed and the raw input event hasn't consumed the event)
    @ReceiveEvent(components = ClientComponent.class, priority = EventPriority.PRIORITY_HIGH)
    public void bindEvent(BindButtonEvent event, EntityRef entity) {
    }

    private void inject(Object object) {
        for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(In.class))) {
            Object value = CoreRegistry.get(field.getType());
            if (value != null) {
                try {
                    field.setAccessible(true);
                    field.set(object, value);
                } catch (IllegalAccessException e) {
                    logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                }
            }
        }
    }

}
