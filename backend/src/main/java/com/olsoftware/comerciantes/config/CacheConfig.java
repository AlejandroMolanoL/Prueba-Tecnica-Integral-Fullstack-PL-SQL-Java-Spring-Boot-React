package com.olsoftware.comerciantes.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        // El constructor sin argumentos establece dynamic = true en Spring.
        // Pre-creamos las cachés del catálogo para asegurar su disponibilidad
        // inmediata.
        cacheManager.getCache("departamentos");
        cacheManager.getCache("municipios");
        cacheManager.getCache("municipiosPorNombre");
        return cacheManager;
    }
}
