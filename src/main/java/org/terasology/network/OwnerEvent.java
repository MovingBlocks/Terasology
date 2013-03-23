package org.terasology.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for events that are run on the owning client of the entity the event is sent to.
 * <p/>
 * If the net owner is null, or a local player the event is run on the server.
 *
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface OwnerEvent {
}
