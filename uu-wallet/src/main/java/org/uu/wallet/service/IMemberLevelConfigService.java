package org.uu.wallet.service;

import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.pay.dto.MemberLevelConfigDTO;
import org.uu.common.pay.req.MemberManualLogsReq;
import org.uu.wallet.entity.MemberLevelConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.boot.CommandLineRunner;

/**
 * <p>
 * 会员等级配置 服务类
 * </p>
 *
 * @author 
 * @since 2024-04-09
 */
public interface IMemberLevelConfigService extends IService<MemberLevelConfig>, CommandLineRunner {

    PageReturn<MemberLevelConfigDTO> listPage(MemberManualLogsReq req);

    RestResult updateInfo(MemberLevelConfigDTO req);
}
