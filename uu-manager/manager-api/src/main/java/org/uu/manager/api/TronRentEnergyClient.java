package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.TronRentEnergyDTO;
import org.uu.common.pay.dto.TronRentEnergyExportDTO;
import org.uu.common.pay.req.TronRentEnergyReq;

import java.util.List;


/**
 * @author afei
 */
@FeignClient(value = "uu-wallet", contextId = "tron-rent-energy")
public interface TronRentEnergyClient {


    /**
     * 能量租用记录
     *
     * @param
     * @return
     */
    @PostMapping("/tron-rent-energy/tronRentEnergyListPage")
    RestResult<List<TronRentEnergyDTO>> tronRentEnergyListPage(@RequestBody TronRentEnergyReq req);

    /**
     * 能量租用记录导出
     *
     * @param
     * @return
     */
    @PostMapping("/tron-rent-energy/tronRentEnergyExport")
    RestResult<List<TronRentEnergyExportDTO>> tronRentEnergyExport(@RequestBody TronRentEnergyReq req);


}
