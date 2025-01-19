package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.interceptor.MockDataInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private MockDataInterceptor mockDataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mockDataInterceptor).addPathPatterns("/api/**");
    }
}
