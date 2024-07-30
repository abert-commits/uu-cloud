package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.RechargeTronDetailDTO;
import org.uu.common.pay.dto.RechargeTronExportDTO;
import org.uu.common.pay.req.RechargeTronDetailReq;

import java.util.List;


/**
 * @author afei
 */
@FeignClient(value = "uu-wallet", contextId = "recharge-tron-detail")
public interface RechargeTronDetailClient {


    /**
     * 代收钱包交易记录分页
     *
     * @param
     * @return
     */
    @PostMapping("/recharge-tron-detail/rechargeTronDetailPage")
    RestResult<List<RechargeTronDetailDTO>> rechargeTronDetailPage(@RequestBody RechargeTronDetailReq req);

    /**
     * 代收钱包交易记录分页
     *
     * @param
     * @return
     */
    @PostMapping("/recharge-tron-detail/rechargeTronDetailPageExport")
    RestResult<List<RechargeTronExportDTO>> rechargeTronDetailPageExport(@RequestBody RechargeTronDetailReq req);


}
