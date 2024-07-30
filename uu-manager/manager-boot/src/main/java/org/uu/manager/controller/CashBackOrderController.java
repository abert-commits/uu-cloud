package org.uu.manager.controller;

import io.swagger.annotations.ApiParam;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.CashBackOrderListPageDTO;
import org.uu.common.pay.req.CashBackOrderListPageReq;
import org.uu.manager.api.CashBackOrderClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 余额退回控制器
 * @author admin
 */
@RestController
@RequestMapping(value = {"/api/v1/cashBackOrderAdmin", "/cashBackOrderAdmin"})
public class CashBackOrderController {
    @Resource
    CashBackOrderClient cashBackOrderClient;

    @PostMapping("/listPage")
    public RestResult<List<CashBackOrderListPageDTO>> listPage(@RequestBody @ApiParam CashBackOrderListPageReq req) throws ExecutionException, InterruptedException {
        PageReturn<CashBackOrderListPageDTO> result = cashBackOrderClient.listPage(req);
        return RestResult.page(result);
    }

}
