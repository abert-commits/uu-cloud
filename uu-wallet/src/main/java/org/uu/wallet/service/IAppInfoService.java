package org.uu.wallet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.AppInfoDTO;
import org.uu.common.pay.req.AppInfoPageReq;
import org.uu.common.pay.req.AppInfoReq;
import org.uu.wallet.entity.AppInfo;

/**
 * <p>
 * app信息维护表 服务类
 * </p>
 *
 * @author
 * @since 2024-07-25
 */
public interface IAppInfoService extends IService<AppInfo> {

    PageReturn<AppInfoDTO> appInfoPage(AppInfoPageReq req);

    RestResult<AppInfoDTO> getAppInfoByDevice(Integer device);

    RestResult addAppInfo(AppInfoReq req);

    RestResult updateAppInfo(Long id, AppInfoReq req);
}
