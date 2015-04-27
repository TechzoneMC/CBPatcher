package net.techcable.cbpatcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inject all methods in the annotated class into the specified class
 *
 * If the method or field in question already exists it will be overridden
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Inject {
    /**
     * Return the class to inject this method into
     *
     * @return the class to inject this method into
     */
    public Class<?> injectInto();
}
