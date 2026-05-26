package ru.ssau.cafe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        logger.info("WebConfig: Configuring SPA fallback routes");
        registry.addViewController("/{spring:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:(?!api|assets|styles|scripts)[\\w-]+}/**")
                .setViewName("forward:/index.html");
    }
}
