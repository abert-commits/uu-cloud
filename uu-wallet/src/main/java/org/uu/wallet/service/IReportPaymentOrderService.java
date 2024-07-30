package org.uu.wallet.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.PaymentOrderDTO;
import org.uu.common.pay.req.PaymentOrderReq;
import org.uu.wallet.entity.PaymentOrder;


/**
 * @author
 */
public interface IReportPaymentOrderService extends IService<PaymentOrder> {

    PageReturn<PaymentOrderDTO> listDayPage(PaymentOrderReq req);

    PageReturn<PaymentOrderDTO> listMothPage(PaymentOrderReq req);

    PageReturn<PaymentOrderDTO> listDayTotal(PaymentOrderReq req);

    PageReturn<PaymentOrderDTO> listMothTotal(PaymentOrderReq req);


}
