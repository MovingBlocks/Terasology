package org.terasology.entitySystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark up methods that can be registered to receive events through the EventSystem
 *
 * These methods should have the form
 * <code>void handlerMethod(EventType event, EntityRef entity)</code>
 * If the method only services a single type of component, then the concrete Component type can be used for the third
 * argument.
 *
 * @author Immortius <immortius@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReceiveEvent {
    /**
     * What components that the entity must have for this method to be invoked
     */
    Class<? extends Component>[] components();
}
