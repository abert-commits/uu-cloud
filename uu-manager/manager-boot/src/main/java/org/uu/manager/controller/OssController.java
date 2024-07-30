package org.uu.manager.controller;


import com.alibaba.cloud.commons.lang.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.manager.oss.OssService;
import org.uu.manager.req.GeneratePresignedUrlReq;
import org.uu.manager.vo.GeneratePresignedUrlVo;
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
@Api(description = "文件上传控制器")
@Validated
@Slf4j
public class OssController {

    @Autowired
    private OssService ossService;

    @Value("${oss.baseUrl}")
    private String baseUrl;

    @PostMapping("/generatePresignedUrl")
    @ApiOperation(value = "生成上传凭证")
    public RestResult<GeneratePresignedUrlVo> checkUpiIdDuplicate(@RequestBody @ApiParam @Valid GeneratePresignedUrlReq generatePresignedUrlReq) {

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
