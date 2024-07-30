package org.uu.wallet.service;



import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;

import org.uu.common.pay.dto.CollectionOrderDTO;

import org.uu.wallet.entity.CollectionOrder;
import org.uu.common.pay.req.CollectionOrderReq;


/**
 * @author
 */
public interface IReportCollectionOrderService extends IService<CollectionOrder> {

    PageReturn<CollectionOrderDTO> listDayPage(CollectionOrderReq req);

    PageReturn<CollectionOrderDTO> listMothPage(CollectionOrderReq req);


    PageReturn<CollectionOrderDTO> listDayPageTotal(CollectionOrderReq req);

    PageReturn<CollectionOrderDTO> listMothPageTotal(CollectionOrderReq req);





}
