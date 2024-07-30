package org.uu.auth.security;

import org.uu.common.core.utils.GoogleAuthenticatorUtil;
import org.iherus.codegen.qrcode.SimpleQrcodeGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/oauth/google")
public class GoogleAuthCodeController {

    /**
     * 生成 Google 密钥，两种方式任选一种
     */
    @GetMapping("getSecret")
    public String getSecret() {
        return GoogleAuthenticatorUtil.getSecretKey();
    }

    /**
     * 生成二维码，APP直接扫描绑定，两种方式任选一种
     */
    @GetMapping("getQrcode")
    public void getQrcode(String name, HttpServletResponse response) throws Exception {
        // 生成二维码内容
        String qrCodeText = GoogleAuthenticatorUtil.getQrCodeText(GoogleAuthenticatorUtil.getSecretKey(), name, "");
        // 生成二维码输出
        new SimpleQrcodeGenerator().generate(qrCodeText).toStream(response.getOutputStream());
    }

    /**
     * 获取code
     */
    @GetMapping("getCode")
    public String getCode(String secretKey) {
        return GoogleAuthenticatorUtil.getCode(secretKey);
    }

    /**
     * 验证 code 是否正确
     */
    @GetMapping("checkCode")
    public String checkCode(String secret, String code) {
        boolean b = GoogleAuthenticatorUtil.checkCode(secret, Long.parseLong(code), System.currentTimeMillis());
        if (b) {
            return "success";
        }
        return "error";
    }
}
