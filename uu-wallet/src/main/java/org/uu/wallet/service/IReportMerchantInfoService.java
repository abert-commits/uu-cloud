package org.uu.wallet.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.pay.dto.MerchantInfoReportDTO;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.common.pay.req.MerchantInfoReq;



/**
 * @author
 */
public interface IReportMerchantInfoService extends IService<MerchantInfo> {


    PageReturn<MerchantInfoReportDTO> listDayPage(MerchantInfoReq req);


}
