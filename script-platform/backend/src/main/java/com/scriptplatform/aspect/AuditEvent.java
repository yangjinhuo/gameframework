package com.scriptplatform.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark a controller method that should be recorded in audit log.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditEvent {
    /**
     * event type, e.g. USER_CREATE.
     */
    String type();

    /**
     * descriptive remark.
     */
    String remark() default "";
}
