package org.uu.manager.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AppInfoDTO;
import org.uu.common.pay.req.AppInfoPageReq;
import org.uu.common.pay.req.AppInfoReq;

import java.util.List;


/**
 * @author afei
 */
@FeignClient(value = "uu-wallet", contextId = "app-info")
public interface AppInfoClient {


    /**
     * app配置记录
     *
     * @param
     * @return
     */
    @PostMapping("/app-info/appInfoPage")
    RestResult<List<AppInfoDTO>> appInfoPage(@RequestBody AppInfoPageReq req);

    /**
     * 新增 app配置
     */
    @PostMapping("/app-info/addAppInfo")
    RestResult addAppInfo(@RequestBody AppInfoReq req);

    /**
     * 更新 app配置
     */
    @PostMapping("/app-info/updateAppInfo/{id}")
    RestResult updateAppInfo(@PathVariable("id") Long id, @RequestBody AppInfoReq req);


}
