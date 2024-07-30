package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.C2cConfigDTO;
import org.uu.common.pay.dto.CollectionInfoDTO;
import org.uu.common.pay.req.C2cConfigReq;
import org.uu.common.pay.req.CollectionInfoIdReq;
import org.uu.common.pay.req.CollectionInfoListPageReq;
import org.uu.common.pay.req.CollectionInfoReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "collection-info")
public interface CollectionInfoClient {


    /**
     *
     * @param
     * @return
     */
    @PostMapping("/api/v1/collectionInfo/listPage")
    RestResult<List<CollectionInfoDTO>> listpage(@RequestBody CollectionInfoListPageReq req);

    /**
     *
     * @param req
     * @return
     */



    /**
     * 详情
     * @param
     * @param
     * @return
     */
    @PostMapping("/api/v1/collectionInfo/update")
    RestResult<CollectionInfoDTO> update(@RequestBody CollectionInfoReq req);

    @PostMapping("/api/v1/collectionInfo/getInfo")
    RestResult<List<CollectionInfoDTO>> getInfo(@RequestBody CollectionInfoIdReq req);



    @PostMapping("/api/v1/collectionInfo/delete")
    RestResult delete(@RequestBody CollectionInfoIdReq req);


    @PostMapping("/api/v1/collectionInfo/add")
    RestResult<CollectionInfoDTO> add(CollectionInfoReq collectionInfoReq);
}
