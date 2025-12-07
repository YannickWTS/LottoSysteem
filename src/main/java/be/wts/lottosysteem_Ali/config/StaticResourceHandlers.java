package be.wts.lottosysteem_Ali.config;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class StaticResourceHandlers implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/updates/**")
                .addResourceLocations("file:/C:/Server/updates/");
    }
}
