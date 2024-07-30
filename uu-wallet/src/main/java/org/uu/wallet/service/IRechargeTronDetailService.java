package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.RechargeTronDetailDTO;
import org.uu.common.pay.dto.RechargeTronExportDTO;
import org.uu.common.pay.req.RechargeTronDetailReq;
import org.uu.wallet.entity.RechargeTronDetail;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 钱包交易记录 服务类
 * </p>
 *
 * @author
 * @since 2024-07-03
 */
public interface IRechargeTronDetailService extends IService<RechargeTronDetail> {

    /**
     * 查询两天内 交易金额大于订单金额 并且未上分的订单
     *
     * @param toAddress
     * @param minRechargeAmount
     * @return {@link List }<{@link RechargeTronDetail }>
     */
    RechargeTronDetail getLatestPendingOrderWithLock(String toAddress, BigDecimal minRechargeAmount);


    /**
     * 查询两天内 交易金额大于订单金额 并且未上分的订单 TRX
     *
     * @param toAddress
     * @param minRechargeAmount
     * @return {@link List }<{@link RechargeTronDetail }>
     */
    RechargeTronDetail getLatestPendingOrderWithLockTRX(String toAddress, BigDecimal minRechargeAmount);


    Map<String, RechargeTronDetail> getRechargeTronDetailByTxid(List<String> txids);

    /**
     * 根据收款地址和金额匹配交易记录  存在则填充订单号并响应更新结果  不存在则响应false
     *
     * @param orderId   需要填充的订单号
     * @param toAddress 收款地址
     * @param amount    交易金额
     */
    Boolean queryAndFillOrderId(@NotEmpty String orderId, @NotEmpty String toAddress, @NotNull BigDecimal amount);

    PageReturn<RechargeTronDetailDTO> rechargeTronDetailPage(RechargeTronDetailReq req);

    PageReturn<RechargeTronExportDTO> rechargeTronDetailPageExport(RechargeTronDetailReq req);
}
