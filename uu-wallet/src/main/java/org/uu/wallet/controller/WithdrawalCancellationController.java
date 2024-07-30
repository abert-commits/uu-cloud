package org.uu.wallet.controller;


import cn.hutool.core.bean.BeanUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.WithdrawalCancellationDTO;
import org.uu.common.pay.req.WithdrawalCancellationAddReq;
import org.uu.common.pay.req.WithdrawalCancellationCreateReq;
import org.uu.common.pay.req.WithdrawalCancellationIdReq;
import org.uu.common.pay.req.WithdrawalCancellationReq;
import org.uu.wallet.entity.MerchantInfo;

import org.uu.wallet.entity.WithdrawalCancellation;
import org.uu.wallet.req.MerchantInfoReq;

import org.uu.wallet.service.IWithdrawalCancellationService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

    import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

/**
* @author 
*/
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(description = "出款取消原因控制器")
@RequestMapping(value = {"/api/v1/withdrawalCancellation", "/withdrawalCancellation"})
@ApiIgnore
public class WithdrawalCancellationController {
    private final IWithdrawalCancellationService withdrawalCancellationService;
    @PostMapping("/listpage")
    @ApiOperation(value = "获取出款取消原因列表")
    public RestResult<List<WithdrawalCancellationDTO>> listpage(@RequestBody @ApiParam WithdrawalCancellationReq withdrawalCancellationReq) {


        PageReturn<WithdrawalCancellationDTO> withdrawalCancellationPage = withdrawalCancellationService.listPage(withdrawalCancellationReq);
        return RestResult.page(withdrawalCancellationPage);
    }

    @PostMapping("/create")
    @ApiOperation(value = "创建记录")
    public RestResult<WithdrawalCancellationDTO> create(@RequestBody @ApiParam WithdrawalCancellationCreateReq req) {
        WithdrawalCancellation  withdrawalCancellation  = new WithdrawalCancellation();
        BeanUtils.copyProperties(req,withdrawalCancellation);
         withdrawalCancellationService.save(withdrawalCancellation);
        WithdrawalCancellationDTO withdrawalCancellationDTO = new WithdrawalCancellationDTO();
        BeanUtils.copyProperties(withdrawalCancellation,withdrawalCancellationDTO);
        return RestResult.ok(withdrawalCancellationDTO);
    }

    @PostMapping("/update")
    @ApiOperation(value = "修改记录")
    public RestResult<WithdrawalCancellationDTO> update(@RequestBody @ApiParam WithdrawalCancellationAddReq req) {
        WithdrawalCancellation  withdrawalCancellation  = new WithdrawalCancellation();
        BeanUtil.copyProperties(req,withdrawalCancellation);
        withdrawalCancellationService.updateById(withdrawalCancellation);
        WithdrawalCancellationDTO withdrawalCancellationDTO = new WithdrawalCancellationDTO();
        BeanUtils.copyProperties(withdrawalCancellation,withdrawalCancellationDTO);
        return RestResult.ok(withdrawalCancellationDTO);
    }
    @PostMapping("/getInfo")
    @ApiOperation(value = "获取记录详情")
    public RestResult<WithdrawalCancellationDTO> getInfo(@RequestBody @ApiParam WithdrawalCancellationIdReq req) {

        WithdrawalCancellation  withdrawalCancellation =  withdrawalCancellationService.getById(req.getId());
        WithdrawalCancellationDTO withdrawalCancellationDTO = new WithdrawalCancellationDTO();
        BeanUtils.copyProperties(withdrawalCancellation,withdrawalCancellationDTO);
        return RestResult.ok(withdrawalCancellationDTO);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除记录详情")
    public RestResult delete(@RequestBody @ApiParam WithdrawalCancellationIdReq req) {
      try {
          WithdrawalCancellation withdrawalCancellation = new WithdrawalCancellation();
          BeanUtils.copyProperties(req,withdrawalCancellation);
          withdrawalCancellationService.removeById(withdrawalCancellation);
          return RestResult.ok("删除成功");
      }catch(Exception e){
          e.printStackTrace();
          return RestResult.failed("删除记录失败");
      }
    }

    }
