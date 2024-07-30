package org.uu.manager.service;


import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.*;
import org.uu.common.pay.req.CommonDateLimitReq;
import org.uu.common.pay.req.MemberInfoIdReq;
import org.uu.manager.entity.BiOverViewStatisticsDaily;
import org.uu.manager.entity.MerchantInfo;
import org.uu.manager.req.MerchantDailyReportReq;
import org.uu.manager.req.MerchantInfoReq;
import org.uu.manager.vo.MerchantInfoVo;
import org.uu.manager.vo.MerchantNameListVo;

import java.util.List;


/**
 * @author
 */
public interface IMerchantInfoService extends IService<MerchantInfo> {


    PageReturn<MerchantInfo> listPage(MerchantInfoReq req);

    List<MerchantInfo> getAllMerchantByStatus();


     String getMd5KeyByCode(String merchantCode);



    boolean getIp(String code, String addr);

    MerchantInfo getMerchantInfoByCode(String code);




    /*
     * 根据商户号获取md5Key
     * */


    /*
     * 获取商户名称列表
     * */
    List<MerchantNameListVo> getMerchantNameList();


     MerchantInfoVo currentMerchantInfo();


     MerchantInfo userDetail(Long userId);


     UserAuthDTO getByUsername(String username);
    RestResult<BiOverViewStatisticsDailyDTO>  getMerchantOrderOverview(MerchantDailyReportReq req);
}
