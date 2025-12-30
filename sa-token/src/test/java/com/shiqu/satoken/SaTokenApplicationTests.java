package com.shiqu.satoken;

import cn.dev33.satoken.secure.SaBase32Util;
import cn.dev33.satoken.secure.SaBase64Util;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.secure.totp.SaTotpUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SaTokenApplicationTests {

    @Test
    void AES() {
        // 定义秘钥和明文
        String key = "123456";
        String text = "Sa-Token 一个轻量级java权限认证框架";

        // 加密
        String ciphertext = SaSecureUtil.aesEncrypt(key, text);
        System.out.println("AES加密后：" + ciphertext);

        // 解密
        String text2 = SaSecureUtil.aesDecrypt(key, ciphertext);
        System.out.println("AES解密后：" + text2);

    }

    @Test
    void base64Text() {
        // 文本
        String text = "Sa-Token 一个轻量级java权限认证框架";

        // 使用Base64编码
        String base64Text = SaBase64Util.encode(text);
        System.out.println("Base64编码后：" + base64Text);

        // 使用Base64解码
        String text2 = SaBase64Util.decode(base64Text);
        System.out.println("Base64解码后：" + text2);
    }

    @Test
    void base32Text () {
        // 文本
        String text = "Sa-Token 一个轻量级java权限认证框架";

        // 使用Base32编码
        String base32Text = SaBase32Util.encode(text);
        System.out.println("Base32编码后：" + base32Text);

        // 使用Base32解码
        String text2 = SaBase32Util.decode(base32Text);
        System.out.println("Base32解码后：" + text2);

    }

    @Test
    void totp () {
        // 1、生成密钥
        String secretKey = SaTotpUtil.generateSecretKey();
        System.out.println("TOTP 秘钥: " + secretKey);

        // 2、生成扫码字符串
        String qeString = SaTotpUtil.generateGoogleSecretKey("zhangsan", secretKey);
        System.out.println("扫码字符串: " + qeString);

        // 3、计算当前 TOTP 码
        String code = SaTotpUtil.generateTOTP(secretKey);
        System.out.println("当前时间戳对应的 TOTP 码: " + code);

        // 4、验证用户输入
        boolean isValid = SaTotpUtil.validateTOTP(secretKey, code, 1);
        System.out.println("验证结果: " + isValid);

    }

}
