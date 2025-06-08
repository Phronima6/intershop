package ru.yandex.practicum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.yandex.practicum.cache.ItemDetailCache;
import ru.yandex.practicum.cache.ItemListCache;

@Configuration
public class RedisConfig {
    
    @Value("${cache.item.ttl:3600}")
    private long itemCacheTtl;

    @Bean
    public ReactiveRedisTemplate<String, ItemDetailCache> itemDetailRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ItemDetailCache> valueSerializer = 
                new Jackson2JsonRedisSerializer<>(ItemDetailCache.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, ItemDetailCache> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, ItemDetailCache> context = 
                builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, ItemListCache> itemListRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<ItemListCache> valueSerializer = 
                new Jackson2JsonRedisSerializer<>(ItemListCache.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, ItemListCache> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, ItemListCache> context = 
                builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

}