package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CancellationRechargeDTO;
import org.uu.common.pay.req.CancellationRechargeAddReq;
import org.uu.common.pay.req.CancellationRechargeIdReq;
import org.uu.common.pay.req.CancellationRechargePageListReq;
import org.uu.common.pay.req.CancellationRechargeReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.ApplyDistributedClient;
import org.uu.manager.api.CancellationRechargeClient;

import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
    @RequiredArgsConstructor
    @RestController
    @Api(description = "充值订单取消原因控制器")
    @RequestMapping(value = {"/api/v1/cancellationRecharge", "/cancellationRecharge"})
    public class CancellationRechargeController {
    @Resource
    CancellationRechargeClient cancellationRechargeClient;

        @PostMapping("/listpage")
        @ApiOperation(value = "充值订单取消原因列表")
        public RestResult<List<CancellationRechargeDTO>> list(@RequestBody @ApiParam CancellationRechargePageListReq req) {
            RestResult<List<CancellationRechargeDTO>> result  = cancellationRechargeClient.listpage(req);
            return result;
        }

        @PostMapping("/create")
        @SysLog(title="充值订单取消原因控制器",content = "新增")
        @ApiOperation(value = "新增")
        public RestResult<CancellationRechargeDTO> create(@RequestBody @ApiParam CancellationRechargeAddReq cancellationRechargeReq) {
            try {
               RestResult<CancellationRechargeDTO> result =  cancellationRechargeClient.create(cancellationRechargeReq);
                return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return RestResult.failed("创建记录失败");
                }
            }

            @PostMapping("/update")
            @SysLog(title="充值订单取消原因控制器",content = "更新")
            @ApiOperation(value = "更新")
            public RestResult<CancellationRechargeDTO> update(@RequestBody @ApiParam CancellationRechargeReq cancellationRechargeReq) {
                try {
                  RestResult<CancellationRechargeDTO> result = cancellationRechargeClient.update(cancellationRechargeReq);
                    return result;
                }catch(Exception e){
                    e.printStackTrace();
                    return RestResult.failed("修改记录失败");
                }
            }


        @PostMapping("/delete")
        @ApiOperation(value = "删除")
        @SysLog(title="充值订单取消原因控制器",content = "删除")
        public RestResult<CancellationRechargeDTO> delete(@RequestBody @ApiParam CancellationRechargeIdReq req) {
            try {
                RestResult<CancellationRechargeDTO>  result = cancellationRechargeClient.delete(req);
                return result;
            }catch(Exception e){
                e.printStackTrace();
                return RestResult.failed("删除记录失败");
            }
        }


        @PostMapping("/getInfo")
        @ApiOperation(value = "详情")
        public RestResult<CancellationRechargeDTO> get(@RequestBody @ApiParam CancellationRechargeIdReq req) {
            try {

               // BeanUtils.copyProperties(cancellationRechargeReq, cancellationRecharge);
                RestResult<CancellationRechargeDTO> result  =cancellationRechargeClient.getInfo(req);
                return result;
            }catch(Exception e){
                e.printStackTrace();
                return RestResult.failed("获取记录信息失败");
            }
        }



    }
