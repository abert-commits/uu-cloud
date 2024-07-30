package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.vertx.core.json.JsonObject;
import org.uu.common.core.page.PageRequestHome;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AppealOrderDTO;
import org.uu.common.pay.dto.AppealOrderExportDTO;
import org.uu.common.pay.req.AppealOrderIdReq;
import org.uu.common.pay.req.AppealOrderPageListReq;
import org.uu.wallet.entity.AppealOrder;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.vo.AppealDetailsVo;
import org.uu.wallet.vo.AppealOrderVo;
import org.uu.wallet.vo.ViewMyAppealVo;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;

/**
 * @author
 */
public interface IAppealOrderService extends IService<AppealOrder> {

    AppealOrderVo queryAppealOrder(String orderNo, Integer appealType) throws Exception;



    AppealOrderDTO pay(AppealOrderIdReq req);

    AppealOrderDTO nopay(AppealOrderIdReq req);



    PageReturn<AppealOrderDTO> listPage(AppealOrderPageListReq req) throws ExecutionException, InterruptedException;
    PageReturn<AppealOrderExportDTO> listPageExport(AppealOrderPageListReq req) throws ExecutionException, InterruptedException;

    /**
     * 根据买入订单号获取申诉订单
     *
     * @param platformOrder
     */
    AppealOrder getAppealOrderByBuyOrderNo(String platformOrder);


    /**
     * 根据卖出订单号获取申诉订单
     *
     * @param platformOrder
     */
    AppealOrder getAppealOrderBySellOrderNo(String platformOrder);

    /**
     * 查看订单申诉详情
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link AppealDetailsVo}>
     */
    RestResult<AppealDetailsVo> viewAppealDetails(PlatformOrderReq platformOrderReq, String type);

    /**
     * 我的申诉
     *
     * @param pageRequestHome
     * @return {@link RestResult}<{@link PageReturn}<{@link ViewMyAppealVo}>>
     */
    RestResult<PageReturn<ViewMyAppealVo>> viewMyAppeal(PageRequestHome pageRequestHome);

    /**
     * 变更会员信用分
     *
     * @param orderSuccess
     * @param buyerId
     * @param sellerId
     * @param appealOrder
     */
    void changeCreditScore(boolean orderSuccess, String buyerId, String sellerId, AppealOrder appealOrder);
}
