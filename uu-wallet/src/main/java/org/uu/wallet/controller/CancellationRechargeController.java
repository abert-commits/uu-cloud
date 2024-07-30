package org.uu.wallet.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import jodd.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;

import org.uu.common.pay.dto.CancellationRechargeDTO;
import org.uu.common.pay.req.CancellationRechargeAddReq;
import org.uu.common.pay.req.CancellationRechargeIdReq;
import org.uu.common.pay.req.CancellationRechargePageListReq;
import org.uu.common.pay.req.CancellationRechargeReq;
import org.uu.wallet.entity.CancellationRecharge;


import org.uu.wallet.service.ICancellationRechargeService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

    import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Slf4j
    @RequiredArgsConstructor
    @RestController
    @Api(description = "充值订单取消原因控制器")
    @RequestMapping(value = {"/api/v1/cancellationRecharge", "/cancellationRecharge"})
    @ApiIgnore
    public class CancellationRechargeController {
        private final ICancellationRechargeService cancellationRechargeService;

        @PostMapping("/listpage")
        @ApiOperation(value = "充值订单取消原因列表")
        public RestResult<List<CancellationRechargeDTO>> listpage(@RequestBody @ApiParam CancellationRechargePageListReq req) {
            PageReturn<CancellationRechargeDTO> payConfigPage = cancellationRechargeService.listPage(req);
            return RestResult.page(payConfigPage);
        }

        @PostMapping("/create")
        @ApiOperation(value = "新增")
        public RestResult<CancellationRechargeDTO> create(@RequestBody @ApiParam CancellationRechargeAddReq cancellationRechargeReq) {
            try {
                CancellationRecharge cancellationRecharge = new CancellationRecharge();
                BeanUtils.copyProperties(cancellationRechargeReq, cancellationRecharge);
                cancellationRechargeService.save(cancellationRecharge);
                CancellationRechargeDTO cancellationRechargeDTO = new CancellationRechargeDTO();
                BeanUtils.copyProperties(cancellationRecharge,cancellationRechargeDTO);
                return RestResult.ok(cancellationRechargeDTO);
                } catch (Exception e) {
                    e.printStackTrace();
                    return RestResult.failed("创建记录失败");
                }
            }

            @PostMapping("/update")
            @ApiOperation(value = "更新")
            public RestResult<CancellationRechargeDTO> update(@RequestBody @ApiParam CancellationRechargeReq cancellationRechargeReq) {
                try {
                    CancellationRecharge cancellationRecharge = new CancellationRecharge();
                    BeanUtils.copyProperties(cancellationRechargeReq, cancellationRecharge);
                    cancellationRechargeService.updateById(cancellationRecharge);
                    CancellationRechargeDTO cancellationRechargeDTO = new CancellationRechargeDTO();
                    BeanUtils.copyProperties(cancellationRecharge,cancellationRechargeDTO);
                    return RestResult.ok(cancellationRechargeDTO);
                }catch(Exception e){
                    e.printStackTrace();
                    return RestResult.failed("修改记录失败");
                }
            }


        @PostMapping("/delete")
        @ApiOperation(value = "删除")
        public RestResult delete(@RequestBody @ApiParam CancellationRechargeIdReq req) {
            try {
                CancellationRecharge cancellationRecharge = new CancellationRecharge();
                BeanUtils.copyProperties(req, cancellationRecharge);
                cancellationRechargeService.removeById(cancellationRecharge);
                return RestResult.ok("删除成功");
            }catch(Exception e){
                e.printStackTrace();
                return RestResult.failed("删除记录失败");
            }
        }


        @PostMapping("/getInfo")
        @ApiOperation(value = "详情")
        public RestResult<CancellationRechargeDTO> get(@RequestBody @ApiParam CancellationRechargeReq cancellationRechargeReq) {
            try {

               // BeanUtils.copyProperties(cancellationRechargeReq, cancellationRecharge);
                CancellationRecharge cancellationRecharge =cancellationRechargeService.getById(cancellationRechargeReq.getId());
                CancellationRechargeDTO cancellationRechargeDTO = new CancellationRechargeDTO();
                BeanUtils.copyProperties(cancellationRecharge,cancellationRechargeDTO);

                return RestResult.ok(cancellationRechargeDTO);
            }catch(Exception e){
                e.printStackTrace();
                return RestResult.failed("获取记录信息失败");
            }
        }



    }
