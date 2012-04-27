package org.terasology.entitySystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark up methods that can be registered to receive events through the EventSystem
 *
 * These methods should have the form
 * <code>public void handlerMethod(EventType event, EntityRef entity)</code>
 *
 * @author Immortius <immortius@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReceiveEvent {
    public static final int PRIORITY_CRITICAL = 200;
    public static final int PRIORITY_HIGH = 150;
    public static final int PRIORITY_NORMAL = 100;
    public static final int PRIORITY_LOW = 50;
    public static final int PRIORITY_TRIVIAL = 0;

    /**
     * What components that the entity must have for this method to be invoked
     */
    Class<? extends Component>[] components();
    int priority() default PRIORITY_NORMAL;
}
