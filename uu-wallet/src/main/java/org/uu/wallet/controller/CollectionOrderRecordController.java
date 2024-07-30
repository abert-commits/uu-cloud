package org.uu.wallet.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CollectionOrderRecordDTO;
import org.uu.common.pay.dto.CollectionOrderRecordExportDTO;
import org.uu.common.pay.req.CollectionOrderRecordReq;
import org.uu.wallet.service.ICollectionOrderRecordService;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 归集订单记录 前端控制器
 * </p>
 *
 * @author
 * @since 2024-07-20
 */
@RestController
@RequestMapping("/collection-order-record")
public class CollectionOrderRecordController {
    @Resource
    private ICollectionOrderRecordService collectionOrderRecordService;


    @PostMapping("/collectionOrderRecordPage")
    @ApiOperation(value = "归集订单记录分页列表")
    public RestResult<List<CollectionOrderRecordDTO>> collectionOrderRecordPage(@RequestBody @ApiParam CollectionOrderRecordReq req) {
        PageReturn<CollectionOrderRecordDTO> pageReturn = collectionOrderRecordService.collectionOrderRecordPage(req);
        return RestResult.page(pageReturn);
    }

    @PostMapping("/collectionOrderRecordPageExport")
    @ApiOperation(value = "归集订单记录分页列表导出")
    public RestResult<List<CollectionOrderRecordDTO>> collectionOrderRecordPageExport(@RequestBody @ApiParam CollectionOrderRecordReq req) {
        PageReturn<CollectionOrderRecordExportDTO> pageReturn = collectionOrderRecordService.collectionOrderRecordPageExport(req);
        return RestResult.page(pageReturn);
    }


}
