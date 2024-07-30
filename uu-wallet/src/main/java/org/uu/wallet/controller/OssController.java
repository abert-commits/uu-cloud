package org.uu.wallet.controller;


import com.alibaba.cloud.commons.lang.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.wallet.entity.MemberInfo;
import org.uu.wallet.oss.OssService;
import org.uu.wallet.req.GeneratePresignedUrlReq;
import org.uu.wallet.service.IMemberInfoService;
import org.uu.wallet.vo.GeneratePresignedUrlVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/oss")
@Api(description = "前台-文件上传控制器")
@Validated
@Slf4j
public class OssController {

    @Autowired
    private OssService ossService;

    @Autowired
    private IMemberInfoService memberInfoService;

    @Value("${oss.baseUrl}")
    private String baseUrl;

    @PostMapping("/generatePresignedUrl")
    @ApiOperation(value = "前台-生成上传凭证")
    public RestResult<GeneratePresignedUrlVo> checkUpiIdDuplicate(@RequestBody @ApiParam @Valid GeneratePresignedUrlReq generatePresignedUrlReq) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("生成上传凭证失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        String signedUrl = ossService.generatePresignedUrl(generatePresignedUrlReq.getObjectName(), generatePresignedUrlReq.getContentType());

        if (StringUtils.isEmpty(signedUrl)){
            log.error("生成上传凭证失败: 生成 signedUrl 失败");
            return RestResult.failure(ResultCode.UPLOAD_CREDENTIALS_GENERATION_FAILED);
        }

        GeneratePresignedUrlVo generatePresignedUrlVo = new GeneratePresignedUrlVo();
        generatePresignedUrlVo.setSignedUrl(signedUrl);
        generatePresignedUrlVo.setBaseUrl(baseUrl);
        return RestResult.ok(generatePresignedUrlVo);
    }
}
