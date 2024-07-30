package org.uu.manager.service;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.pay.req.OrderMonitorReq;
import org.uu.manager.entity.MerchantInfo;
import org.uu.manager.entity.OrderMonitor;

import java.util.List;


/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2024-04-04
 */
public interface IOrderMonitorService extends IService<OrderMonitor> {


//    boolean saveMerchantPaymentOrders();
//    boolean saveCollectionOrder();
//    boolean savePaymentOrder();
//    boolean merchantCollectOrders();


     List<OrderMonitor> getOrderMonitorList(OrderMonitorReq req);

     List<OrderMonitor> getAllOrderMonitorListByDay(OrderMonitorReq req);


}
