package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.*;

import java.util.List;


/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "tron-wallet")
public interface TronWalletClient {

    /**
     * 波场钱包地址管理分页列表
     */
    @PostMapping("/tron-wallet/addressListPage")
    RestResult<List<TronWalletAddressDTO>> addressListPage(@RequestBody UsdtAddrPageReq req);
}
