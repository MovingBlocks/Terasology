// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.contextMenu;

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
    private final String name;
    /**
     * A consumer operation.
     */
    private final Consumer<E> consumer;
    /**
     * An input object instance.
     */
    private final E object;
    /**
     * Whether the option is final (i.e. on selection closes the context menu
     * and triggers the relevant listeners, if any).
     */
    private final boolean finalized;

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
