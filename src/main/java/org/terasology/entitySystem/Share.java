package org.terasology.entitySystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark a component system to be shared through the Core Registry. This is done against the
 * class that is the value of the annotation - it is recommended this should be an interface.
 *
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Share {
    Class<?>[] value();
}
