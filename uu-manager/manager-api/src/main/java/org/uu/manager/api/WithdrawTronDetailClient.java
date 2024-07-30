package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.WithdrawTronDetailDTO;
import org.uu.common.pay.dto.WithdrawTronDetailExportDTO;
import org.uu.common.pay.req.WithdrawTronDetailReq;

import java.util.List;


/**
 * @author afei
 */
@FeignClient(value = "uu-wallet", contextId = "withdraw-tron-detail")
public interface WithdrawTronDetailClient {


    /**
     * 代付钱包交易记录分页
     *
     * @param
     * @return
     */
    @PostMapping("/withdraw-tron-detail/withdrawTronDetailPage")
    RestResult<List<WithdrawTronDetailDTO>> withdrawTronDetailPage(@RequestBody WithdrawTronDetailReq req);

    /**
     * 代付钱包交易记录分页导出
     *
     * @param
     * @return
     */
    @PostMapping("/withdraw-tron-detail/withdrawTronDetailPageExport")
    RestResult<List<WithdrawTronDetailExportDTO>> withdrawTronDetailPageExport(@RequestBody WithdrawTronDetailReq req);


}
