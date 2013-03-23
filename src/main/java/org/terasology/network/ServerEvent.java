package org.terasology.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for events that are consumed by the server. If the event is sent on a client, then it will be replicated
 * to the server and actioned there.
 *
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ServerEvent {
    boolean lagCompensate() default false;
}
