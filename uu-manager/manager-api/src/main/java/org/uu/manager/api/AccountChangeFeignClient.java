package org.uu.manager.api;

import org.uu.common.core.result.RestResult;
import org.uu.common.pay.req.AccountChangeReq;
import org.uu.manager.dto.AccountChangeDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "account-change")
public interface AccountChangeFeignClient {




    @PostMapping("/accountChange/query")
    RestResult<List<AccountChangeDTO>> listPage(@RequestBody AccountChangeReq req);


    @PostMapping("/accountChange/queryTotal")
    RestResult<AccountChangeDTO> queryTotal(@RequestBody AccountChangeReq req);

    /**
     * 获取账变类型
     * @return
     */
    @PostMapping("/accountChange/fetchAccountType")
    RestResult<Map<Integer, String>> fetchAccountType();
}
