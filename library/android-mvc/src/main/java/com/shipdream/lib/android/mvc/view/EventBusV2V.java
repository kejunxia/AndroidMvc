package com.shipdream.lib.android.mvc.view;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Indicates the annotated event bus is for communication among views. Events through the
 * event bus annotated by this annotation should be posted and received on UI thread.
 */
@Qualifier
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface EventBusV2V {
}
