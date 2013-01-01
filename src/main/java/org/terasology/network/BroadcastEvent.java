package org.terasology.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for events that are broadcast to all clients (in addition to running on the server).
 * These events must originate from the server in order to be broadcast.
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BroadcastEvent {
}
