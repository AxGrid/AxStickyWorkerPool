package com.axgrid.worker;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan(basePackages = "com.axgrid.worker")
public class AxStickyWorkerPoolConfiguration {
}
