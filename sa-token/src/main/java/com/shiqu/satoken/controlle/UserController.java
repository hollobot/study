package com.shiqu.satoken.controlle;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/user/")
public class UserController {

    // 测试登录，浏览器访问： http://localhost:8081/user/doLogin?username=zhang&password=123456
    @Operation(summary = "测试登录", description = "sa-token 模拟登录鉴权")
    @PostMapping("doLogin")
    public SaResult doLogin(
        @Parameter(description = "账号ID",example = "hello") @RequestParam String name,
        @Parameter(description = "密码",example = "123456")@RequestParam String pwd)
    {
        // 第一步：比对前端提交的账号名称、密码
        if ("hello".equals(name) && "123456".equals(pwd)) {
            // 第二步：根据账号id，进行登录
            StpUtil.login(10001);
            return SaResult.ok("登录成功");
        }
        return SaResult.error("登录失败");
    }

    // 查询登录状态，浏览器访问： http://localhost:8081/user/isLogin
    @Operation(summary = "测试是否登录", description = "测试是否登录")
    @GetMapping("isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    @Operation(summary = "查询登录信息", description = "查询登录信息")
    @GetMapping("loginInfo")
    public Object loginInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenName", StpUtil.getTokenName());
        map.put("tokenValue", StpUtil.getTokenValue());
        map.put("token to id", StpUtil.getLoginIdByToken(StpUtil.getTokenValue()));
        map.put("token TTL", StpUtil.getTokenTimeout());
        map.put("tokenInfo", StpUtil.getTokenInfo());
        return map;
    }

    /**
     * @param device
     *      * "PC"       // 电脑端
     *      * "APP"      // 手机 APP
     *      * "WAP"      // 手机浏览器/H5
     *      * "WEB"      // 网页端
     *      * "MINI"     // 小程序
     *      * "IOS"      // iOS 设备
     *      * "ANDROID"  // Android 设备
     * @return
     */
    @Operation(summary = "踢人下线", description = "踢人下线")
    @PostMapping("logout")
    public Object logout(
        @Parameter(description = "操作类型：1-踢下线，其他-注销下线",example = "1") @RequestParam(required = false) String type,
        @Parameter(description = "账号ID",example = "10001") @RequestParam(required = false) String id,
        @Parameter(description = "设备标识",example = "PC") @RequestParam(required = false) String device,
        @Parameter(description = "Token值") @RequestParam(required = false) String token) {

        if (Objects.nonNull(id) && Objects.nonNull(device)) {
            if ("1".equals(type)) {
                StpUtil.kickout(id, device); // 将指定账号指定端踢下线
                return "ok";
            }
            StpUtil.logout(id, device);      // 强制指定账号指定端注销下线
            return "ok";
        }

        if (Objects.nonNull(id)) {
            if ("1".equals(type)) {
                StpUtil.kickout(id);         // 将指定账号踢下线
                return "ok";
            }
            StpUtil.logout(id);              // 强制指定账号注销下线
            return "ok";
        }

        if (Objects.nonNull(token)) {
            if ("1".equals(type)) {
                StpUtil.kickoutByTokenValue(token); // 将指定 Token 踢下线
                return "ok";
            }
            StpUtil.logoutByTokenValue(token);      // 强制指定 Token 注销下线
            return "ok";
        }

        return "ok";
    }

}
