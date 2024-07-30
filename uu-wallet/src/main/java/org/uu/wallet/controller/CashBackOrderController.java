package org.uu.wallet.controller;

import io.swagger.annotations.ApiParam;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.CashBackOrderListPageDTO;
import org.uu.common.pay.req.CashBackOrderListPageReq;
import org.uu.wallet.service.ICashBackOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;

/**
 * 余额退回控制器
 * @author admin
 */
@RestController
@RequestMapping(value = {"/api/v1/cashBackOrder", "/cashBackOrder"})
@ApiIgnore
public class CashBackOrderController {
    @Resource
    ICashBackOrderService cashBackOrderService;

    @PostMapping("/listPage")
    public PageReturn<CashBackOrderListPageDTO> listPage(@RequestBody @ApiParam CashBackOrderListPageReq req) throws ExecutionException, InterruptedException {
        return cashBackOrderService.listPage(req);
    }
}
