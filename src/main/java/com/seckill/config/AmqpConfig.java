package com.seckill.config;

import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Collections;

@Configuration
public class AmqpConfig {

    @Bean
    public SimpleMessageConverter messageConverter() {
        SimpleMessageConverter converter = new SimpleMessageConverter();
        // 信任 com.seckill.mq 包下的所有类
        converter.setAllowedListPatterns(Collections.singletonList("com.seckill.mq.*"));
        return converter;
    }
}