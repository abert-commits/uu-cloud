package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.KycPartnersDTO;
import org.uu.common.pay.req.KycPartnerIdReq;
import org.uu.common.pay.req.KycPartnerListPageReq;
import org.uu.common.pay.req.KycPartnerMemberReq;
import org.uu.common.pay.req.KycPartnerUpdateReq;
import org.uu.wallet.entity.KycPartners;
import org.uu.wallet.service.IKycPartnersService;
import org.springframework.web.bind.annotation.*;
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
@RequestMapping({"/api/v1/kycPartners", "/kycPartners"})
@Api(description = "KYC-Partners")
@ApiIgnore
public class KycPartnersController {

    private final IKycPartnersService kycPartnersService;

    /**
     * 获取KYC Partner列表
     *
     * @return {@link RestResult}<{@link List}<{@link KycPartnersDTO}>>
     */
    @PostMapping("/listPage")
    @ApiOperation(value = "获取KYC Partner列表")
    public RestResult<List<KycPartnersDTO>> listPage(@RequestBody @ApiParam KycPartnerListPageReq kycPartnerListPageReq) {
        PageReturn<KycPartnersDTO> kycPartnersDTOPageReturn = kycPartnersService.listPage(kycPartnerListPageReq);
        return RestResult.page(kycPartnersDTOPageReturn);
    }


    /**
     * 删除
     *
     * @return RestResult
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除")
    public RestResult listPage(@RequestBody @ApiParam KycPartnerIdReq req) {
        boolean delete = kycPartnersService.delete(req);
        return delete ? RestResult.ok() : RestResult.failed();
    }


    /**
     * 连接kyc
     */
    @GetMapping("/link")
    @ApiOperation(value = "刷新kyc连接")
    public void kycPartnersLink(){
        kycPartnersService.kycPartnersLink();
    }


    /**
     * 更新kyc收款相关信息
     * @param req req
     * @return {@link RestResult}
     */
    @PostMapping("/updateKycPartner")
    @ApiOperation(value = "更新kycPartner")
    public RestResult updateKycPartner(@RequestBody @ApiParam KycPartnerUpdateReq req){
        KycPartners kycPartners = kycPartnersService.updateKycPartner(req);
        return ObjectUtils.isNotEmpty(kycPartners) ? RestResult.ok(kycPartners) : RestResult.failed();
    }

    @PostMapping("/getKycPartnerByMemberId")
    @ApiOperation(value = "获取会员的kyc信息")
    public RestResult<List<KycPartnersDTO>> getKycPartnerByMemberId(@RequestBody @ApiParam KycPartnerMemberReq req){
        List<KycPartnersDTO> kycPartnerByMemberId = kycPartnersService.getKycPartnerByMemberId(req);
        return RestResult.ok(kycPartnerByMemberId);
    }


}
