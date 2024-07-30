package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.KycApprovedOrderDTO;
import org.uu.common.pay.req.KycApprovedOrderListPageReq;
import org.uu.manager.api.KycApprovedOrderFeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 通过 KYC 验证完成的订单表 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-05-03
 */
@RestController
@RequestMapping({"/api/v1/kycApprovedOrderAdmin", "/kycApprovedOrderAdmin"})
@Api(description = "验证完成的订单控制器")
public class KycApprovedOrderController {
    @Resource
    KycApprovedOrderFeignClient kycApprovedOrderFeignClient;

    @PostMapping("/listPage")
    @ApiOperation(value = "验证完成订单列表")
    public RestResult<List<KycApprovedOrderDTO>> listPage(@RequestBody @ApiParam KycApprovedOrderListPageReq req){
        return kycApprovedOrderFeignClient.listPage(req);
    }
}
