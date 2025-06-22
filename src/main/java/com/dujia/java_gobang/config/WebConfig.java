package com.dujia.java_gobang.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginConfig loginConfig;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginConfig).addPathPatterns("/**")
                .excludePathPatterns("/login",
                        "/register",
                        "/css/**",
                        "/js/**",
                        "/pic/**",
                        "/blog-editormd/**",
                        "/*.html",
                        "/favicon.ico");
    }
}
