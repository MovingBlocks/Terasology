package org.terasology.network;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type or field to be replicated. For events, fields default to replicated so this isn't needed
 *
 * @author Immortius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Replicate {
    public ReplicateType value() default ReplicateType.SERVER_TO_CLIENT;

    public boolean initialOnly() default false;
}
