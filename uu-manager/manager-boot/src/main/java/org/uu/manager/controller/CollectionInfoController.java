package org.uu.manager.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CollectionInfoDTO;
import org.uu.common.pay.req.CollectionInfoIdReq;
import org.uu.common.pay.req.CollectionInfoListPageReq;
import org.uu.common.pay.req.CollectionInfoReq;
import org.uu.manager.annotation.SysLog;
import org.uu.manager.api.CollectionInfoClient;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = {"/api/v1/collectionInfoAdmin", "/collectionInfoAdmin"})
@Api(description = "收款信息控制器")
public class CollectionInfoController {

    private final CollectionInfoClient collectionInfoClient;

    @PostMapping("/update")
    @SysLog(title="收款信息控制器",content = "修改会员信息")
    @ApiOperation(value = "修改会员信息")
    public RestResult<CollectionInfoDTO> update(@RequestBody @ApiParam @Valid CollectionInfoReq collectionInfoReq) {
        RestResult<CollectionInfoDTO> result = collectionInfoClient.update(collectionInfoReq);
        return result;
    }

    @PostMapping("/add")
    @SysLog(title="收款信息控制器",content = "添加收款信息")
    @ApiOperation(value = "添加收款信息")
    public RestResult<CollectionInfoDTO> add(@RequestBody @ApiParam @Valid CollectionInfoReq collectionInfoReq) {
        RestResult<CollectionInfoDTO> result = collectionInfoClient.add(collectionInfoReq);
        return result;
    }


    @PostMapping("/getInfo")
    @ApiOperation(value = "收款信息")
    public RestResult<List<CollectionInfoDTO>> getInfo(@RequestBody @ApiParam @Valid CollectionInfoIdReq collectionInfoReq) {
        RestResult<List<CollectionInfoDTO>> result = collectionInfoClient.getInfo(collectionInfoReq);
        return result;
    }


    @PostMapping("/delete")
    @SysLog(title="收款信息控制器",content = "删除")
    @ApiOperation(value = "删除")
    public RestResult delete(@RequestBody @ApiParam @Valid CollectionInfoIdReq collectionInfoReq) {
        RestResult restResult = collectionInfoClient.delete(collectionInfoReq);
        return restResult;
    }

    @PostMapping("/listPage")
    @ApiOperation(value = "收款列表")
    public RestResult<List<CollectionInfoDTO>> listPage(@RequestBody @ApiParam @Valid CollectionInfoListPageReq req) {
        RestResult<List<CollectionInfoDTO>> result = collectionInfoClient.listpage(req);

        return result;
    }
}
