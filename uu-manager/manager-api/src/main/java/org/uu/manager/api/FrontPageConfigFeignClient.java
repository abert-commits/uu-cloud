package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.FrontPageConfigInfoDTO;
import org.uu.common.pay.req.UpdateFrontPageConfigReq;

import java.util.List;

/**
 * @author Admin
 */
@FeignClient(value = "uu-wallet", contextId = "frontPageConfig")
public interface FrontPageConfigFeignClient {

    /**
     * 查询首页弹窗内容
     */
    @PostMapping("/frontPageConfig/frontPageConfigList")
    RestResult<List<FrontPageConfigInfoDTO>> frontPageConfigList();

    /**
     * 修改首页弹窗内容
     */
    @PostMapping("/frontPageConfig/updateFrontPageConfig")
    RestResult<Boolean> updateFrontPageConfig(@RequestBody UpdateFrontPageConfigReq requestVO);
}
