package org.uu.manager.controller;


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
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.ApplyDistributedClient;
import org.uu.manager.api.WindrawalCancellationClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
* @author 
*/
@Slf4j
@RequiredArgsConstructor
@RestController
@Api(description = "出款取消原因控制器")
@RequestMapping(value = {"/api/v1/withdrawalCancellation", "/withdrawalCancellation"})
public class WithdrawalCancellationController {
    @Resource
    WindrawalCancellationClient windrawalCancellationClient;
    @PostMapping("/listpage")
    @ApiOperation(value = "获取出款取消原因列表")
    public RestResult<List<WithdrawalCancellationDTO>> list(@RequestBody @ApiParam WithdrawalCancellationReq withdrawalCancellationReq) {
        RestResult<List<WithdrawalCancellationDTO>> result = windrawalCancellationClient.listpage(withdrawalCancellationReq);
        return result;
    }

    @PostMapping("/create")
    @SysLog(title="出款取消原因控制器",content = "创建记录")
    @ApiOperation(value = "创建记录")
    public RestResult<WithdrawalCancellationDTO> create(@RequestBody @ApiParam WithdrawalCancellationCreateReq req) {

        RestResult<WithdrawalCancellationDTO> result = windrawalCancellationClient.create(req);
        return result;
    }

    @PostMapping("/update")
    @SysLog(title="出款取消原因控制器",content = "修改记录")
    @ApiOperation(value = "修改记录")
    public RestResult<WithdrawalCancellationDTO> update(@RequestBody @ApiParam WithdrawalCancellationAddReq req) {
        RestResult<WithdrawalCancellationDTO>  result =  windrawalCancellationClient.update(req);

        return result;
    }
    @PostMapping("/getInfo")
    @ApiOperation(value = "获取记录详情")
    public RestResult<WithdrawalCancellationDTO> getInfo(@RequestBody @ApiParam WithdrawalCancellationIdReq req) {

        RestResult<WithdrawalCancellationDTO>  result =  windrawalCancellationClient.getInfo(req);
        return result;
    }

    @PostMapping("/delete")
    @SysLog(title="出款取消原因控制器",content = "删除记录详情")
    @ApiOperation(value = "删除记录详情")
    public RestResult delete(@RequestBody @ApiParam WithdrawalCancellationIdReq req) {
      try {
       RestResult result =   windrawalCancellationClient.delete(req);
          return result;
      }catch(Exception e){
          e.printStackTrace();
          return RestResult.failed("删除记录失败");
      }
    }

    }
