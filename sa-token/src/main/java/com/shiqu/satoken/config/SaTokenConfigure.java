package com.shiqu.satoken.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    // 注册 Sa-Token 的拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册路由拦截器，自定义认证规则
        registry.addInterceptor(new SaInterceptor(handler -> {
            String path = SaHolder.getRequest().getRequestPath();
            String saToken = SaHolder.getRequest().getHeader("saToken");
            log.info("当前请求路径：{}。token：{}", path, saToken);

            // 登录校验 -- 拦截所有路由，并排除/user/doLogin 用于开放登录
            SaRouter.match("/api/")
                .notMatch("/api/user/doLogin","/api/basic/**")
                .check(r -> StpUtil.checkLogin());

            // 角色校验 -- 拦截以 admin 开头的路由，必须具备 admin 角色或者 super-admin 角色才可以通过认证
            SaRouter.match("/api/admin/**", r -> StpUtil.checkRoleOr("admin", "super-admin"));

            // 权限校验 -- 不同模块校验不同权限
            SaRouter.match("/api/admin/**", r -> StpUtil.checkPermission("admin"));

        })).addPathPatterns("/**");
    }
}
