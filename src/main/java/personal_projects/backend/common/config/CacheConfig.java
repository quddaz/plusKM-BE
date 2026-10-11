package personal_projects.backend.common.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    public static final String EMERGENCY_BEDS_CACHE = "emergencyBeds";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(EMERGENCY_BEDS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder().maximumSize(1));
        return cacheManager;
    }
}
