/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.contextMenu;

import java.util.function.Consumer;

/**
 * A single option in a context menu.
 *
 * @param <E> The type of the object passed to the option.
 */
public class ContextMenuOption<E> implements AbstractContextMenuItem {
    /**
     * The name of the option.
     */
    private String name;
    /**
     * A consumer operation.
     */
    private Consumer<E> consumer;
    /**
     * An input object instance.
     */
    private E object;
    /**
     * Whether the option is final (i.e. on selection closes the context menu
     * and triggers the relevant listeners, if any).
     */
    private boolean finalized;

    public ContextMenuOption(String name, Consumer<E> consumer, E object, boolean finalized) {
        this.name = name;
        this.consumer = consumer;
        this.object = object;
        this.finalized = finalized;
    }

    public void select() {
        consumer.accept(object);
    }

    public boolean isFinalized() {
        return finalized;
    }

    /**
     * @return The consumer operation.
     */
    public Consumer<E> getConsumer() {
        return consumer;
    }

    /**
     * @return The input object instance.
     */
    public E getObject() {
        return object;
    }

    @Override
    public String toString() {
        return name;
    }
}
