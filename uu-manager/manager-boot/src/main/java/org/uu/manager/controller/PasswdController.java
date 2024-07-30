package org.uu.manager.controller;


import lombok.RequiredArgsConstructor;
import org.uu.common.core.result.RestResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/passwd")
@RequiredArgsConstructor
@ApiIgnore
public class PasswdController {
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/genPasswd/{passwd}")
    public RestResult<String> genPasswd(@PathVariable("passwd") String passwd) {
        return RestResult.ok(passwordEncoder.encode(passwd));
    }
}
