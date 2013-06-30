package org.terasology.entitySystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation will prevent the automatic registration of event and component classes. It is intended for use
 * by tests.
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DoNotAutoRegister {
}
