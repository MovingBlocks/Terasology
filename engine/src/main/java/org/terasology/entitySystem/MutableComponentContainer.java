/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.entitySystem;

import java.util.Optional;
import java.util.function.Function;

/**
 * An extension to allow for mutation of the components contained in this {@link ComponentContainer}.
 */
public interface MutableComponentContainer extends ComponentContainer {

    /**
     * Adds a component. If this already has a component of the same class it is replaced.
     *
     * @param component component to add (may override existing component)
     * @see MutableComponentContainer#saveComponent(Component)
     * @see MutableComponentContainer#addOrSaveComponent(Component)
     */
    <T extends Component> T addComponent(T component);

    /**
     * Removes a component of the given class, if it exists.
     *
     * @param componentClass class of the component to remove
     */
    void removeComponent(Class<? extends Component> componentClass);

    /**
     * Saves changes made to a component.
     * <p>
     * If this container does not hold a component if the component's class yet the component is <strong>not</strong>
     * added.
     *
     * @param component component to save changes for
     * @see MutableComponentContainer#addComponent(Component)
     * @see MutableComponentContainer#addOrSaveComponent(Component)
     */
    void saveComponent(Component component);

    /**
     * Saves changes made to a component, or add the component if not yet present.
     * <p>
     * This is equivalent to the following:
     * <pre>
     * {@code
     * if (container.hasComponent(component.getClass())) {
     *     container.saveComponent(component);
     * } else {
     *     container.addComponent(component);
     * }
     * }
     * </pre>
     *
     * @param component the component to save or add
     * @see MutableComponentContainer#addComponent(Component)
     * @see MutableComponentContainer#saveComponent(Component)
     */
    default void addOrSaveComponent(Component component) {
        if (hasComponent(component.getClass())) {
            saveComponent(component);
        } else {
            addComponent(component);
        }
    }

    /**
     * Perform an in-place update on the component if the specified component class is present.
     * <p>
     * If this contains a component of the given class the transformation function 'f' is applied to that component.
     * Otherwise, function 'f' is <strong>not</strong> called.
     * <p>
     * This is a functional convenience method to abstract over retrieving and saving a component for modification.
     * Given an update function {@code ItemComponent updateItem(ItemComponent component)} that takes a component and
     * returns the modified component the component update can be written as concise as:
     * <pre>
     *     {@code
     *     EntityRef item = entityManager.create("CoreAdvancedAssets:door");
     *     item.updateComponent(ItemComponent.class, MyItemHandler::updateItem);
     *     }
     * </pre>
     * The same effect can be achieved by extracting and saving the component manually:
     * <pre>
     *     {@code
     *     EntityRef item = entityManager.create("CoreAdvancedAssets:door");
     *     ItemComponent doorItemComp = item.getComponent(ItemComponent.class);
     *     updateItem(doorItemComp);
     *     item.saveComponent(doorItemComp);
     *     }
     * </pre>
     * <p>
     * If the transformation function {@code f} returns {@code null} the component will be removed from this container.
     * This can be useful in cases where updating the component leads to a state where it becomes obsolete and should be
     * removed.
     *
     * <p>
     * To create or update a component on this container, see {@link MutableComponentContainer#upsertComponent(Class,
     * Function)}
     *
     * @param componentClass component class to update
     * @param f transformation function used to compute the updated component; returning {@code null} removes
     *         the component
     * @param <T> type of the component to update
     * @see MutableComponentContainer#upsertComponent(Class, Function)
     */
    default <T extends Component> void updateComponent(Class<T> componentClass, Function<T, T> f) {
        if (hasComponent(componentClass)) {
            T component = f.apply(getComponent(componentClass));
            if (component == null) {
                removeComponent(componentClass);
            } else {
                saveComponent(component);
            }

        }
    }

    /**
     * Perform an in-place update or add (insert) a component to this container.
     * <p>
     * If this container contains a component of given class the transformation function 'f' is applied to an Optional
     * with the current component. Otherwise, 'f' is called with an empty Optional.
     * <p>
     * This is a functional convenience method to abstract over retrieving/creating and saving a component for
     * modification. It can be used to conditionally create and modify components of this container. Given an update
     * function {@code ItemComponent updateItem(ItemComponent component)} that takes a component and returns the
     * modified component the following ensures that the container {@code item} has an {@code ItemComponent} in the
     * desired configuration.
     * <pre>
     *     {@code
     *     EntityRef item = entityManager.create("CoreAdvancedAssets:door");
     *     item.upsertComponent(ItemComponent.class,
     *          maybeComponent -> updateItem(maybeComponent.orElse(new ItemComponent())));
     *     }
     * </pre>
     * The same effect can achieved by extracting, creating the component if necessary, and saving it manually:
     * <pre>
     *     {@code
     *     EntityRef item = entityManager.create("CoreAdvancedAssets:door");
     *     ItemComponent doorItemComp = item.getComponent(ItemComponent.class);
     *     if (doorItemComp == null) {
     *         doorItemComp = new ItemComponent();
     *     }
     *     updateItem(doorItemComp);
     *     item.saveComponent(doorItemComp);
     *     }
     * </pre>
     * <p>
     * If the transformation function {@code f} returns {@code null} no component will be added to this container.
     *
     * <p>
     * To perform an in-place update only if the component is present, see
     * {@link MutableComponentContainer#updateComponent(Class,
     * Function)}
     *
     * @param componentClass component class to update or insert
     * @param f transformation function used to compute the new or updated component; may return {@code null}
     * @param <T> type of the component to insert or update
     * @see MutableComponentContainer#updateComponent(Class, Function)
     */
    default <T extends Component> void upsertComponent(Class<T> componentClass, Function<Optional<T>, T> f) {
        T component = f.apply(Optional.ofNullable(getComponent(componentClass)));
        if (component != null) {
            addOrSaveComponent(component);
        }

    }
}
