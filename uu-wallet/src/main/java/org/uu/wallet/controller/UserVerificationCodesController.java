package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.UserVerificationCodeslistPageDTO;
import org.uu.common.pay.req.UserTextMessageReq;
import org.uu.wallet.service.IUserVerificationCodesService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * 用户验证码记录表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-01-20
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = {"/api/v1/userVerificationCodes", "/userVerificationCodes"})
@Api(description = "用户验证码控制器")
@ApiIgnore
public class UserVerificationCodesController {

    private final IUserVerificationCodesService userVerificationCodesService;

    @PostMapping("/listPage")
    @ApiOperation(value = "获取用户验证码列表")
    public RestResult<List<UserVerificationCodeslistPageDTO>> listPage(@RequestBody @ApiParam UserTextMessageReq userTextMessageReq) {
        PageReturn<UserVerificationCodeslistPageDTO> payConfigPage = userVerificationCodesService.listPage(userTextMessageReq);
        return RestResult.page(payConfigPage);
    }
}

