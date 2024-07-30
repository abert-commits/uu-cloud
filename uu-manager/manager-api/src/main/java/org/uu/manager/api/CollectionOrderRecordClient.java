package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CollectionOrderRecordDTO;
import org.uu.common.pay.dto.CollectionOrderRecordExportDTO;
import org.uu.common.pay.req.CollectionOrderRecordReq;

import java.util.List;


/**
 * @author afei
 */
@FeignClient(value = "uu-wallet", contextId = "collection-order-record")
public interface CollectionOrderRecordClient {


    /**
     * 归集订单记录分页
     *
     * @param
     * @return
     */
    @PostMapping("/collection-order-record/collectionOrderRecordPage")
    RestResult<List<CollectionOrderRecordDTO>> collectionOrderRecordPage(@RequestBody CollectionOrderRecordReq req);

    /**
     * 归集订单记录导出
     *
     * @param
     * @return
     */
    @PostMapping("/collection-order-record/collectionOrderRecordPageExport")
    RestResult<List<CollectionOrderRecordExportDTO>> collectionOrderRecordPageExport(@RequestBody CollectionOrderRecordReq req);


}
