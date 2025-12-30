package com.shiqu.satoken.controlle;

import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.util.SaResult;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/basic/")
public class HttpBasicController {


    @Operation(summary = "Http Basic 认证", description = "我们访问这个接口时，浏览器会强制弹出一个表单")
    @GetMapping("alter")
    public SaResult test3() {
        SaHttpBasicUtil.check("hello:123456");
        // ... 其它代码
        return SaResult.ok();
    }
}
