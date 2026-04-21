package ru.ssau.cafe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();
        logger.info("WebConfig: Configuring uploads handler");
        logger.info("  Resource pattern: /uploads/**");
        logger.info("  File location: {}", uploadDir);
        logger.info("  Absolute path: {}", Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath());
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadDir)
                .setCachePeriod(3600);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        logger.info("WebConfig: Configuring SPA fallback routes");
        registry.addViewController("/{spring:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:(?!uploads|api|assets|styles|scripts)[\\w-]+}/**")
                .setViewName("forward:/index.html");
    }
}
