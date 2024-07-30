package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.KycBankDTO;
import org.uu.common.pay.dto.KycPartnersDTO;
import org.uu.common.pay.req.KycBankIdReq;
import org.uu.common.pay.req.KycBankListPageReq;
import org.uu.common.pay.req.KycBankReq;
import org.uu.manager.api.KycBankClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
 * <p>
 * kyc信息表 前端控制器
 * </p>
 *
 * @author
 * @since 2024-04-26
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping({"/api/v1/kycBankAdmin", "/kycBankAdmin"})
@Api(description = "KYC-Bank")
public class KycBankController {

    private final KycBankClient kycBankClient;

    /**
     * 获取KYC Partner列表
     *
     * @return {@link RestResult}<{@link List}<{@link KycPartnersDTO}>>
     */
    @PostMapping("/listPage")
    @ApiOperation(value = "获取KYC Bank列表")
    public RestResult<List<KycBankDTO>> listPage(@RequestBody @ApiParam KycBankListPageReq req) {
        return kycBankClient.listPage(req);
    }

    /**
     * 获取KYC BANK CODE
     *
     */
    @PostMapping("/getBankCodeList")
    @ApiOperation(value = "获取KYC BANK CODE")
    public RestResult<List<String>> getBankCodeList() {
        return kycBankClient.getBankCodeList();
    }


    /**
     * 删除
     *
     * @return RestResult
     */
    @PostMapping("/deleteKycBank")
    @ApiOperation(value = "删除")
    public RestResult deleteKycBank(@RequestBody @ApiParam KycBankIdReq req) {
        return kycBankClient.deleteKycBank(req);
    }

    @PostMapping("/addKycBank")
    @ApiOperation(value = "新增")
    public RestResult<KycBankDTO> addKycBank(@RequestBody @ApiParam KycBankReq req) {
        return kycBankClient.addKycBank(req);
    }

    @PostMapping("/updateKycBank")
    @ApiOperation(value = "修改")
    public RestResult<KycBankDTO> updateKycBank(@RequestBody @ApiParam KycBankReq req) {
        return kycBankClient.updateKycBank(req);
    }

}
