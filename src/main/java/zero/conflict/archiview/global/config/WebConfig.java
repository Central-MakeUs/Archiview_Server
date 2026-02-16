package zero.conflict.archiview.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "https://archiview.space",
                        "https://api.archiview.space",
                        "https://appleid.apple.com",
                        "http://localhost:3000",
                        "http://localhost:8080")
                .allowedOriginPatterns(
                        "http://192.168.*.*:*",
                        "https://192.168.*.*:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConverter) {
                List<MediaType> mediaTypes = new ArrayList<>(jacksonConverter.getSupportedMediaTypes());
                if (!mediaTypes.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                    mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                    jacksonConverter.setSupportedMediaTypes(mediaTypes);
                }
            }
        }
    }
}
