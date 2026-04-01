package com.seckill.config;

import com.seckill.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",            // 登录接口
                        "/register",         // 注册接口
                        "/index.html",       // 首页
                        "/",                 // 根路径
                        "/css/**",           // 静态资源css
                        "/js/**",            // 静态资源js
                        "/favicon.ico"       // 网站图标
                );
    }
}