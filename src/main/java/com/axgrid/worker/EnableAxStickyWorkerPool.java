package com.axgrid.worker;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AxStickyWorkerPoolConfiguration.class})
public @interface EnableAxStickyWorkerPool {
}
