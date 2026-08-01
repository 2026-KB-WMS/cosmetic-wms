package com.kb.ordering.global.config;

import com.kb.common.error.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(GlobalExceptionHandler.class)
public class WebConfig {
}